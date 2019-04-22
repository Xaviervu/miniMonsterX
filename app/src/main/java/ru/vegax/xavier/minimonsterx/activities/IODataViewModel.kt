package ru.vegax.xavier.minimonsterx.activities

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
import ru.vegax.xavier.minimonsterx.iodata.IOItem
import ru.vegax.xavier.minimonsterx.models.ControlData
import ru.vegax.xavier.minimonsterx.repository.LoadingStatus
import ru.vegax.xavier.minimonsterx.retrofit2.ApiServiceFactory
import java.util.concurrent.TimeUnit


internal class IODataViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = "XavvViewModel"

//    val dao = NewsDb.get(app).newsDao()
    val mLoadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus> = mLoadingStatus

    val mIOData = MutableLiveData<List<IOItem>>()
    val controlData: LiveData<List<IOItem>> = mIOData

//    val allDevices = dao.allDevices()
//
//    fun clearAndInsert(devices: List<DeviceData>) = ioThread {
//        dao.deleteAll()
//        dao.insert(devices)
//    }
//
//    fun insert(deviceData: DeviceData) = ioThread {
//        dao.insert(deviceData)
//    }
//
//    fun remove(device: DeviceData) = ioThread {
//        dao.delete(device)
//    }

    //REST functions and values

    private var cyclicRequest: Disposable = CompositeDisposable()
    private var outRequest: Disposable? = null
    private var impulseRequest: Disposable? = null

    private val apiService by lazy {
        cyclicRequest.dispose()
        ApiServiceFactory.createService()
    }

    fun getDataCyclically(baseUrl: String){
        if ( cyclicRequest.isDisposed){
            val url = "$baseUrl?js="
            mLoadingStatus.postValue(LoadingStatus.LOADING)
            cyclicRequest =
                    Flowable.interval(0, REFRESH_TIME, TimeUnit.MILLISECONDS)
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
                                        },
                                    {
                                        cyclicRequest.dispose()
                                        Log.d(TAG, it.message)
                                        mLoadingStatus.postValue(LoadingStatus.ERROR)
                                    })
        }
    }


    fun setOutput(baseUrl:String, outputN :Int, on:Boolean) { //set outputN on or off
        val url = "$baseUrl?sw=$outputN-" + (if (on) "1" else {"0"}) //?sw={output}-{on} outputNumber 1..6; on = "1" off = "0"
        outRequest = apiService.setOutput(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {  mLoadingStatus.postValue(LoadingStatus.ERROR) }
                )
    }
    fun setImpulse(baseUrl:String, outputN :Int) { //toggle outputN for n seconds (time set up at minimonsterController
        val url = "$baseUrl?rst=$outputN"  //"?rst={output}" outputNumber 1..6
        impulseRequest = apiService.setImpulse(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {  mLoadingStatus.postValue(LoadingStatus.ERROR) }
                )
    }

    fun dispose() {
       cyclicRequest.dispose()
        if (outRequest!= null){
            outRequest!!.dispose()
        }
        if(impulseRequest != null){
            impulseRequest!!.dispose()
        }
    }
    private fun getIOData(data:ControlData):List<IOItem>{
        var outN = 0
        var inN = 0
        return data.prt.mapIndexed { i, _ ->
            val isOutput = data.pst[i] == 1
            val name :String
            name = if (isOutput){
                outN ++
                "Output$outN"
            }else{
                inN ++
                "Input$inN"
            }
            // todo: get names and impulse from db
            // todo make it work as is changing
            IOItem(name,isOutput,data.prt[i]== 1,false,false)}
    }

companion object {
    const val REFRESH_TIME = 250L //ms
}
}
