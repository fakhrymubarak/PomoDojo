package com.fakhry.pomodojo.focus.data.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

private val Migration1To2 =
    object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE active_sessions ADD COLUMN pause_started_at_epoch_ms INTEGER")
        }
    }

internal val FocusMigrations: Array<Migration> = arrayOf(Migration1To2)
