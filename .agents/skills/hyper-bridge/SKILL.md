--
name: hyper-bridge
description: HyperBridge is dynamic island on xiaomi Hyper OS. This is skills to integrate it on the android platform
--

This guide details how developers can integrate **Native Xiaomi HyperOS Dynamic Island Notifications** (Focus Notifications) directly into their apps without relying on third-party ZIP overlays.

## **1. Core Concept & Setup**

We use the native Kotlin SDK to build dynamic island bundles mapped to standard Android notifications:

**Dependency:**
```kotlin
dependencies {
    implementation("io.github.d4viddf:hyperisland_kit:0.4.0")
}
```

> [!WARNING]  
> The `hyperisland_kit` has a static `minSdkVersion 35` declared. To use it on older Android versions where HyperOS is still supported (e.g., Android 13/14, SDK 33-34), you MUST add an override in your `AndroidManifest.xml`:
> ```xml
> <uses-sdk tools:overrideLibrary="io.github.d4viddf.hyperisland_kit" />
> ```

---

## **2. Dispatching a Dynamic Island Notification**

The SDK uses the `HyperIslandNotification.Builder` to construct bundles and JSON params that get attached to a standard `NotificationCompat.Builder`. Always ensure you isolate this API to Xiaomi devices explicitly to prevent crashes.

### **Kotlin Implementation**

```kotlin
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo

fun dispatchDynamicIsland(context: Context, notificationManager: NotificationManagerCompat) {
    if (!Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) return

    try {
        val picKey = "app_icon"
        val pic = HyperPicture(picKey, context, android.R.drawable.ic_popup_reminder)
        
        // 1. Initialise Builder securely
        val builder = HyperIslandNotification.Builder(context, "custom_island", "Custom Island")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .setSmallIsland(picKey)

        // 2. Map Actions (Used when Island is Expanded)
        val actAction = HyperAction(
            key = "action_key",
            title = "Run",
            icon = null,
            pendingIntent = yourPendingIntent(),
            actionIntentType = 1,
            actionBgColor = "#E0E0E0" // background optionally sets button appearance
        )
        builder.addAction(actAction)

        // 3. Set standard collapsed "Hint Timer" or Highlight style (Template 11 example)
        builder.setHintTimer(
            frontText1 = "Status",
            mainText1 = "Running...",
            action = actAction // Specific templates mandate action parameters directly
        )

        // 4. (CRITICAL) Big Island Config
        builder.setBigIslandInfo(
            left = ImageTextInfoLeft(
                type = 1,
                picInfo = PicInfo(type = 1, pic = picKey),
                textInfo = TextInfo(title = "Task", content = "Running...")
            ),
            actionKeys = listOf("action_key") // Bind any actions you registered earlier!
        )

        // 5. Append bundles to a standard Notification instance
        val notification = NotificationCompat.Builder(context, "your_channel_id")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Dynamic Island")
            .addExtras(builder.buildResourceBundle())
            .setGroup("dynamic_island") // prevents shade duplication
            .build()
            
        notification.extras.putString("miui.focus.param", builder.buildJsonParam())

        // 6. Notify
        notificationManager.notify(4001, notification)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}
```

---

## **3. Critical Constraints & Fixes**

### **SystemUI `islandExpandedViewnull` Error**
If you register action buttons using `.addAction(...)` or `.setHintTimer(..., action = action)` **without** providing a `BigIsland` layout context, the Xiaomi `FocusPlugin` (System UI component) will **crash on inflation** and register an `islandExpandedViewnull` anomaly in Logcat. 
**Fix:** Any time you use Action arguments that trigger expansion, you **MUST** call `.setBigIslandInfo(...)` to populate exactly what the System UI should draw natively when the pill is long-pressed!

### **Duplicate Notification Drawers**
The `NotificationCompat` object that drives your Dynamic Island config will also natively appear in the user's notification drawer. If you intend to dispatch a Dynamic island *alongside* your standard Foreground Service Notification, ensure you give the Dynamic Island notification a completely independent `NotificationID`, otherwise substituting them will overwrite your Foreground Service! Use `setGroup` to manage drawer clutter. 

---

## **4. Legacy Intent (HyperBridge Overlay)**

If the user strictly demands integrating with the legacy 3rd Party `HyperBridge` app overlay (which requires generating a `.hbr` zip asset file mapped to `theme_config.json`), use intent dispatch:

```kotlin
val intent = Intent("com.d4viddf.hyperbridge.APPLY_THEME").apply {  
    setDataAndType(uri, "application/zip")  
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  
}
context.startActivity(intent)
```
*(Only use the legacy method if requested explicitly since the Native Library directly injects to OS-level UI)*
