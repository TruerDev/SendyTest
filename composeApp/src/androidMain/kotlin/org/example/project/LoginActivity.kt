package org.example.project

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.types.ApiCallback

class LoginActivity : AppCompatActivity() {

    private lateinit var continueButton: Button
    private lateinit var countryCodeSpinner: Spinner
    private lateinit var block1: EditText
    private lateinit var block2: EditText
    private lateinit var block3: EditText
    private lateinit var block4: EditText
    private lateinit var agreementCheckBox: CheckBox
    private lateinit var loadingIndicator: ProgressBar

    private val countryCodes = listOf("+7")
    private var selectedCountryCode = "+7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        countryCodeSpinner = findViewById(R.id.countryCodeSpinner)
        block1 = findViewById(R.id.block1)
        block2 = findViewById(R.id.block2)
        block3 = findViewById(R.id.block3)
        block4 = findViewById(R.id.block4)
        agreementCheckBox = findViewById(R.id.agreementCheckBox)
        continueButton = findViewById(R.id.continueButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        setupCountryCodeSpinner()
        setupPhoneInputBehavior()
        setupOfferLink()

        continueButton.setOnClickListener {
            val phone = selectedCountryCode.drop(1) + listOf(block1, block2, block3, block4).joinToString("") { it.text.toString() }

            Log.d("LoginActivity", "Введён номер: $phone, согласие: ${agreementCheckBox.isChecked}")

            when {
                !validatePhoneNumber(phone) -> showToast("Неверный номер телефона")
                !agreementCheckBox.isChecked -> showToast("Вы должны согласиться с офертой")
                !isInternetAvailable() -> showSnackbar("Нет соединения с интернетом")
                else -> {
                    continueButton.isEnabled = false
                    loadingIndicator.visibility = View.VISIBLE
                    sendRegistrationRequest(phone)
                }
            }
        }
    }

    private fun setupCountryCodeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)
        countryCodeSpinner.adapter = adapter
        countryCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCountryCode = countryCodes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onResume() {
        super.onResume()
        continueButton.isEnabled = true
        loadingIndicator.visibility = View.GONE
    }

    private fun setupPhoneInputBehavior() {
        val blocks = listOf(block1, block2, block3, block4)
        val blockSizes = listOf(3, 3, 2, 2)

        for ((index, block) in blocks.withIndex()) {
            val maxLength = blockSizes[index]
            block.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == maxLength && index < blocks.size - 1) {
                        blocks[index + 1].requestFocus()
                    } else if (s.isNullOrEmpty() && before > 0 && index > 0) {
                        blocks[index - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }


    private fun setupOfferLink() {
        val fullText = "Я соглашаюсь с условиями оферты"
        val spannable = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("https://example.com/offer.pdf")
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.BLUE
                ds.isUnderlineText = true
            }
        }

        val start = fullText.indexOf("оферты")
        val end = start + "оферты".length
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        agreementCheckBox.text = spannable
        agreementCheckBox.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(InputMethodManager::class.java)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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
}
