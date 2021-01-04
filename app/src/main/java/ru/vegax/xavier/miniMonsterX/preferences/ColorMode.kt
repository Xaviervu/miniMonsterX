package ru.vegax.xavier.miniMonsterX.preferences

import androidx.annotation.StringRes
import ru.vegax.xavier.miniMonsterX.R

enum class ColorMode(@StringRes val colorRes: Int) {
    SYSTEM(R.string.systemDefault), LIGHT(R.string.lightMode), DARK(R.string.darkMode);

    companion object {
        fun fromOrdinal(ordinal: Int): ColorMode {
            for (value in values()) {
                if (value.ordinal == ordinal) return value
            }
            throw IllegalArgumentException("Unknown ordinal $ordinal for ColorMode")
        }
    }
}