package com.fakhry.pomodojo.features.update

enum class UpdateType { NONE, SOFT, FORCE }

/**
 * Which update the installed [current] version code needs. A force update wins over a soft one;
 * a soft update the user already declined ([dismissed]) stays quiet until a newer [latest] ships.
 */
fun decideUpdate(current: Int, min: Int, latest: Int, dismissed: Int): UpdateType = when {
    current < min -> UpdateType.FORCE
    current < latest && latest > dismissed -> UpdateType.SOFT
    else -> UpdateType.NONE
}
