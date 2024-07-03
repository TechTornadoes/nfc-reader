package com.example.nfc_mobile_code.retrofit

import com.example.nfc_mobile_code.retrofit.model.CodeRequest
import com.example.nfc_mobile_code.retrofit.model.JwtResponse
import com.example.nfc_mobile_code.retrofit.model.JwtResponseNoCode
import com.example.nfc_mobile_code.retrofit.model.SessionCloseRequest
import com.example.nfc_mobile_code.retrofit.model.TokenRequest
import com.example.nfc_mobile_code.retrofit.model.TokenRequestInstant
import com.example.nfc_mobile_code.retrofit.model.ValidationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/validate-code")
    fun validateCode(@Body codeRequest: CodeRequest): Call<ValidationResponse>

    @POST("api/auth/authenticate")
    fun sendToken(@Body tokenRequest: TokenRequest): Call<JwtResponse>

    @POST("api/auth/close-session")
    fun closeSession(@Body sessionCloseRequest: SessionCloseRequest): Call<Void>

    @POST("api/auth/validate-sans-code")
    fun validateSansCode(@Body tokenRequestInstant: TokenRequestInstant): Call<JwtResponseNoCode>

}
