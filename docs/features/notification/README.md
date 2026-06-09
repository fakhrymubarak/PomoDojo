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

    VM -- " schedule(snapshot) " --> NI
    VM -- " cancel(sessionId) " --> NI
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
    BR -- " schedule(snapshot) " --> AN
    IN --> LA
    IN --> UNC
```

---

## ViewModel → Notifier Flow

The `PomodoroSessionViewModel` triggers notifications and alarms through two methods on
`PomodoroSessionNotifier`:

- **`schedule(snapshot)`** — Creates/updates the notification with current session state.
- **`cancel(sessionId)`** — Dismisses the notification and cancels any pending alarms.

```mermaid
flowchart TD
    subgraph ViewModel["PomodoroSessionViewModel"]
        RESTORE["restoreOrStartSession()"]
        TICK["handleTick()"]
        TOGGLE["togglePauseResume()"]
        COMPLETE["completeActiveSession()"]
        NOTIF["updateNotification()"]
    end

    subgraph Throttle["Notification Throttle"]
        FORCE{"forceUpdate?"}
        THROTTLE{"now - lastUpdated\n≤ 5000ms?"}
    end

    subgraph Notifier["PomodoroSessionNotifier"]
        SCHEDULE["schedule(snapshot)"]
        CANCEL["cancel(sessionId)"]
    end

    RESTORE -- " after preparing session " --> NOTIF
    TICK -- " segment advanced " --> NOTIF
    TOGGLE -- " pause/resume " --> NOTIF
    NOTIF -- " session complete? " --> |Yes|CANCEL
NOTIF -- " session active " --> FORCE
FORCE -->|Yes|SCHEDULE
FORCE -->|No|THROTTLE
THROTTLE -->|Yes, skip|SKIP["Return (throttled)"]
THROTTLE -->|No|SCHEDULE

COMPLETE --> CANCEL
COMPLETE -- " resetNotificationThrottle() " --> RESET["lastUpdated = 0"]
```

### When `updateNotification()` Is Called

| Trigger                           | `forceUpdate` | Notes                                   |
|-----------------------------------|---------------|-----------------------------------------|
| `restoreOrStartSession()`         | `false`       | Initial session load/restore            |
| `togglePauseResume()`             | `true`        | Immediate update on pause/resume        |
| `handleTick()` (segment advanced) | `true`        | Immediate update when segment completes |

### Throttle Logic

To avoid flooding the notification system, `updateNotification()` throttles non-forced updates to
**at most once per 5 seconds** (`TICK_UPDATE_NOTIF_INTERVAL_MILLIS = 5_000L`).

---

## Android Platform

### Architecture

```mermaid
flowchart TD
    subgraph AndroidNotifier["AndroidFocusSessionNotifier"]
        SCHED["schedule(snapshot)"]
        CANC["cancel(sessionId)"]
    end

    subgraph Mapping["Snapshot → NotificationSummary"]
        MAP["PomodoroSessionDomain\n.toNotificationSummary(context, now)"]
    end

    subgraph NotifMgr["NotifManager (interface)"]
        NOTIFY["notify(summary)"]
        NCANCEL["cancel(sessionId)"]
    end

    subgraph AlarmMgr["PomodojoAlarmManager (interface)"]
        ASCHEDULE["schedule(summary)"]
        ACANCEL["cancel(sessionId)"]
    end

    SCHED --> MAP
    MAP --> NOTIFY
    MAP --> ASCHEDULE
    CANC --> NCANCEL
    CANC --> ACANCEL
```

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
    - Progress bar (`segmentProgressPercent`).
    - Chronometer countdown (when running, using `setWhen(finishTimeMillis)`).
    - BigTextStyle with the motivational quote.
- Uses `PRIORITY_MAX` for persistent visibility.

#### XiaomiNotifManager

- Uses `HyperIslandNotification.Builder` for **Xiaomi Dynamic Island** integration.
- Sends countdown timer via `TimerInfo` and `setBigIslandCountdown(finishTimeMillis)`.
- Wraps notification extras with `miui.focus.param` JSON payload.

### Alarm Manager (Android)

```mermaid
flowchart TD
    subgraph AlarmScheduler["AndroidPomodojoAlarmManager"]
        CHECK{"isPaused OR\nfinishTime ≤ 0?"}
        SEG_ALARM["scheduleSegmentCompletionAlarm\n(exact alarm at finishTimeMillis)"]
        PROG_ALARM["scheduleProgressUpdateAlarm\n(⚠️ currently disabled)"]
        CANCEL_SEG["cancelSegmentCompletionAlarm"]
        CANCEL_PROG["cancelProgressUpdateAlarm"]
    end

    subgraph AlarmAPI["Android AlarmManager"]
        EXACT["setExactAndAllowWhileIdle\n(RTC_WAKEUP)"]
        INEXACT["setAndAllowWhileIdle\n(fallback, API 31+)"]
    end

    CHECK -->|" paused / no finish time "| CANCEL_SEG
    CHECK -->|" paused / no finish time "| CANCEL_PROG
    CHECK -->|" running & finishTime > 0 "| SEG_ALARM
    CHECK -->|" running & finishTime > 0 "| PROG_ALARM
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
    ASYNC --> LOAD["Load active session\nfrom ActiveSessionRepository"]
    LOAD --> HAS_SESSION{"Session exists?"}
    HAS_SESSION -->|No| FINISH_1["pendingResult.finish()"]
    HAS_SESSION -->|Yes| ACTION{"intent.action?"}
    ACTION -->|" ACTION_SEGMENT_COMPLETE "| PROCESS["processSessionCompletion(session, now)"]
    PROCESS --> ADVANCE_LOOP["Loop: advance through\noverdue segments"]
    ADVANCE_LOOP --> SAVE{"Session modified?"}
    SAVE -->|Yes| PERSIST["Save updated session\nto ActiveSessionRepository"]
    SAVE -->|No| SKIP_SAVE["Skip save"]
    PERSIST --> RESCHEDULE["notifier.schedule(updatedSession)"]
    SKIP_SAVE --> RESCHEDULE
    RESCHEDULE --> PLAY_SOUND["SoundPlayer\n.playSegmentCompleted()"]
    ACTION -->|" ACTION_PROGRESS_UPDATE "| REFRESH["notifier.schedule(session)\n(refresh notification)"]
    PLAY_SOUND --> FINISH_2["pendingResult.finish()"]
    REFRESH --> FINISH_2
```

#### Segment Completion Processing

`processSessionCompletion()` handles the case where the alarm fires and the current segment's
remaining time has reached zero. It **loops through all overdue segments** to catch up if multiple
alarms were missed:

```mermaid
flowchart TD
    START["processSessionCompletion(session, now)"]
    FIND["Find active segment index\n(first RUNNING or PAUSED)"]
    LOOP{"Current segment\ncompleted?"}
    LOOP -->|" RUNNING & remaining = 0 "| FINALIZE["finalizeSegment()\n→ status = COMPLETED"]
    LOOP -->|" COMPLETED "| ALREADY["Already completed"]
    LOOP -->|" PAUSED / INITIAL / remaining > 0 "| DONE["Stop loop"]
    FINALIZE --> HAS_NEXT{"Has next segment?"}
    ALREADY --> HAS_NEXT
    HAS_NEXT -->|Yes| PREPARE["prepareSegmentForRun()\n→ status = RUNNING\n→ calculate finishedInMillis"]
    HAS_NEXT -->|No| DONE
    PREPARE --> NEXT_DONE{"Next segment\nalso overdue?"}
    NEXT_DONE -->|Yes| LOOP
    NEXT_DONE -->|No| DONE
    DONE --> RETURN["Return updated session"]
```

### Android Request Code Scheme

| Purpose                    | Formula                                |
|----------------------------|----------------------------------------|
| Session notification ID    | `42_000 + sessionId.hashCode()`        |
| Segment completion alarm   | `42_000 + 1000 + sessionId.hashCode()` |
| Progress update alarm      | `42_000 + 2000 + sessionId.hashCode()` |
| Completion notification ID | `42_000 + 3000 + sessionId.hashCode()` |
| Xiaomi Dynamic Island ID   | `42_000 + 4000 + sessionId.hashCode()` |

---

## iOS Platform

### Architecture

```mermaid
flowchart TD
    subgraph IOS_Notifier["IosFocusSessionNotifier"]
        SCHED["schedule(snapshot)"]
        CANC["cancel(sessionId)"]
    end

    subgraph LiveAct["LiveActivityManager"]
        LA_START["startLiveActivity()"]
        LA_UPDATE["updateLiveActivity()"]
        LA_END["endLiveActivity()"]
        LA_END_C["endLiveActivityWithCompletion()"]
    end

    subgraph LocalNotif["UNUserNotificationCenter"]
        COMP_NOTIF["Completion Notification\n(fires after 1 sec)"]
        SEG_CHIME["Segment Completion Chimes\n(time-interval triggers)"]
    end

    subgraph Bridge["Native Bridge"]
        OBJ_C["PomodoroLiveActivityBridge\n(Objective-C)"]
        SWIFT["Swift Live Activity"]
    end

    SCHED --> LIVE_CHECK{"LiveActivity\nsupported?"}
    LIVE_CHECK -->|Yes| FIRST_SEG{"First segment\n& no completed?"}
    FIRST_SEG -->|Yes| LA_START
    FIRST_SEG -->|No| LA_UPDATE
    LIVE_CHECK -->|No| SKIP_LA["Skip Live Activity"]
    SCHED --> ALL_DONE{"All segments\ncompleted?"}
    ALL_DONE -->|Yes| LA_END_C
    ALL_DONE -->|Yes| COMP_NOTIF
    ALL_DONE -->|No| CHIME_CHECK{"isPaused?"}
    CHIME_CHECK -->|No| SEG_CHIME
    CHIME_CHECK -->|Yes| CANCEL_CHIME["Cancel segment chimes"]
    CANC --> LA_END
    CANC --> CANCEL_ALL["Cancel pending &\ndelivered notifications"]
    LA_START --> OBJ_C --> SWIFT
    LA_UPDATE --> OBJ_C
    LA_END --> OBJ_C
    LA_END_C --> OBJ_C
```

### iOS Schedule Flow (Detailed)

```mermaid
flowchart TD
    START["schedule(snapshot)"]
    MAP["toIosNotificationSummary(now)"]
    START --> MAP
    MAP --> ALL_COMPLETE{"All segments\ncompleted?"}
    ALL_COMPLETE -->|Yes| END_LA["LiveActivityManager\n.endLiveActivityWithCompletion()"]
    END_LA --> COMP_NOTIF["Schedule completion\nUNNotification (1-sec delay)"]
    COMP_NOTIF --> CANCEL_CHIMES_1["cancelSegmentCompletionChime()"]
    ALL_COMPLETE -->|No| FIND_SEG["Find active segment\n(RUNNING or PAUSED)"]
    FIND_SEG --> CALC["Calculate remaining seconds\n& build schedule payload"]
    CALC --> PAUSED{"isPaused?"}
    PAUSED -->|No| SCHEDULE_CHIMES["scheduleSegmentCompletionChimes()\nfor each remaining segment"]
    PAUSED -->|Yes| CANCEL_CHIMES_2["cancelSegmentCompletionChime()"]
    CALC --> LA_SUPPORTED{"LiveActivity\nsupported?"}
    LA_SUPPORTED -->|No| DONE["Return"]
    LA_SUPPORTED -->|Yes| ALREADY_STARTED{"hasStartedForSession?"}
    ALREADY_STARTED -->|" No (first time) "| LA_START["LiveActivityManager\n.startLiveActivity(\n sessionId, quote,\n cycleNumber, segmentType,\n remainingSeconds,\n scheduleJson\n)"]
    ALREADY_STARTED -->|Yes| LA_UPDATE["LiveActivityManager\n.updateLiveActivity(\n cycleNumber, segmentType,\n remainingSeconds,\n scheduleJson\n)"]
```

### iOS Segment Completion Chimes

Unlike Android's `AlarmManager`, iOS schedules **local notifications** via
`UNTimeIntervalNotificationTrigger` for each remaining segment in the session. These serve as audio
chimes to alert the user when a segment ends.

```mermaid
flowchart TD
    BUILD["buildLiveActivitySchedulePayload(now)"]
    BUILD --> ENTRIES["For each non-completed segment:\n - type, cycleNumber\n - totalSeconds\n - startOffsetSeconds"]
    ENTRIES --> FOREACH["For each entry:\ncalculate fireInSeconds =\nstartOffset + totalSeconds"]
    FOREACH --> TRIGGER["UNTimeIntervalNotificationTrigger\n(fireInSeconds, repeats: false)"]
    TRIGGER --> REQUEST["UNNotificationRequest\nwith sound: timer_notification.wav"]
    REQUEST --> ADD["notificationCenter\n.addNotificationRequest()"]
```

---

## JVM / Desktop

The JVM (Desktop) platform uses `NoOpPomodoroSessionNotifier`, which does nothing:

```mermaid
flowchart LR
    VM["PomodoroSessionViewModel"] --> NOOP["NoOpPomodoroSessionNotifier"]
    NOOP --> NOTHING["schedule() → Unit\ncancel() → Unit"]
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
| Common  | `PomodoroSessionNotifier.kt`             | Interface: `schedule()` / `cancel()`     |
| Common  | `SoundPlayer.kt`                         | Interface for segment completion audio   |
| Common  | `CompletionSummaryMapper.kt`             | Maps session → completion summary        |
| Common  | `NotificationFeatureModules.kt`          | Koin DI wiring                           |
| Common  | `PomodoroSessionNotificationProvider.kt` | `expect fun` provider                    |
| Android | `AndroidFocusSessionNotifier.kt`         | Orchestrates notification + alarm        |
| Android | `NotifManager.kt`                        | Notification display interface           |
| Android | `DefaultPomodojoNotifManager.kt`         | Standard Android notification            |
| Android | `XiaomiNotifManager.kt`                  | Xiaomi Dynamic Island notification       |
| Android | `PomodojoAlarmManager.kt`                | Alarm scheduling interface               |
| Android | `AndroidPomodojoAlarmManager.kt`         | Exact alarm scheduling                   |
| Android | `NotificationSegmentProgressReceiver.kt` | BroadcastReceiver for alarm intents      |
| Android | `NotificationSummaryMapper.kt`           | Maps session → Android notification data |
| iOS     | `IosFocusSessionNotifier.kt`             | Live Activity + local notifications      |
| iOS     | `LiveActivityManager.kt`                 | Kotlin wrapper for Swift bridge          |
| iOS     | `LiveActivitySchedule.kt`                | Schedule payload builder                 |
| iOS     | `NotificationSummaryMapper.ios.kt`       | Maps session → iOS notification data     |
| JVM     | `PomodoroSessionNotifier.jvm.kt`         | No-op implementation                     |
| Feature | `PomodoroSessionViewModel.kt`            | Triggers `schedule()` / `cancel()`       |

---

## Known Issues

> ⚠️ **Progress Update Alarm Loop Bug**
>
> The `scheduleProgressUpdateAlarm()` in `AndroidPomodojoAlarmManager` is **currently disabled**
> (commented out). When enabled, it creates an infinite loop where each alarm trigger re-schedules
> itself, causing continuous notification updates. The alarm fires → receiver calls
> `notifier.schedule()` → which calls `alarmManager.schedule()` → which schedules another alarm →
> repeat.
>
> **TODO:** Re-draw the flowchart diagram of notification and alarm triggers to identify the
> correct fix. See comment in `AndroidPomodojoAlarmManager.kt:86-87`.
