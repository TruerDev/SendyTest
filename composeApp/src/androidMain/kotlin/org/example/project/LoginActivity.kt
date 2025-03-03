package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vicmikhailau.maskededittext.MaskedEditText
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback

class LoginActivity : AppCompatActivity() {

    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val phoneInput = findViewById<MaskedEditText>(R.id.phoneEditText)
        val agreementCheckBox = findViewById<CheckBox>(R.id.agreementCheckBox)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            val phone = "7" + phoneInput.unMaskedText.toString()
            Log.d("LoginActivity", "Введён номер: $phone, согласие: ${agreementCheckBox.isChecked}")

            when {
                !validatePhoneNumber(phone) -> showToast("Неверный номер телефона")
                !agreementCheckBox.isChecked -> showToast("Вы должны согласиться с офертой")
                !isInternetAvailable() -> startActivity(Intent(this, NoInternetActivity::class.java))
                else -> {
                    continueButton.isEnabled = false
                    sendRegistrationRequest(phone)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        continueButton.isEnabled = true
    }

    private fun validatePhoneNumber(phone: String) = phone.matches(Regex("^7\\d{10}$"))

    private fun sendRegistrationRequest(phone: String) {
        API.getInstance().loginAtAuthWS(this, phone, object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                if (!res || getErrNo() != 0) {
                    API.outLog("Ошибка регистрации: ${this.toString()}")
                    showToast("Ошибка при регистрации")
                    continueButton.isEnabled = true
                } else {
                    startActivity(Intent(this@LoginActivity, SmsCodeActivity::class.java).apply {
                        putExtra("phone", phone)
                    })
                }
            }
        })?.takeIf { it.hasError() }?.let {
            API.outLog("runResult запрос не был запущен:\r\n$it")
            continueButton.isEnabled = true
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
