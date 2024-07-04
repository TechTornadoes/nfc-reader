package com.example.nfc_mobile_code

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nfc_mobile_code.util.main.CodeValidator
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    private lateinit var codeInput: EditText
    private lateinit var validateButton: Button
    private lateinit var nfcDirectButton: Button
    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        codeInput = findViewById(R.id.codeInput)
        validateButton = findViewById(R.id.validateButton)
        nfcDirectButton = findViewById(R.id.nfcDirectButton)
        scanButton = findViewById(R.id.scanButton)

        validateButton.setOnClickListener {
            val code = codeInput.text.toString()
            if (code.isNotEmpty()) {
                CodeValidator(this) { validatedCode ->
                    goToNfcScanActivity(validatedCode)
                }.validateCode(code)
            } else {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show()
            }
        }

        nfcDirectButton.setOnClickListener {
            goToNfcScanActivity(null)
        }

        scanButton.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                codeInput.setText(result.contents)
                CodeValidator(this) { validatedCode ->
                    goToNfcScanActivity(validatedCode)
                }.validateCode(result.contents)
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun goToNfcScanActivity(code: String?) {
        val intent = Intent(this, NfcScanActivity::class.java).apply {
            putExtra("EXTRA_CODE", code)
        }
        startActivity(intent)
    }
}
