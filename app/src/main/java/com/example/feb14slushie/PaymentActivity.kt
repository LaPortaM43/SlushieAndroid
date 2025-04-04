package com.example.feb14slushie

// PaymentActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        findViewById<Button>(R.id.payButton).setOnClickListener { v ->
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100)
            }
            startActivity(Intent(this, ThanksActivity::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val homeIntent = Intent(this, HomepageActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                else -> false
            }
            when (item.itemId) {
                R.id.navigation_menu -> {
                    val menuIntent = Intent(this, MenuActivity::class.java)
                    startActivity(menuIntent)
                    true
                }
                else -> false
            }
            when (item.itemId) {
                R.id.navigation_history-> {
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    startActivity(historyIntent)
                    true
                }
                else -> false
            }
        }
    }
}