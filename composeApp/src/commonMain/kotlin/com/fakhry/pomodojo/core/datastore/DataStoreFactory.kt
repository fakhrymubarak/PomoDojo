package com.fakhry.pomodojo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

const val PREFERENCES_FILE_NAME = "pomodojo.preferences_pb"

internal expect fun provideDataStore(): DataStore<Preferences>
