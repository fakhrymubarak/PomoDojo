package com.fakhry.pomodojo.core.utils.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class DispatcherProvider(
    val main: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val io: CoroutineDispatcher,
) {
    constructor() : this(Dispatchers.Main, Dispatchers.Default, Dispatchers.IO)

    constructor(testDispatcher: CoroutineDispatcher) : this(
        testDispatcher,
        testDispatcher,
        testDispatcher,
    )
}
