# PomoDojo

A Pomodoro timer: the user runs focus and break periods back to back, and finished runs are recorded for later review.

## Session

**Session**:
One run of the Pomodoro timer, from start until the user finishes it or its last Segment completes. At most one Session is active at a time.
_Avoid_: focus session (when meaning the whole run), snapshot

**Timeline**:
The ordered Segments of a Session, together with its Hour splits.

**Timeline plan**:
The Timeline that Preferences produce before anything has started: every Segment is waiting and none has a start time.

**Segment**:
One timed block in a Timeline: a focus, short break or long break period with a fixed duration.
_Avoid_: phase (in code; user-facing copy may still say "phase")

**Cycle**:
One focus Segment and the break that follows it. A Session's length is set as a number of Cycles.

**Gate**:
The pause between Segments after one finishes on its own: the next Segment does not start until the user chooses to continue. Time spent at the Gate is not counted.
_Avoid_: awaiting continue, phase transition

**Hour split**:
The Timeline's total length divided into consecutive hour-long pieces plus a remainder, used to show the Timeline against a clock-like ruler.

## Outcomes

**Completion summary**:
What a finished Session amounted to: the minutes actually spent in focus and in breaks, and the number of Cycles finished. A Segment that was skipped or ended early only counts the time it actually ran.

**Cycle finished**:
A focus Segment that ran to completion. Skipping or ending a focus Segment early does not finish its Cycle.

**History**:
The record of finished Sessions, grouped by day, that the dashboard reports on.
