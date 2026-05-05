package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //access localhost from emulator
    private const val BASE_URL = "http://10.0.2.2:5001/"

    //creates api when needed
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            //sets for all api request
            .baseUrl(BASE_URL)
            //converts JSON responses from backend into kotlin objects
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            //creates api service from ApiService file
            .create(ApiService::class.java)
    }
}
