package com.fakhry.pomodojo.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.IO

data class DispatcherProvider(
    val main: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val io: CoroutineDispatcher,
) {
    constructor() : this(Main, Default, Dispatchers.IO)

    constructor(testDispatcher: CoroutineDispatcher) : this(
        testDispatcher,
        testDispatcher,
        testDispatcher,
    )
}
