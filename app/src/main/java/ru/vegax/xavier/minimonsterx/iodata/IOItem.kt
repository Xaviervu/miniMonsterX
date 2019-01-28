package ru.vegax.xavier.minimonsterx.iodata

data class IOItem
/**
 * Constructor for the Sport data model
 *
 * @param itemName The name of the item.
 * @param isOutput The item is of an output type.
 * @param isOn     The input/ output is on
 * @param isImpulse     The input/ output is in impulse mode
 */
(val itemName: String,
 var isOutput: Boolean,
 var isOn: Boolean,
 var isImpulse: Boolean,
 var isChanging: Boolean) {


}
