package com.maasrahman.chitchat.data.network

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.maasrahman.chitchat.BuildConfig
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("messages")
    fun sendNotif(@Body params: RequestData) : Deferred<ResponseData>

    companion object {
        operator fun invoke(context: Context) : ApiService {
            val client = OkHttpClient.Builder()
                //.addInterceptor(ChuckInterceptor(context))
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}