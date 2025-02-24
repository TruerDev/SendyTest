package org.example.project

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d(TAG, "Splash screen started")

        val logo = findViewById<ImageView>(R.id.logoImageView)
        logo.setImageResource(R.drawable.logo)

        Handler().postDelayed({
            Log.d(TAG, "Splash screen finished, moving to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }
}
