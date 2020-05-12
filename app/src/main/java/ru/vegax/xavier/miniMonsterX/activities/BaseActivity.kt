package ru.vegax.xavier.miniMonsterX.activities

import android.app.UiModeManager
import android.content.res.Configuration
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ru.vegax.xavier.miniMonsterX.R

abstract class BaseActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    fun setToolbar(toolbar: Toolbar) {
        this.toolbar = toolbar
        setSupportActionBar(toolbar)
    }

    fun seToolbarTitle(title: String) {
        toolbar?.title = title
    }

    fun isAndroidTV(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as? UiModeManager
        return (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }

    fun getToolbar() = toolbar
    fun replaceFragment(fragment: Fragment, @IdRes id: Int, tag: String,
                        @AnimatorRes enter: Int = R.animator.slide_in, @AnimatorRes exit: Int = R.animator.slide_in,
                        @AnimatorRes popEnter: Int = R.animator.slide_in, @AnimatorRes popExit: Int = R.animator.slide_in) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(id, fragment, tag)
                .commitAllowingStateLoss()
    }

    fun replaceFragmentWithBackStack(fragment: Fragment, @IdRes id: Int, tag: String,
                                     @AnimatorRes enter: Int = R.animator.slide_in, @AnimatorRes exit: Int = R.animator.slide_in,
                                     @AnimatorRes popEnter: Int = R.animator.slide_in, @AnimatorRes popExit: Int = R.animator.slide_in) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(id, fragment, tag)
                .addToBackStack(tag)
                .commitAllowingStateLoss()
    }
}