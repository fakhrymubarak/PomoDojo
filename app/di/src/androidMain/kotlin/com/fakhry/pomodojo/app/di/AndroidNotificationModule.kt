package com.fakhry.pomodojo.app.di

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.core.notification.notifications.AndroidFocusSessionNotifier
import com.fakhry.pomodojo.core.notification.notifications.AndroidPomodojoAlarmManager
import com.fakhry.pomodojo.core.notification.notifications.DefaultPomodojoNotifManager
import com.fakhry.pomodojo.core.notification.notifications.NotifManager
import com.fakhry.pomodojo.core.notification.notifications.PomodojoAlarmManager
import com.fakhry.pomodojo.core.notification.notifications.XiaomiNotifManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidNotificationModule(): Module = module {
    single { androidContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager }
    single<PomodojoAlarmManager> { AndroidPomodojoAlarmManager(get(), get()) }

    single { NotificationManagerCompat.from(androidContext()) }
    single { AndroidFocusSessionNotifier(androidContext(), get(), get()) }
    single<NotifManager> {
        // Separate Notification Implementation based on Manufacturer
        when (Build.MANUFACTURER.uppercase()) {
            "XIAOMI" -> XiaomiNotifManager(androidContext(), get())
            else -> DefaultPomodojoNotifManager(androidContext(), get())
        }
    }
}
