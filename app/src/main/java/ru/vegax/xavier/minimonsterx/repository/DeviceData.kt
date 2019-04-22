package ru.vegax.xavier.minimonsterx.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "device_table")
class DeviceData(deviceName: String, url : String, portNames: List<String>, impulseTypes: List<Boolean>) {
    @field:PrimaryKey(autoGenerate = true)
    var homeId: Int = 0

    @field:ColumnInfo(name = "deviceName")
    val deviceName: String = deviceName

    @field:ColumnInfo(name = "url")
    val url: String = url

    @field:ColumnInfo(name = "portNames")
    val portNames: List<String> = portNames

    @field:ColumnInfo(name = "impulseTypes")
    val impulseTypes: List<Boolean> = impulseTypes
}