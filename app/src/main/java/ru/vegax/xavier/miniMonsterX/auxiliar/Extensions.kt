package ru.vegax.xavier.miniMonsterX.auxiliar

import android.view.View

fun View.show() {
    this.visibility = View.VISIBLE
}

private fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}
