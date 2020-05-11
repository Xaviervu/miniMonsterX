package ru.vegax.xavier.miniMonsterX.models

import com.google.gson.annotations.SerializedName

data class ControlData(
        @SerializedName("fwv")
        var fwv: String,
        @SerializedName("id")
        var id: String,
        @SerializedName("prt")
        var prt: List<Int>,
        @SerializedName("pst")
        var pst: List<Int>,
        @SerializedName("t")
        var t: List<String>,
        @SerializedName("wdr")
        var wdr: List<Int>,
        @SerializedName("pwm1")
        var pwm1: Int = 0,
        @SerializedName("pwm2")
        var pwm2: Int = 0,
        @SerializedName("pwmt")
    var pwmt: Int = 0
)