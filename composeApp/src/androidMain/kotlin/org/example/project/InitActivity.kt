package org.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API

class InitActivity : MasterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("InitActivity", "Initializing API...")

        val api = API.getInsatce("https://testwallet.sendy.land")

        Log.d("InitActivity", "API initialized, moving to SplashActivity")

        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }
}
