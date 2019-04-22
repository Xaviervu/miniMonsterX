package ru.vegax.xavier.minimonsterx.retrofit2

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException;
import ru.vegax.xavier.minimonsterx.BuildConfig



public class jsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request)
        var rawJson = response.body()!!.string()
        if (!rawJson.contains("}")) {
            rawJson += "}"
        }
        return response.newBuilder()
                .body(ResponseBody.create(response.body()!!.contentType(), rawJson)).build()
    }

}