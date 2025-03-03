package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vicmikhailau.maskededittext.MaskedEditText
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback

class SmsCodeActivity : AppCompatActivity() {

    private lateinit var phone: String
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_code)

        phone = intent.getStringExtra("phone") ?: ""
        confirmButton = findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val smsCode = findViewById<MaskedEditText>(R.id.smsEditText).text.toString()
            when {
                !validateSmsCode(smsCode) -> {
                    showToast("Введите 6-значный код")
                    confirmButton.isEnabled = true
                }
                !isInternetAvailable() -> {
                    startActivity(Intent(this, NoInternetActivity::class.java))
                    confirmButton.isEnabled = true
                }
                else -> {
                    confirmButton.isEnabled = false
                    sendSmsCode(smsCode)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        confirmButton.isEnabled = true
    }

    private fun validateSmsCode(code: String) = code.matches(Regex("^\\d{6}$"))

    private fun sendSmsCode(smsCode: String) {
        API.getInstance().activateWlletWS(this, smsCode, "sms", object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                if (!res || getErrNo() != 0) {
                    API.outLog("Ошибка валидации SMS: ${this.toString()}")
                    showToast("Неверный код")
                    confirmButton.isEnabled = true
                } else {
                    API.outLog("Код подтверждён, вход выполнен!")
                    showToast("Вход выполнен успешно!")
                    // startActivity(Intent(this@SmsCodeActivity, <СледующееАктивити>::class.java))
                    // finish()
                }
            }
        })?.takeIf { it.hasError() }?.let {
            API.outLog("runResult запрос не был запущен:\r\n$it")
            confirmButton.isEnabled = true
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
}
