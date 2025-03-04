package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NoInternetActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        val retryButton: Button = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            if (isInternetAvailable()) {
                showToast("Интернет восстановлен!")
                startActivity(Intent(this, InitActivity::class.java))
                finish()
            } else {
                showToast("Попытка не удалась. Проверьте подключение.")
            }
        }

        checkInternetConnection()
    }

    private fun checkInternetConnection() {
        handler.postDelayed({
            if (isInternetAvailable()) {
                showToast("Интернет восстановлен!")
                startActivity(Intent(this, InitActivity::class.java))
                finish()
            } else {
                checkInternetConnection()
            }
        }, checkInterval)
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
