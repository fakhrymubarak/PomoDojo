package com.fakhry.pomodojo.data.pomodoro.wiring

import com.fakhry.pomodojo.core.datastore.provideDataStore
import com.fakhry.pomodojo.data.pomodoro.repository.ActiveSessionRepositoryImpl
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository

fun activeSessionStoreFactory(): ActiveSessionRepository = ActiveSessionRepositoryImpl(
    provideDataStore()
)
