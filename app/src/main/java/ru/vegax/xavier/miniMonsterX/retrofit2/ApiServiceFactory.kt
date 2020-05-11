package ru.vegax.xavier.miniMonsterX.retrofit2

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiServiceFactory {
    private const val DEF_URL = "http://192.168.0.12/"

    fun createService(useGson: Boolean): ControlDataApi {
        val gSonConverter = GsonBuilder()
                .setLenient()
                .create()
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(JsonInterceptor())
        val builder = Retrofit.Builder()
                .baseUrl(DEF_URL)
        if (useGson) builder.addConverterFactory(GsonConverterFactory.create(gSonConverter))
        builder.client(httpClient.build())
        return builder
                .build()
                .create(ControlDataApi::class.java)
    }
}