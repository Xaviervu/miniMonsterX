package ru.vegax.xavier.minimonsterx.repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DevicesDao {

    @Query("SELECT * from device_table ORDER BY deviceName ASC")
    fun allDevices(): LiveData<List<DeviceData>>

    @Query("SELECT * from device_table WHERE deviceId = :deviceId LIMIT 1")
    fun currDevice(deviceId: Long): DeviceData

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(devices: List<DeviceData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(singleDevice: DeviceData): Long

    @Query("DELETE FROM device_table")
    fun deleteAll()

    @Delete
    fun delete(deviceData: DeviceData)

    @Query("DELETE FROM device_table WHERE deviceId = :deviceId")
    fun delete(deviceId: Long)
}