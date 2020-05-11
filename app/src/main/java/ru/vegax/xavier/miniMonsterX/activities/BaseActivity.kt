package ru.vegax.xavier.miniMonsterX.activities

import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.vegax.xavier.miniMonsterX.R

abstract class BaseActivity : AppCompatActivity() {

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