package org.example.project

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API

class InitActivity : MasterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nextActivity = if (isInternetAvailable()) {
            try {
                Log.d("InitActivity", "Initializing API...")
                API.getInsatce("https://testwallet.sendy.land")
                Log.d("InitActivity", "API initialized, moving to SplashActivity")
                SplashActivity::class.java
            } catch (e: Exception) {
                Log.e("InitActivity", "API initialization error: ${e.message}")
                NoInternetActivity::class.java
            }
        } else {
            Log.d("InitActivity", "No internet connection, moving to NoInternetActivity")
            NoInternetActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
