package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback
import land.sendy.pfe_sdk.model.pfe.response.AuthActivateRs

class SmsCodeActivity : AppCompatActivity() {

    private lateinit var smsCodeInput: EditText
    private lateinit var continueButton: Button
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_code)

        phone = intent.getStringExtra("phone") ?: ""

        smsCodeInput = findViewById(R.id.smsCodeEditText)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            val smsCode = smsCodeInput.text.toString()
            if (validateSmsCode(smsCode)) {
                if (isInternetAvailable()) {
                    sendSmsCode(smsCode)
                } else {
                    val intent = Intent(this, NoInternetActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Введите 6-значный код", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateSmsCode(code: String): Boolean {
        return code.matches(Regex("^\\d{6}$"))
    }

    private fun sendSmsCode(smsCode: String) {
        val api = API.getInstance()

        val runResult = api.activateWlletWS(this, smsCode, "sms", object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                if (!res || getErrNo() != 0) {
                    API.outLog("Ошибка валидации SMS: ${this.toString()}")
                    Toast.makeText(this@SmsCodeActivity, "Неверный код", Toast.LENGTH_SHORT).show()
                } else {
                    val response = this.oResponse as? AuthActivateRs
                    if (response != null) {
                        API.outLog("Код подтверждён, вход выполнен!")
                        Toast.makeText(this@SmsCodeActivity, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        if (runResult != null && runResult.hasError()) {
            API.outLog("runResult запрос не был запущен:\r\n" + runResult.toString())
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
