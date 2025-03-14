package org.example.project

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback

class SmsCodeActivity : AppCompatActivity() {

    private lateinit var phone: String
    private lateinit var confirmButton: Button
    private lateinit var smsInput: EditText
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_code)

        phone = intent.getStringExtra("phone") ?: ""
        confirmButton = findViewById(R.id.confirmButton)
        smsInput = findViewById(R.id.smsEditText)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        smsInput.setText(getSavedSmsCode())

        confirmButton.setOnClickListener {
            val smsCode = smsInput.text.toString()
            when {
                !validateSmsCode(smsCode) -> showToast("Введите 6-значный код")
                !isInternetAvailable() -> showSnackbar("Нет соединения с интернетом")
                else -> {
                    saveSmsCode(smsCode)
                    confirmButton.isEnabled = false
                    loadingIndicator.visibility = View.VISIBLE
                    sendSmsCode(smsCode)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        confirmButton.isEnabled = true
        loadingIndicator.visibility = View.GONE
    }

    private fun validateSmsCode(code: String) = code.matches(Regex("^\\d{6}$"))

    private fun sendSmsCode(smsCode: String) {
        API.getInstance().activateWlletWS(this, smsCode, "sms", object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                loadingIndicator.visibility = View.GONE
                if (!res || getErrNo() != 0) {
                    showToast("Неверный код")
                    confirmButton.isEnabled = true
                } else {
                    showToast("Вход выполнен успешно!")
                    // startActivity(Intent(this@SmsCodeActivity, <СледующееАктивити>::class.java))
                    // finish()
                }
            }
        })?.takeIf { it.hasError() }?.let {
            showToast("Ошибка запроса")
            confirmButton.isEnabled = true
            loadingIndicator.visibility = View.GONE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun saveSmsCode(code: String) {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("saved_sms_code", code)
            .apply()
    }

    private fun getSavedSmsCode(): String {
        return getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("saved_sms_code", "") ?: ""
    }
}
