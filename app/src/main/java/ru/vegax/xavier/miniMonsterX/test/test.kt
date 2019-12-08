package ru.vegax.xavier.miniMonsterX.test

fun main() {
    val j = readLine()
    val s = readLine()

    var result = 0
    for (element in s!!) {
        if (j!!.indexOf(element) >= 0) {
            ++result
        }
    }
   // println(args.size)
    println(result)
}