package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API

class InitActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isInternetAvailable()) {
            Log.d("InitActivity", "API initialized, moving to SplashActivity")
            Log.d("InitActivity", "Initializing API...")

            val api = API.getInsatce("https://testwallet.sendy.land")
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("InitActivity", "No internet connection, moving to NoInternetActivity")
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
