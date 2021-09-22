package br.com.antonio.distribuidoradoguila.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.antonio.distribuidoradoguila.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        Thread.sleep(2000)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }
}