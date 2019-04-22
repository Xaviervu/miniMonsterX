package ru.vegax.xavier.minimonsterx.iodata

data class IOItem
(val itemName: String,
 var isOutput: Boolean,
 var isOn: Boolean,
 var isImpulse: Boolean,
 var isChanging: Boolean)