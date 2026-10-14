package com.fakhry.pomodojo.features.update

internal enum class UpdatePrompt {
    /** Blocks the app until the user updates. */
    FORCE,

    /** Offers an update the user may decline. */
    SOFT,

    /** A downloaded update waits for a restart to install. */
    RESTART,
}
