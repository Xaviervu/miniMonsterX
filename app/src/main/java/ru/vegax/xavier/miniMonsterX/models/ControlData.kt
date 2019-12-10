package ru.vegax.xavier.miniMonsterX.models

import com.google.gson.annotations.SerializedName

class ControlData {
    @SerializedName("fwv")
    lateinit var fwv: String
    @SerializedName("id")
    lateinit var id: String
    @SerializedName("prt")
    lateinit var prt: List<Int>
    @SerializedName("pst")
    lateinit var pst: List<Int>
    @SerializedName("t")
    lateinit var t: List<String>
    @SerializedName("wdr")
    lateinit var wdr: List<Int>
    @SerializedName("pwm1")
    var pwm1: Int = 0
    @SerializedName("pwm2")
    var pwm2: Int = 0
    @SerializedName("pwmt")
    var pwmt: Int = 0

}