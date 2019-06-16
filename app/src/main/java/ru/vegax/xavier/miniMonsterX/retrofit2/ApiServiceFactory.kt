package ru.vegax.xavier.miniMonsterX.retrofit2

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object ApiServiceFactory {
    private const val DEF_URL = "http://192.168.1.12"

    fun createService(): ControlDataApi {

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(JsonInterceptor())
        return Retrofit.Builder()
                .baseUrl(DEF_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
                .create(ControlDataApi::class.java)
    }
}