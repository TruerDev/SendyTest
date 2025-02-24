package org.example.project

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback
import com.vicmikhailau.maskededittext.MaskedEditText
import land.sendy.pfe_sdk.model.pfe.response.AuthLoginRs


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val phoneInput = findViewById<MaskedEditText>(R.id.phoneEditText)
        val agreementCheckBox = findViewById<CheckBox>(R.id.agreementCheckBox)
        val continueButton = findViewById<Button>(R.id.continueButton)



        continueButton.setOnClickListener {
            val phone = "7" + phoneInput.unMaskedText.toString()
            Log.d(TAG, "проверка")
            Log.d(TAG, phone)
            val isAgreementChecked = agreementCheckBox.isChecked
            Log.d(TAG, isAgreementChecked.toString())

            if (validatePhoneNumber(phone)) {
                if (isAgreementChecked) {
                    sendRegistrationRequest(phone)
                } else {
                    Toast.makeText(this, "Вы должны согласиться с офертой", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Неверный номер телефона", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePhoneNumber(phone: String): Boolean {
        val regex = "^7\\d{10}$".toRegex()
        return phone.matches(regex)
    }

    private fun sendRegistrationRequest(phone: String) {
        val api = API.getInstance()

        val runResult = api.loginAtAuthWS(this, phone, object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                if (!res || getErrNo() != 0) {
                    API.outLog("Ошибка: ${this.toString()}")
                    Toast.makeText(this@LoginActivity, "Ошибка при регистрации", Toast.LENGTH_SHORT).show()
                } else {
                    val response = this.oResponse as? AuthLoginRs
                    if (response != null && response.TwoFactor == true) {
                        val intent = Intent(this@LoginActivity, SmsCodeActivity::class.java)
                        intent.putExtra("phone", phone)
                        startActivity(intent)
                    } else {
                        // Обычная аутентификация (код из SMS)
                        val intent = Intent(this@LoginActivity, SmsCodeActivity::class.java)
                        intent.putExtra("phone", phone)
                        startActivity(intent)
                    }
                }
            }
        })

        if (runResult != null && runResult.hasError()) {
            API.outLog("runResult запрос не был запущен:\r\n" + runResult.toString())
        }
    }
}
