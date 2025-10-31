package com.fakhry.pomodojo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform