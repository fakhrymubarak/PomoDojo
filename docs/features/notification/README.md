# Notification & Alarm Architecture

This document describes how PomoDojo handles notifications and alarms across platforms. The flow
starts from `PomodoroSessionViewModel` in the shared layer and branches into platform-specific
implementations via the `PomodoroSessionNotifier` interface.

---

## High-Level Overview

```mermaid
flowchart TB
    subgraph Shared["Shared Layer (commonMain)"]
        VM["PomodoroSessionViewModel"]
        NI["PomodoroSessionNotifier\n(interface)"]
        SP["SoundPlayer\n(interface)"]
    end

    subgraph Android["Android Platform"]
        AN["AndroidFocusSessionNotifier"]
        NM["NotifManager\n(interface)"]
        AM["PomodojoAlarmManager\n(interface)"]
        DN["DefaultPomodojoNotifManager"]
        XN["XiaomiNotifManager"]
        AAM["AndroidPomodojoAlarmManager"]
        BR["NotificationSegmentProgressReceiver\n(BroadcastReceiver)"]
    end

    subgraph iOS["iOS Platform"]
        IN["IosFocusSessionNotifier"]
        LA["LiveActivityManager"]
        UNC["UNUserNotificationCenter"]
    end

    subgraph JVM["JVM / Desktop"]
        NOOP["NoOpPomodoroSessionNotifier"]
    end

    VM -- " show(notice) " --> NI
    VM -- " playSegmentCompleted() " --> SP
    NI -.-> AN
    NI -.-> IN
    NI -.-> NOOP
    AN --> NM
    AN --> AM
    NM -.-> DN
    NM -.-> XN
    AM -.-> AAM
    AAM -- " fires alarm " --> BR
    BR -- " show(notice) " --> AN
    IN --> LA
    IN --> UNC
```

---

## ViewModel → Notifier Flow

`PomodoroSessionNotifier` has one method, **`show(notice: SessionNotice)`**. A `SessionNotice` is
the Session projected at one moment by `PomodoroSessionDomain.toNotice(now)` in `domain/pomodoro`:

| Notice    | When                                         | Carries                                    |
|-----------|----------------------------------------------|--------------------------------------------|
| `Running` | A Segment is counting down                   | Segment, remaining time, **`endsAtMs`**    |
| `Paused`  | A Segment is paused                          | Segment, remaining time                    |
| `AtGate`  | A Segment ran out; the next waits (the Gate) | Finished Segment, next Segment             |
| `Cleared` | Not started, complete, or finished          | Session id only                            |

Adapters only render the notice. They arm their alarm or chime from `Running.endsAtMs` and
cancel it for every other notice; no status rules or clock reads live in the adapters.

```mermaid
flowchart TD
    RESTORE["restoreOrStartSession()"] --> NOTICE["showNotice(session.toNotice(now))"]
    TRANSITION["transition() — pause, resume,\nskip, continue, segment ran out"] --> NOTICE
    COMPLETE["completeSession()"] --> CLEARED["show(SessionNotice.Cleared(sessionId))"]
    NOTICE --> SHOW["PomodoroSessionNotifier.show()"]
    CLEARED --> SHOW
```

A plain tick (only the clock moved) shows nothing new: the platform countdowns run on their own
from `endsAtMs`.

---

## Android Platform

### Architecture

```mermaid
flowchart TD
    SHOW["AndroidFocusSessionNotifier\n.show(notice)"]
    SHOW -->|" Running / Paused / AtGate "| NOTIFY["NotifManager.notify(notice)"]
    SHOW -->|" Cleared "| NCANCEL["NotifManager.cancel(sessionId)"]
    SHOW -->|" Running "| ASCHEDULE["PomodojoAlarmManager\n.schedule(sessionId, endsAtMs)"]
    SHOW -->|" otherwise "| ACANCEL["PomodojoAlarmManager.cancel(sessionId)"]
```

Titles and status lines come from `SessionNoticeText.kt` (Android string resources).

### NotifManager Implementations

There are two `NotifManager` implementations, selected based on device manufacturer:

```mermaid
flowchart LR
    NI["NotifManager\n(interface)"]
    DN["DefaultPomodojoNotifManager\n(Standard Android)"]
    XN["XiaomiNotifManager\n(Xiaomi Dynamic Island)"]
    NI -.->|" standard devices "| DN
    NI -.->|" Xiaomi HyperOS "| XN
```

#### DefaultPomodojoNotifManager

- Creates a notification channel (`focus_session_channel`).
- Displays an **ongoing, silent notification** with:
    - Progress bar (`progressPercent()`; full at the Gate).
    - Chronometer countdown while running, using `setWhen(endsAtMs)`.
    - BigTextStyle with the motivational quote, or "Tap to continue" at the Gate.
- Uses `PRIORITY_MAX` for persistent visibility.

#### XiaomiNotifManager

- Uses `HyperIslandNotification.Builder` for **Xiaomi Dynamic Island** integration.
- While running, sends a countdown via `TimerInfo` and `setBigIslandCountdown(endsAtMs)`.
- Paused or at the Gate, shows static text instead (e.g. "12:00 remaining • Paused").
- Wraps notification extras with `miui.focus.param` JSON payload.

### Alarm Manager (Android)

```mermaid
flowchart TD
    subgraph AlarmScheduler["AndroidPomodojoAlarmManager"]
        SCHEDULE["schedule(sessionId, endsAtMs)"]
        CANCEL["cancel(sessionId)"]
        SEG_ALARM["scheduleSegmentCompletionAlarm\n(exact alarm at endsAtMs)"]
        PROG_ALARM["scheduleProgressUpdateAlarm\n(⚠️ currently disabled)"]
        CANCEL_SEG["cancelSegmentCompletionAlarm"]
        CANCEL_PROG["cancelProgressUpdateAlarm"]
    end

    subgraph AlarmAPI["Android AlarmManager"]
        EXACT["setExactAndAllowWhileIdle\n(RTC_WAKEUP)"]
        INEXACT["setAndAllowWhileIdle\n(fallback, API 31+)"]
    end

    SCHEDULE --> SEG_ALARM
    SCHEDULE --> PROG_ALARM
    CANCEL --> CANCEL_SEG
    CANCEL --> CANCEL_PROG
    SEG_ALARM --> EXACT
    SEG_ALARM -.->|" API 31+,\nno exact alarm permission "| INEXACT
    PROG_ALARM -.->|" ⚠️ DISABLED\n(bug: loop issue) "| EXACT
```

> **Note:** Progress update alarms are currently **disabled** (commented out in
> `AndroidPomodojoAlarmManager`) due to a known bug where they loop the notification update
> continuously.

### Broadcast Receiver Flow

When the alarm fires, `NotificationSegmentProgressReceiver` handles the intent:

```mermaid
flowchart TD
    ALARM["AlarmManager fires"] --> RECEIVER["NotificationSegmentProgressReceiver\n.onReceive()"]
    RECEIVER --> PERM_CHECK{"POST_NOTIFICATIONS\npermission granted?"}
    PERM_CHECK -->|No| RETURN_EARLY["Return (no action)"]
    PERM_CHECK -->|Yes| ASYNC["goAsync() — acquire wake lock"]
    ASYNC --> ACTION{"intent.action?"}
    ACTION -->|" ACTION_SEGMENT_COMPLETE "| PARK["ActiveSessionStore.update:\ntimeline.elapsedAt(now)\n(parks the Session at the Gate)"]
    ACTION -->|" ACTION_PROGRESS_UPDATE "| READ["ActiveSessionStore.session.first()"]
    PARK --> HAS_SESSION{"Session exists?"}
    READ --> HAS_SESSION
    HAS_SESSION -->|No| FINISH["pendingResult.finish()"]
    HAS_SESSION -->|Yes| RESCHEDULE["notifier.show(session.toNotice(now))"]
    RESCHEDULE --> PLAY_SOUND["SoundPlayer.playSegmentCompleted()\n(segment complete only)"]
    PLAY_SOUND --> FINISH
```

The receiver never starts the next Segment or finishes the Session: it only parks the Session at
the Gate, and the foreground finishes it (see `docs/adr/0001-background-alarm-only-parks-session.md`).
The update is atomic, so it cannot overwrite a change the app made at the same moment. When the
last Segment runs out the notice is `Cleared`, so no stale "00:00" notification is left behind.

### Android Request Code Scheme

| Purpose                    | Formula                                |
|----------------------------|----------------------------------------|
| Session notification ID    | `42_000 + sessionId.hashCode()`        |
| Segment completion alarm   | `42_000 + 1000 + sessionId.hashCode()` |
| Progress update alarm      | `42_000 + 2000 + sessionId.hashCode()` |
| Xiaomi Dynamic Island ID   | `42_000 + 4000 + sessionId.hashCode()` |

---

## iOS Platform

### Show Flow

```mermaid
flowchart TD
    SHOW["IosFocusSessionNotifier.show(notice)"]
    SHOW -->|" Running "| CHIME["Replace the segment chime:\nUNTimeIntervalNotificationTrigger\n(fires when the Segment ends)"]
    SHOW -->|" Paused "| CANCEL_CHIME["Cancel segment chimes"]
    SHOW -->|" AtGate "| KEEP["Keep the delivered\n'Phase complete' chime"]
    SHOW -->|" Cleared "| END["Cancel chimes +\nLiveActivityManager.endLiveActivity()"]
    CHIME --> LA{"Live Activities\nsupported?"}
    CANCEL_CHIME --> LA
    KEEP --> LA
    LA -->|" first notice for\nthis Session "| START["LiveActivityManager\n.startLiveActivity(notice)"]
    LA -->|" otherwise "| UPDATE["LiveActivityManager\n.updateLiveActivity(notice)"]
    START --> BRIDGE["PomodoroLiveActivityBridge (Obj-C)\n→ PomodoroLiveActivityManager (Swift)"]
    UPDATE --> BRIDGE
```

The bridge passes `endsAtEpochMs` (0 unless running) and `nextSegmentType` (set only at the
Gate). The widget counts down to `endsAtEpochMs`; at the Gate it shows the finished Segment at
00:00 with "Break next — tap to continue". If a Live Activity for the same Session already exists
(e.g. after the app was relaunched), the Swift side updates it instead of starting a second one.

### iOS Segment Completion Chime

Unlike Android's `AlarmManager`, iOS schedules a **local notification** that fires when the
running Segment ends (`UNTimeIntervalNotificationTrigger` with the notice's remaining time). It
plays `timer_notification.wav` and says "Phase complete — tap to continue".

---

## JVM / Desktop

The JVM (Desktop) platform uses `NoOpPomodoroSessionNotifier`, which does nothing:

```mermaid
flowchart LR
    VM["PomodoroSessionViewModel"] --> NOOP["NoOpPomodoroSessionNotifier"]
    NOOP --> NOTHING["show() → Unit"]
```

---

## DI Wiring

Dependency injection is handled via Koin with `expect/actual`:

```mermaid
flowchart TD
    subgraph Common["commonMain"]
        EXPECT["expect fun\nprovidePomodoroSessionNotifier()"]
        MODULE["notificationCoreModule\n(Koin module)"]
        MODULE --> EXPECT
    end

    subgraph Android["androidMain"]
        ACTUAL_A["actual fun → AndroidFocusSessionNotifier\n(via Koin GlobalContext)"]
    end

    subgraph iOS["iosMain"]
        ACTUAL_I["actual fun → IosFocusSessionNotifier()"]
    end

    subgraph JVM["jvmMain"]
        ACTUAL_J["actual fun → NoOpPomodoroSessionNotifier"]
    end

    EXPECT -.-> ACTUAL_A
    EXPECT -.-> ACTUAL_I
    EXPECT -.-> ACTUAL_J
```

---

## Key Files Reference

| Layer   | File                                     | Purpose                                  |
|---------|------------------------------------------|------------------------------------------|
| Domain  | `SessionNotice.kt`                       | `SessionNotice` + `toNotice(now)`        |
| Common  | `PomodoroSessionNotifier.kt`             | Interface: `show(notice)`                |
| Common  | `SoundPlayer.kt`                         | Interface for segment completion audio   |
| Common  | `NotificationFeatureModules.kt`          | Koin DI wiring                           |
| Common  | `PomodoroSessionNotificationProvider.kt` | `expect fun` provider                    |
| Android | `AndroidFocusSessionNotifier.kt`         | Orchestrates notification + alarm        |
| Android | `NotifManager.kt`                        | Notification display interface           |
| Android | `DefaultPomodojoNotifManager.kt`         | Standard Android notification            |
| Android | `XiaomiNotifManager.kt`                  | Xiaomi Dynamic Island notification       |
| Android | `PomodojoAlarmManager.kt`                | Alarm scheduling interface               |
| Android | `AndroidPomodojoAlarmManager.kt`         | Exact alarm scheduling                   |
| Android | `NotificationSegmentProgressReceiver.kt` | BroadcastReceiver for alarm intents      |
| Android | `SessionNoticeText.kt`                   | Notice → Android title / status text     |
| iOS     | `IosFocusSessionNotifier.kt`             | Live Activity + local notifications      |
| iOS     | `LiveActivityManager.kt`                 | Kotlin wrapper for Swift bridge          |
| JVM     | `PomodoroSessionNotifier.jvm.kt`         | No-op implementation                     |
| Feature | `PomodoroSessionViewModel.kt`            | Calls `show()`                           |

---

## Known Issues

> ⚠️ **Progress Update Alarm Loop Bug**
>
> The `scheduleProgressUpdateAlarm()` in `AndroidPomodojoAlarmManager` is **currently disabled**
> (commented out). When enabled, it creates an infinite loop where each alarm trigger re-schedules
> itself, causing continuous notification updates. The alarm fires → receiver calls
> `notifier.show()` → which calls `alarmManager.schedule()` → which schedules another alarm →
> repeat.
>
> **TODO:** Re-draw the flowchart diagram of notification and alarm triggers to identify the
> correct fix. See comment in `AndroidPomodojoAlarmManager.kt:86-87`.
