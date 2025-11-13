# USE_EXACT_ALARM Permission Justification

## Google Play Store Policy Compliance

This document provides justification for the `USE_EXACT_ALARM` permission used in PomoDojo, as
required by Google Play Store's Exact Alarm policy.

## Permission Declaration

```xml

<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

## App Category

**Alarm and Timer Application** - PomoDojo is a Pomodoro Technique timer application.

## Justification

### 1. Core Functionality Requirement

PomoDojo is a time management application based on the Pomodoro Technique. The app's **primary and
core functionality** is to provide precise, reliable timing for:

- **Focus sessions**: Typically 25-minute work intervals
- **Short breaks**: 5-minute rest periods
- **Long breaks**: 15-minute extended rest periods

Exact alarm scheduling is **essential** to the app's core purpose - users depend on precise timing
to manage their productivity workflows.

### 2. User-Facing Timer Notifications

The app displays persistent notifications showing:

- Current segment (Focus, Short Break, or Long Break)
- Live countdown timer using Android's Chronometer
- Current cycle progress (e.g., "Cycle 2 of 4")

**Why exact alarms are critical:**

- When a segment completes, the notification must update **immediately** to show the next segment
- Users rely on precise transitions between work and break periods
- Inexact alarms could cause delays of several minutes, breaking the Pomodoro workflow
- The timer must fire even when the device is in Doze mode or deep sleep

### 3. Technical Implementation

**Location in code:**
`composeApp/src/androidMain/kotlin/com/fakhry/pomodojo/focus/domain/AndroidFocusSessionNotifier.kt`

```kotlin
// Line 106-110: Schedules exact alarm when segment completes
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    finishTime,  // Exact time when segment should complete
    pendingIntent
)
```

The alarm is scheduled to fire precisely when a timer segment completes, triggering a notification
update via `SegmentCompletionReceiver`.

### 4. Why Inexact Alarms Are Insufficient

Using inexact alarms (like `setAndAllowWhileIdle()`) would:

- L Delay segment transitions by minutes
- L Break user trust in timer accuracy
- L Defeat the purpose of the Pomodoro Technique (which requires strict time intervals)
- L Lead to poor user experience when app is in background

### 5. Use Case Alignment with Google Policy

According
to [Google's Exact Alarm Policy](https://support.google.com/googleplay/android-developer/answer/13161072),
`USE_EXACT_ALARM` is permitted for:

> "Apps whose core functionality requires precise timing, such as alarm clock apps and timer apps"

**PomoDojo qualifies because:**

-  It is fundamentally a timer application
-  Precise timing is not optional - it's the core value proposition
-  Users explicitly set timers and expect exact notifications
-  The Pomodoro Technique methodology requires strict adherence to time intervals

### 6. User Transparency

Users are aware that PomoDojo is a timer app and expect:

- Notifications when segments complete
- Precise timing for work and break intervals
- Reliable operation even when the device is in low-power states

## Alternative Permissions Considered

### SCHEDULE_EXACT_ALARM

Also declared in the manifest, but requires user permission on Android 12+. `USE_EXACT_ALARM` is
more appropriate because:

- It's automatically granted for timer/alarm apps
- It doesn't require users to navigate to system settings
- It aligns with Google's policy for timer applications

## Compliance Summary

- **Permission**: `USE_EXACT_ALARM`
- **Category**: Alarm and Timer Application
- **Justification**: Core timer functionality requires precise segment transitions
- **User Benefit**: Accurate Pomodoro timing for productivity
- **Policy Alignment**: Explicitly permitted for timer apps per Google's Exact Alarm Policy

## References

- [Google Play Exact Alarm Policy](https://support.google.com/googleplay/android-developer/answer/13161072)
- [Android AlarmManager Documentation](https://developer.android.com/reference/android/app/AlarmManager)
- [Pomodoro Technique](https://en.wikipedia.org/wiki/Pomodoro_Technique)

---

**Last Updated**: 2025-11-13
**App Version**: Current development version
**Reviewer**: If you have questions about this permission usage, please contact the development
team.
