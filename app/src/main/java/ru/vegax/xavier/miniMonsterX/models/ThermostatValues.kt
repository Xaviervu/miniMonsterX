package ru.vegax.xavier.miniMonsterX.models

data class ThermostatValues(
        val thermostatIsOn: Boolean,
        val tPlusCal: Double = 0.0,
        val outputIsOn: Boolean = false,
        val targetT: Double = 0.0,
        val hysteresis: Double = 0.0,
        val calT: Double = 0.0
)
