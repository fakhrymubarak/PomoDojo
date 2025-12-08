package com.fakhry.pomodojo.domain.pomodoro.di

import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val focusDomainModule = module {
    factoryOf(::BuildTimerSegmentsUseCase)
    factoryOf(::BuildHourSplitTimelineUseCase)
}
