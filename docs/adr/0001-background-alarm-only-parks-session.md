# Background alarm only parks a Session; the foreground finishes it

When a Segment's alarm fires in the background (Android `NotificationSegmentProgressReceiver`), it only marks the elapsed Segment as completed and leaves the Session at the Gate — even for the last Segment. Finishing the Session (clearing it and recording History) happens only in the foreground, and only the caller that atomically takes the stored Session writes History. We chose this over letting the alarm finish the Session because the alarm and the foreground could both finish it and double-count History (History merges into the day's row, so a duplicate insert is not harmless), and because finishing in the foreground keeps the Completion summary screen and keeps History out of `core/notification`.

## Consequences

History is at-most-once: if the process dies between taking the Session and inserting History, that Session's History is lost. We accept that over the previous failure mode of counting it twice.
