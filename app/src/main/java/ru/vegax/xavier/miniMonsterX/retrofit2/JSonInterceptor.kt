package ru.vegax.xavier.miniMonsterX.retrofit2

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody


class JsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request)
        var rawJson = response.body?.string() ?: ""
        if (rawJson.startsWith("{") && !rawJson.endsWith("}")) {
            rawJson += "}"
        }
        return response.newBuilder()
                .body(rawJson.toResponseBody(response.body?.contentType())).build()
    }

}