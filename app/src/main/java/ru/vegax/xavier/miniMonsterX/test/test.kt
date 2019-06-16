package ru.vegax.xavier.miniMonsterX.test

fun main() {
    val j = readLine()
    val s = readLine()

    var result = 0
    for (i in 0 until s!!.length) {
        val ch = s[i]
        if (j!!.indexOf(ch) >= 0) {
            ++result
        }
    }
   // println(args.size)
    println(result)
}