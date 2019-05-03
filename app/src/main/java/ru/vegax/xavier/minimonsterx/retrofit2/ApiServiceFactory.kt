package ru.vegax.xavier.minimonsterx.retrofit2

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object ApiServiceFactory {
    val defUrl = "http://192.168.0.13/password/"

    fun createService(): ControlDataApi {

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(JsonInterceptor())
        return Retrofit.Builder()
                .baseUrl(defUrl)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
                .create(ControlDataApi::class.java)
    }
}