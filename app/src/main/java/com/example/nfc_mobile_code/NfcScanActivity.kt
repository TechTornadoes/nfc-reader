package com.example.nfc_mobile_code

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nfc_mobile_code.retrofit.model.JwtResponse
import com.example.nfc_mobile_code.retrofit.model.JwtResponseNoCode
import com.example.nfc_mobile_code.retrofit.model.TokenRequest
import com.example.nfc_mobile_code.retrofit.model.TokenRequestInstant
import com.example.nfc_mobile_code.retrofit.model.SessionCloseRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.nfc_mobile_code.util.ApiServiceUtil

class NfcScanActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcDataText: TextView
    private var code: String? = null
    private var attemptCount = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scan)

        // Initialisation de l'adaptateur NFC et du TextView
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcDataText = findViewById(R.id.nfc_data_text)

        // Vérifie si l'appareil supporte NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_SHORT).show()
            finish()
        } else if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_SHORT).show()
            finish()
        }
        // Traite l'intent reçu
        handleIntent(intent)

        // Récupérer le code transmis depuis l'intent
        code = intent.getStringExtra("EXTRA_CODE")
        if (code != null) {
            nfcDataText.text = "Code: $code\nScan your NFC"
        }
    }

    override fun onResume() {
        super.onResume()
        // Active le mode lecteur NFC
        nfcAdapter.enableReaderMode(this, { tag ->
            handleTag(tag)
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null)
        Log.d("NfcScanActivity", "Reader mode enabled")
    }

    override fun onPause() {
        super.onPause()
        // Désactive le mode lecteur NFC
        nfcAdapter.disableReaderMode(this)
        Log.d("NfcScanActivity", "Reader mode disabled")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Gère un nouvel intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        // Vérifie si l'action de l'intent est liée à la découverte d'un tag NFC
        if (action != null && (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
                    NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
                    NfcAdapter.ACTION_TECH_DISCOVERED == action)) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                // Gère le tag NFC
                handleTag(tag)
            }
        }
    }

    private fun handleTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                // Lit les données du tag NFC
                val jwtToken = readNfcTag(ndef)
                Log.d("NfcScanActivity", "Data read from NFC: $jwtToken")
                // Envoie le jeton à l'API avec le code ou envoie directement sans code
                if (code != null) {
                    sendTokenToApi(code!!, jwtToken)
                } else {
                    sendTokenWithoutCode(jwtToken)
                }
            } catch (e: Exception) {
                Log.e("NfcScanActivity", "Error reading NFC tag", e)
                runOnUiThread {
                    Toast.makeText(this, "Error reading NFC tag", Toast.LENGTH_SHORT).show()
                }
            } finally {
                ndef.close()
            }
        } else {
            Log.d("NfcScanActivity", "NFC tag is not NDEF compatible")
            runOnUiThread {
                Toast.makeText(this, "NFC tag is not NDEF compatible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readNfcTag(ndef: Ndef): String {
        ndef.connect()
        val ndefMessage: NdefMessage? = ndef.ndefMessage
        val stringBuilder = StringBuilder()
        if (ndefMessage != null) {
            val ndefRecords = ndefMessage.records
            for (record in ndefRecords) {
                val payload = record.payload
                // Skip les 3 premiers octets de l'enregistrement (contiennent des métadonnées)
                val data = String(payload.copyOfRange(3, payload.size))
                stringBuilder.append(data).append("\n")
            }
        } else {
            stringBuilder.append("No NDEF messages found")
        }
        ndef.close()
        return stringBuilder.toString().trim()
    }

    private fun sendTokenToApi(code: String, token: String) {
        val apiService = ApiServiceUtil.getApiService(this)
        val tokenRequest = TokenRequest(code, token)
        val call = apiService.sendToken(tokenRequest)
        call.enqueue(object : Callback<JwtResponse> {
            override fun onFailure(call: Call<JwtResponse>, t: Throwable) {
                Log.e("NfcScanActivity", "API call failed", t)
                runOnUiThread {
                    showError()
                }
            }

            override fun onResponse(call: Call<JwtResponse>, response: Response<JwtResponse>) {
                if (response.isSuccessful) {
                    val jwtResponse = response.body()
                    if (jwtResponse?.authenticated == true) {
                        showSuccessDialog()
                    } else {
                        Log.e("NfcScanActivity", "API call unsuccessful: ${response.message()}")
                        runOnUiThread {
                            showError()
                        }
                    }
                } else {
                    Log.e("NfcScanActivity", "API call unsuccessful: ${response.message()}")
                    runOnUiThread {
                        showError()
                    }
                }
            }
        })
    }

    private fun sendTokenWithoutCode(token: String) {
        val apiService = ApiServiceUtil.getApiService(this)
        val tokenRequestInstant = TokenRequestInstant(token)
        val call = apiService.validateSansCode(tokenRequestInstant)
        call.enqueue(object : Callback<JwtResponseNoCode> {
            override fun onFailure(call: Call<JwtResponseNoCode>, t: Throwable) {
                Log.e("NfcScanActivity", "API call failed", t)
                runOnUiThread {
                    showError()
                }
            }

            override fun onResponse(call: Call<JwtResponseNoCode>, response: Response<JwtResponseNoCode>) {
                if (response.isSuccessful) {
                    val jwtResponse = response.body()
                    if (jwtResponse?.authenticated == true) {
                        runOnUiThread {
                            redirectToWebsite(jwtResponse.token)
                        }
                    } else {
                        Log.e("NfcScanActivity", "API call unsuccessful: ${response.message()}")
                        runOnUiThread {
                            showError()
                        }
                    }
                } else {
                    Log.e("NfcScanActivity", "API call unsuccessful: ${response.message()}")
                    runOnUiThread {
                        showError()
                    }
                }
            }
        })
    }

    private fun redirectToWebsite(token: String) {
        val ip = getString(R.string.ip_resaux)
        val url = "http://$ip:3000/mobile-auth/$token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
        finish()
    }

    private fun showError() {
        attemptCount++
        if (attemptCount >= maxAttempts) {
            // Send request to close session
            closeSession()
        } else {
            // Show error message
            Toast.makeText(this, "Veuillez scanner un NFC valide. Tentative $attemptCount de $maxAttempts.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeSession() {
        val apiService = ApiServiceUtil.getApiService(this)

        if (code != null) {
            val sessionCloseRequest = SessionCloseRequest(code!!)
            val call = apiService.closeSession(sessionCloseRequest)
            call.enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NfcScanActivity", "API call failed to close session", t)
                    runOnUiThread {
                        returnToHome()
                    }
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("NfcScanActivity", "Session closed successfully")
                    } else {
                        Log.e("NfcScanActivity", "Failed to close session: ${response.message()}")
                    }
                    runOnUiThread {
                        returnToHome()
                    }
                }
            })
        } else {
            Log.e("NfcScanActivity", "No code found to close session")
            returnToHome()
        }
    }

    private fun returnToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showSuccessDialog() {
        // Affiche une boîte de dialogue de succès
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Token sent successfully!")
            .setPositiveButton("OK") { _, _ ->
                returnToHome()
            }
            .show()
    }
}
