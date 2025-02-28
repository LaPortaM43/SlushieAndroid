package com.example.feb14slushie

// ThanksActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)


        // Initialize BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Set item selection listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Home button click
                    val homeIntent = Intent(this, MainActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                else -> false
            }
            when (item.itemId) {
                R.id.navigation_menu -> {
                    // Menu button click
                    val menuIntent = Intent(this, MenuActivity::class.java)
                    startActivity(menuIntent)
                    true
                }
                else -> false
            }
            when (item.itemId) {
                R.id.navigation_order -> {
                    // Menu button click
                    val orderIntent = Intent(this, OrderActivity::class.java)
                    startActivity(orderIntent)
                    true
                }
                else -> false
            }
            when (item.itemId) {
                R.id.navigation_history-> {
                    // Menu button click
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    startActivity(historyIntent)
                    true
                }
                else -> false
            }
        }
    }
}