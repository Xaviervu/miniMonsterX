package ru.vegax.xavier.miniMonsterX.activities

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.auxiliar.ioThread
import ru.vegax.xavier.miniMonsterX.iodata.IOItem
import ru.vegax.xavier.miniMonsterX.models.ControlData
import ru.vegax.xavier.miniMonsterX.repository.DeviceData
import ru.vegax.xavier.miniMonsterX.repository.DevicesDb
import ru.vegax.xavier.miniMonsterX.repository.LoadingStatus
import ru.vegax.xavier.miniMonsterX.retrofit2.ApiServiceFactory


internal class IODataViewModel(val app: Application) : AndroidViewModel(app) {


    private val dao = DevicesDb.get(app).newsDao()
    private val mLoadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus> = mLoadingStatus

    private val mIOData = MutableLiveData<List<IOItem>>()
    val controlData: LiveData<List<IOItem>> = mIOData

    var curDevice: DeviceData? = null
        private set
    private val mCurrDeviceLiveData = MutableLiveData<DeviceData>()
    val currentDeviceLiveData: LiveData<DeviceData> = mCurrDeviceLiveData

    val allDevices = dao.allDevices()

    fun clearAndInsert(devices: List<DeviceData>) = ioThread {
        dao.deleteAll()
        dao.insert(devices)
    }

    fun insert(deviceData: DeviceData) = ioThread {
        val newId = dao.insert(deviceData)
        getDevice(newId)
    }

    fun update(deviceData: DeviceData) = ioThread {
        dao.update(deviceData)
    }

    fun remove(device: DeviceData) = ioThread {
        dao.delete(device)
    }

    fun remove(deviceId: Long) = ioThread {
        dao.delete(deviceId)
    }

    fun getDevice(deviceID: Long) = ioThread {
        curDevice = dao.currDevice(deviceID)
        mCurrDeviceLiveData.postValue(curDevice)
        cyclicRequest?.cancel()
        getDataCyclically()
    }
    //REST functions and values

    private var cyclicRequest: Job? = null
    private var outRequest: Job? = null
    private var impulseRequest: Job? = null

    private val apiService by lazy {
        ApiServiceFactory.createService()
    }

    fun getDataCyclically() {
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?js="
            mLoadingStatus.postValue(LoadingStatus.LOADING)
            cyclicRequest = viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    while (cyclicRequest?.isCancelled == false) {
                        try {
                            val ioData = apiService.getControlData(url)
                            mIOData.postValue(getIOData(ioData))
                            delay(REFRESH_TIME)
                        } catch (e: Throwable) {
                            mLoadingStatus.postValue(LoadingStatus.ERROR)
                            cyclicRequest?.cancel()
                            Log.e(TAG, app.getString(R.string.loading_data_error), e)
                        } finally {
                            mLoadingStatus.postValue(LoadingStatus.NOT_LOADING)
                        }
                    }
                }
            }
        }
    }


    fun setOutput(outputN: Int, on: Boolean) { //set outputN on or off
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?sw=$outputN-" + if (on) "1" else "0" //?sw={output}-{on} outputNumber 1..6; on = "1" off = "0"
            outRequest = viewModelScope.launch {
                try {
                    apiService.setOutput(url)
                } catch (e: Throwable) {
                    Log.e(TAG, app.getString(R.string.setting_output_error), e)
                    mLoadingStatus.postValue(LoadingStatus.ERROR)
                }
            }
        }
    }

    fun setImpulse(outputN: Int) { //toggle outputN for n seconds (time set up at minimonsterController
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?rst=$outputN" //"?rst={output}" outputNumber 1..6
            impulseRequest = viewModelScope.launch {
                try {
                    apiService.setImpulse(url)
                } catch (e: Throwable) {
                    Log.e(TAG, app.getString(R.string.impulse_setting_error), e)
                    mLoadingStatus.postValue(LoadingStatus.ERROR)
                }
            }
        }
    }

    fun stopLoading() {
        cyclicRequest?.cancel()
        outRequest?.cancel()
        impulseRequest?.cancel()

    }

    private fun getIOData(data: ControlData): List<IOItem> {
        var outN = 0
        var inN = 0
        return data.prt.mapIndexed { i, _ ->
            val isOutput = data.pst[i] == 1
            val name: String
            name = if (isOutput) {
                outN++
                "Output$outN"
            } else {
                inN++
                "Input$inN"
            }
            IOItem(curDevice?.portNames?.get(i)
                    ?: name, isOutput, data.prt[i] == 1, curDevice?.impulseTypes?.get(i)
                    ?: false, false)
        }
    }

    companion object {
        const val REFRESH_TIME = 250L //ms
        private const val TAG = "XavvViewModel"
    }
}
