package com.fakhry.pomodojo.features.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionStore
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.compose.koinInject

private const val KEY_MIN_VERSION_CODE = "min_version_code"
private const val KEY_LATEST_VERSION_CODE = "latest_version_code"
private const val RELEASE_FETCH_INTERVAL_SECONDS = 3_600L

@Composable
actual fun AppUpdateHost() {
    val context = LocalContext.current
    val sessionStore = koinInject<ActiveSessionStore>()
    val dataStore = koinInject<DataStore<Preferences>>()
    val updateManager = remember { AppUpdateManagerFactory.create(context) }
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf(UpdateType.NONE) }
    var latest by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf<UpdatePrompt?>(null) }
    val sessionActive by remember { sessionStore.session.map { it != null } }
        .collectAsState(initial = true)

    val declineSoft = {
        prompt = null
        scope.launch {
            dataStore.edit { it[PreferenceKeys.DISMISSED_UPDATE_VERSION_CODE] = latest }
        }
    }
    // Backing out of Play's flow: a force update still blocks, a soft one counts as declined.
    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            if (type == UpdateType.FORCE) prompt = UpdatePrompt.FORCE else declineSoft()
        }
    }

    DisposableEffect(updateManager) {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) prompt = UpdatePrompt.RESTART
        }
        updateManager.registerListener(listener)
        onDispose { updateManager.unregisterListener(listener) }
    }

    LaunchedEffect(updateManager) {
        val (min, latestCode) = fetchVersionCodes(context)
        val dismissed = dataStore.data.first()[PreferenceKeys.DISMISSED_UPDATE_VERSION_CODE] ?: 0
        latest = latestCode
        type = decideUpdate(context.versionCode(), min, latestCode, dismissed)

        sessionStore.session.first { it == null }
        prompt = updateManager.startUpdate(type, launcher)
    }

    val current = prompt
    if (current != null && !sessionActive) {
        UpdatePromptOverlay(
            prompt = current,
            onConfirm = {
                when (current) {
                    UpdatePrompt.RESTART -> updateManager.completeUpdate()
                    // A force prompt stays up so the app is still blocked on return.
                    UpdatePrompt.FORCE -> context.openPlayStore()
                    UpdatePrompt.SOFT -> {
                        prompt = null
                        context.openPlayStore()
                    }
                }
            },
            onLater = {
                // A postponed restart asks again next launch, while the update stays downloaded.
                if (current == UpdatePrompt.SOFT) declineSoft() else prompt = null
            },
        )
    }
}

/**
 * Starts Play's in-app update for [type] and returns the prompt to show instead, if any: a pending
 * restart for an already-downloaded update, or the fallback when Play cannot run the flow.
 */
private suspend fun AppUpdateManager.startUpdate(
    type: UpdateType,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
): UpdatePrompt? {
    // Fails off Play (dev builds, App Distribution, sideloads); those get the fallback prompt.
    val info = runCatching { requestAppUpdateInfo() }.getOrNull()
    val playType = if (type == UpdateType.FORCE) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
    val playReady = info != null &&
        info.isUpdateTypeAllowed(playType) &&
        info.updateAvailability() in setOf(
            UpdateAvailability.UPDATE_AVAILABLE,
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
        )
    return when {
        info?.installStatus() == InstallStatus.DOWNLOADED -> UpdatePrompt.RESTART
        type == UpdateType.NONE -> null
        !playReady -> if (type == UpdateType.FORCE) UpdatePrompt.FORCE else UpdatePrompt.SOFT
        else -> {
            startUpdateFlowForResult(info, launcher, AppUpdateOptions.defaultOptions(playType))
            null
        }
    }
}

/** `min_version_code` and `latest_version_code`; cached or 0 (no update) when the fetch fails. */
private suspend fun fetchVersionCodes(context: Context): Pair<Int, Int> {
    val config = Firebase.remoteConfig
    runCatching {
        config.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds =
                    if (context.isDebuggable()) 0 else RELEASE_FETCH_INTERVAL_SECONDS
            },
        ).await()
        config.fetchAndActivate().await()
    }
    return config.getLong(KEY_MIN_VERSION_CODE).toInt() to
        config.getLong(KEY_LATEST_VERSION_CODE).toInt()
}

private fun Context.isDebuggable(): Boolean =
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

private fun Context.versionCode(): Int {
    val info = packageManager.getPackageInfo(packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        info.versionCode
    }
}

private fun Context.openPlayStore() {
    val market = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
    try {
        startActivity(market)
    } catch (_: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri(),
            ),
        )
    }
}
