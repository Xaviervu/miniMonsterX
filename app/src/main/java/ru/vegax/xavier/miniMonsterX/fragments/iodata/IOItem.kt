package ru.vegax.xavier.miniMonsterX.fragments.iodata

data class IOItem
(var portId :Int,
 var itemName: String,
 var isOutput: Boolean,
 var isOn: Boolean,
 var temperature: Double?,
 var isImpulse: Boolean,
 var isChanging: Boolean,
 var isHidden: Boolean)