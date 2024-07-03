package com.example.nfc_mobile_code.util.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.nfc_mobile_code.retrofit.model.CodeRequest
import com.example.nfc_mobile_code.retrofit.model.ValidationResponse
import com.example.nfc_mobile_code.util.ApiServiceUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeValidator(private val context: Context, private val onValidationSuccess: (String?) -> Unit) {

    fun validateCode(code: String) {
        val apiService = ApiServiceUtil.getApiService(context)
        val call = apiService.validateCode(CodeRequest(code))
        call.enqueue(object : Callback<ValidationResponse> {
            override fun onResponse(call: Call<ValidationResponse>, response: Response<ValidationResponse>) {
                if (response.isSuccessful) {
                    val validationResponse = response.body()
                    if (validationResponse?.valid == true) {
                        onValidationSuccess(code)
                    } else {
                        Log.e("API_RESPONSE", "Invalid code: ${response.body()}")
                        showInvalidCodeNotification()
                    }
                } else {
                    Log.e("API_RESPONSE", "Response not successful: ${response.code()} - ${response.message()}")
                    showInvalidCodeNotification()
                }
            }

            override fun onFailure(call: Call<ValidationResponse>, t: Throwable) {
                Log.e("API_ERROR", "API call failed: ${t.message}", t)
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showInvalidCodeNotification() {
        Toast.makeText(context, "Invalid code", Toast.LENGTH_SHORT).show()
    }
}
