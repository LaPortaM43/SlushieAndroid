package com.example.feb14slushie

// HomepageActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class HomepageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        welcomeTextView = findViewById(R.id.welcomeTextView)
        logoutButton = findViewById(R.id.LogoutButton)
        backButton = findViewById(R.id.backButton)

        updateUI()

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.startOrderButton).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                val orderIntent = if (auth.currentUser != null) {
                    Intent(this, OrderActivity::class.java)
                } else {
                    Intent(this, GuestOrderActivity::class.java)
                }
                startActivity(orderIntent)
            }
        }

        findViewById<Button>(R.id.backButton).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100)
            }
            startActivity(Intent(this, MainActivity::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomepageActivity::class.java))
                    true
                }
                R.id.navigation_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                R.id.navigation_history -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this, HistoryActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please log in to view order history", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {

            logoutButton.visibility = Button.VISIBLE
            backButton.visibility = Button.GONE

            db.collection("customers").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("customerName") ?: ""
                        welcomeTextView.text = "Welcome, $name!"
                    }
                }
        } else {
            logoutButton.visibility = Button.GONE
            backButton.visibility = Button.VISIBLE
            welcomeTextView.text = "Welcome!"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}