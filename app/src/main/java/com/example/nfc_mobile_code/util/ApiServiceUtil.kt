package com.example.nfc_mobile_code.util

import android.content.Context
import com.example.nfc_mobile_code.R
import com.example.nfc_mobile_code.retrofit.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiServiceUtil {
    private fun getBaseUrl(context: Context): String {
        val ip = context.getString(R.string.ip_resaux)
        return "http://$ip:5000/"
    }

    fun getApiService(context: Context): ApiService {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
