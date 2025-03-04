package org.example.project

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.vicmikhailau.maskededittext.MaskedEditText
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback

class LoginActivity : AppCompatActivity() {

    private lateinit var continueButton: Button
    private lateinit var phoneInput: MaskedEditText
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneInput = findViewById(R.id.phoneEditText)
        val agreementCheckBox = findViewById<CheckBox>(R.id.agreementCheckBox)
        continueButton = findViewById(R.id.continueButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        phoneInput.setText(getSavedPhoneNumber())

        continueButton.setOnClickListener {
            val phone = "7" + phoneInput.unMaskedText.toString()

            Log.d("LoginActivity", "Введён номер: $phone, согласие: ${agreementCheckBox.isChecked}")

            when {
                !validatePhoneNumber(phone) -> showToast("Неверный номер телефона")
                !agreementCheckBox.isChecked -> showToast("Вы должны согласиться с офертой")
                !isInternetAvailable() -> showSnackbar("Нет соединения с интернетом")
                else -> {
                    savePhoneNumber(phone)
                    continueButton.isEnabled = false
                    loadingIndicator.visibility = View.VISIBLE
                    sendRegistrationRequest(phone)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        continueButton.isEnabled = true
        loadingIndicator.visibility = View.GONE
    }

    private fun validatePhoneNumber(phone: String) = phone.matches(Regex("^7\\d{10}$"))

    private fun sendRegistrationRequest(phone: String) {
        API.getInstance().loginAtAuthWS(this, phone, object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                loadingIndicator.visibility = View.GONE
                if (!res || getErrNo() != 0) {
                    showSnackbar("Ошибка при регистрации")
                    continueButton.isEnabled = true
                } else {
                    startActivity(Intent(this@LoginActivity, SmsCodeActivity::class.java).apply {
                        putExtra("phone", phone)
                    })
                }
            }
        })?.takeIf { it.hasError() }?.let {
            showSnackbar("Ошибка запроса")
            continueButton.isEnabled = true
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

    private fun savePhoneNumber(phone: String) {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("saved_phone", phone)
            .apply()
    }

    private fun getSavedPhoneNumber(): String {
        return getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("saved_phone", "") ?: ""
    }
}
