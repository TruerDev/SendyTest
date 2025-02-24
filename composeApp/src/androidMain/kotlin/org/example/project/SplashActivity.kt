package org.example.project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d("SplashActivity", "Splash screen started")

        findViewById<ImageView>(R.id.logoImageView).setImageResource(R.drawable.logo)

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SplashActivity", "Moving to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}
