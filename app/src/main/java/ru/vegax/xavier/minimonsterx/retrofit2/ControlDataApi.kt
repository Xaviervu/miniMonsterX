package ru.vegax.xavier.minimonsterx.retrofit2


import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Url
import ru.vegax.xavier.minimonsterx.models.ControlData

interface ControlDataApi {

    @GET
    fun getControlData(@Url url:String): Single<ControlData>
    @GET
    fun setOutput(@Url url: String): Single<Unit> // outputNumber 1..6; on = "1" off = "0"
   @GET
   fun setImpulse(@Url url: String): Single<Unit> // outputNumber 1..6

}
