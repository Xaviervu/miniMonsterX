package ru.vegax.xavier.miniMonsterX.preferences

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ru.vegax.xavier.miniMonsterX.activities.BaseActivity

object Preferences {
    private var isAndroidTv: Boolean = false
    lateinit var sharedPreferences: SharedPreferences
    lateinit var appContext:Context
    fun initPrefs(applicationContext: BaseActivity) {
        isAndroidTv = applicationContext.isAndroidTV()
        appContext = applicationContext
        sharedPreferences = applicationContext.getSharedPreferences(applicationContext.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    // ------------------- ColorMode ------------------
    fun getSystemColorMode(): ColorMode {
        val defaultColorMode = if(isAndroidTv)  ColorMode.DARK else ColorMode.SYSTEM
        return ColorMode.fromOrdinal(sharedPreferences.getInt("pref_color_mode", defaultColorMode.ordinal))
    }
    fun toggleSystemColorMode():ColorMode{
        val outColorMode = when(getSystemColorMode()){
            ColorMode.SYSTEM -> {
                ColorMode.LIGHT
            }
            ColorMode.LIGHT -> {
                ColorMode.DARK
            }
            else -> {
                ColorMode.SYSTEM
            }
        }
        setSystemColorMode(outColorMode)
        return outColorMode
    }
    private fun setSystemColorMode(colorMode: ColorMode) {
        sharedPreferences.edit().putInt("pref_color_mode", colorMode.ordinal).apply()
    }

}