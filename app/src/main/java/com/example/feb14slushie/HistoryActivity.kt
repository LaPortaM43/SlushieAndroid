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