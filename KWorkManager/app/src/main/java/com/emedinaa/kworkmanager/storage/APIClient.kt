package com.emedinaa.kworkmanager.storage

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class APIClient {

    private val API_BASE_URL = "https://obscure-earth-55790.herokuapp.com"
    private lateinit var servicesApiInterface:ServicesApiInterface

    fun build():ServicesApiInterface{
        var builder: Retrofit.Builder = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())

        var httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.addInterceptor(interceptor())

        var retrofit: Retrofit = builder.client(httpClient.build()).build()
        servicesApiInterface = retrofit.create(
                ServicesApiInterface::class.java)

        return servicesApiInterface
    }

    private fun interceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level=HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    interface ServicesApiInterface {
        @Multipart
        @POST("/api/photo")
        fun uploadFile(@Part userPhoto: MultipartBody.Part): Call<Any>
    }
}