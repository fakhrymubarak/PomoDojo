package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

class BuildFocusTimelineUseCase {

    operator fun invoke(preferences: PreferencesDomain): ImmutableList<TimelineSegmentUiModel> {
        data class SegmentBlueprint(
            val duration: Int,
            val build: (Int, Float) -> TimelineSegmentUiModel,
        )

        val blueprints = buildList {
            repeat(preferences.repeatCount) { index ->
                add(
                    SegmentBlueprint(
                        duration = preferences.focusMinutes,
                        build = TimelineSegmentUiModel::Focus,
                    ),
                )

                val reachedLongBreakPoint = preferences.longBreakEnabled && (index + 1) % preferences.longBreakAfter == 0
                val isLastFocus = index == preferences.repeatCount - 1

                if (!isLastFocus && reachedLongBreakPoint) {
                    add(
                        SegmentBlueprint(
                            duration = preferences.longBreakMinutes,
                            build = TimelineSegmentUiModel::LongBreak,
                        ),
                    )
                } else if (!isLastFocus) {
                    add(
                        SegmentBlueprint(
                            duration = preferences.breakMinutes,
                            build = TimelineSegmentUiModel::ShortBreak,
                        ),
                    )
                }
            }
        }

        val totalMinutes = blueprints.sumOf { it.duration }.takeIf { it > 0 } ?: return persistentListOf()

        val segments = blueprints.map { blueprint ->
            val weight = blueprint.duration / totalMinutes.toFloat()
            blueprint.build(blueprint.duration, weight)
        }

        return segments.toPersistentList()
    }
}
