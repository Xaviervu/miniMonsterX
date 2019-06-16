package ru.vegax.xavier.miniMonsterX.activities

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.vegax.xavier.miniMonsterX.auxiliar.ioThread
import ru.vegax.xavier.miniMonsterX.iodata.IOItem
import ru.vegax.xavier.miniMonsterX.models.ControlData
import ru.vegax.xavier.miniMonsterX.repository.DeviceData
import ru.vegax.xavier.miniMonsterX.repository.DevicesDb
import ru.vegax.xavier.miniMonsterX.repository.LoadingStatus
import ru.vegax.xavier.miniMonsterX.retrofit2.ApiServiceFactory
import java.util.concurrent.TimeUnit


internal class IODataViewModel(app: Application) : AndroidViewModel(app) {


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

    private var loadCyclically = false
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
        cyclicRequest.dispose()
        getDataCyclically()
    }
    //REST functions and values

    private var cyclicRequest: Disposable = CompositeDisposable()
    private var outRequest: Disposable? = null
    private var impulseRequest: Disposable? = null

    private val apiService by lazy {
        ApiServiceFactory.createService()
    }

    fun getDataCyclically() {
        loadCyclically = true
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?js="
            mLoadingStatus.postValue(LoadingStatus.LOADING)
            cyclicRequest =
                    Flowable.interval(0L, REFRESH_TIME, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .onBackpressureLatest()
                            .flatMap {
                                apiService.getControlData(url).toFlowable()
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {

                                        mIOData.postValue(getIOData(it))
                                        mLoadingStatus.postValue(LoadingStatus.NOT_LOADING)
                                        if (!loadCyclically) {
                                            cyclicRequest.dispose()
                                        }
                                    },
                                    {
                                        cyclicRequest.dispose()
                                        Log.d(TAG, it.message ?: "error loading data Cyclically")
                                        mLoadingStatus.postValue(LoadingStatus.ERROR)
                                    })
        }
    }


    fun setOutput(outputN: Int, on: Boolean) { //set outputN on or off
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?sw=$outputN-" + if (on) "1" else "0" //?sw={output}-{on} outputNumber 1..6; on = "1" off = "0"
            outRequest = apiService.setOutput(url).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                Log.d(TAG, it.message ?: "error setting output")
                                mLoadingStatus.postValue(LoadingStatus.ERROR)
                            }
                    )
        }
    }

    fun setImpulse(outputN: Int) { //toggle outputN for n seconds (time set up at minimonsterController
        val curDev = curDevice
        if (curDev != null) {
            val url = "${curDev.url}${curDev.password}/?rst=$outputN" //"?rst={output}" outputNumber 1..6
            impulseRequest = apiService.setImpulse(url).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                Log.d(TAG, it.message ?: "error setting impulse")
                                mLoadingStatus.postValue(LoadingStatus.ERROR)
                            }
                    )
        }
    }

    fun stopLoading() {
        loadCyclically = false
        outRequest?.dispose()
        impulseRequest?.dispose()

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
