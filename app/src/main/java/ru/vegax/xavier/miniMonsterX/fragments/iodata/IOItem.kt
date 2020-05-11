package ru.vegax.xavier.miniMonsterX.fragments.iodata

data class IOItem
(var itemName: String,
 var isOutput: Boolean,
 var isOn: Boolean,
 var temperature: Double?,
 var isImpulse: Boolean,
 var isChanging: Boolean)