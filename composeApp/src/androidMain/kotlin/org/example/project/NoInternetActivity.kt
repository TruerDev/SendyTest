package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NoInternetActivity : AppCompatActivity() {

    private val handler = Handler()
    private val checkInterval: Long = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        checkInternetConnection()
    }

    private fun checkInternetConnection() {
        handler.postDelayed({
            if (isInternetAvailable()) {
                Toast.makeText(this, "Интернет восстановлен!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, InitActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                checkInternetConnection()
            }
        }, checkInterval)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
