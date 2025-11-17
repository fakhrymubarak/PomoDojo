package com.fakhry.pomodojo.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val POMO_DOJO_MIGRATIONS: Array<Migration> = arrayOf(Migration1To2)

private object Migration1To2 : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS active_session_segments")
        connection.execSQL("DROP TABLE IF EXISTS active_session_hour_splits")
        connection.execSQL("DROP TABLE IF EXISTS active_sessions")
    }
}
