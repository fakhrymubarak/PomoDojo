package com.fakhry.pomodojo.catalog

private const val CORE = ":core"
private const val DATA = ":data"
private const val DOMAIN = ":domain"
private const val FEATURES = ":features"

object Core {
    const val DATABASE = "$CORE:database"
    const val DATASTORE = "$CORE:datastore"
    const val DESIGN_SYSTEM = "$CORE:designsystem"
    const val DI = "$CORE:di"
    const val FRAMEWORK = "$CORE:framework"
    const val UTILS = "$CORE:utils"
}

object Data {
    const val HISTORY = "$DATA:history"
    const val PREFERENCES = "$DATA:preferences"
}

object Domain {
    const val COMMON = "$DOMAIN:common"
    const val POMODORO = "$DOMAIN:pomodoro"
    const val HISTORY = "$DOMAIN:history"
    const val PREFERENCES = "$DOMAIN:preferences"
}

object Features {
    const val DASHBOARD = "$FEATURES:dashboard"
    const val POMODORO = "$FEATURES:pomodoro"
    const val PREFERENCES = "$FEATURES:preferences"
}
