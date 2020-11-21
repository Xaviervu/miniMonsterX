package ru.vegax.xavier.miniMonsterX.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "device_table", indices = [Index(value = ["deviceName"], unique = true)])
class DeviceData(@field:ColumnInfo(name = "deviceName") var deviceName: String,
                 @field:ColumnInfo(name = "url") var url: String,
                 @field:ColumnInfo(name = "password") var password: String,
                 @field:ColumnInfo(name = "portNames") var portNames: MutableList<String>,
                 @field:ColumnInfo(name = "impulseTypes") var impulseTypes: MutableList<Boolean>,
                 @field:ColumnInfo(name = "hiddenInputs", defaultValue = "[false,true,true,true,true,true]") var hiddenInputs: MutableList<Boolean>) {
    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "deviceId")
    var deviceId: Long = 0

}