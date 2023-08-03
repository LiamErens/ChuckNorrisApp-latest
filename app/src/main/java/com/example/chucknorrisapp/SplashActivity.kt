package com.example.chucknorrisapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Add any initialization code or background tasks if needed

        // Delay or perform background tasks here if necessary before proceeding to the main activity
        // For example, you might want to show the splash screen for a few seconds before launching the main activity
        // You can use a Handler with a delayed task to accomplish this.

        Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity after the desired delay
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Remove the following line to prevent the app from exiting after the splash screen
            // finish()
        }, 2000)
    }
}