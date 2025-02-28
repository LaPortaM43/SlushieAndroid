package com.example.feb14slushie

// OrderActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class OrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        // Branch Spinner
        val br_spinner = findViewById<Spinner>(R.id.branchSpinner)
        val br_adapter = ArrayAdapter.createFromResource(
            this, R.array.branch_array, android.R.layout.simple_spinner_item)

        br_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        br_spinner.adapter = br_adapter

        br_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position) as? String
                if (selectedItem == "Select Option") {
                    // do nothing
                } else {
                    val item = parent?.getItemAtPosition(position) as String
                    Toast.makeText(this@OrderActivity, item, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Flavor1 Spinner
        val f1_spinner = findViewById<Spinner>(R.id.firstFlavorSpinner)
        val f1_adapter = ArrayAdapter.createFromResource(
            this, R.array.flavors_array, android.R.layout.simple_spinner_item)

        f1_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        f1_spinner.adapter = f1_adapter

        f1_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position) as? String
                if (selectedItem == "Select Option") {
                    // do nothing
                } else {
                    val item = parent?.getItemAtPosition(position) as String
                    Toast.makeText(this@OrderActivity, item, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Flavor2 Spinner
        val f2_spinner = findViewById<Spinner>(R.id.secondFlavorSpinner)
        val f2_adapter = ArrayAdapter.createFromResource(
            this, R.array.flavors_array, android.R.layout.simple_spinner_item)

        f2_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        f2_spinner.adapter = f2_adapter

        f2_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedItem = parent?.getItemAtPosition(position) as? String
                if (selectedItem == "Select Option") {
                    // do nothing
                } else {
                    val item = parent?.getItemAtPosition(position) as String
                    Toast.makeText(this@OrderActivity, item, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Flavor3 Spinner
        val f3_spinner = findViewById<Spinner>(R.id.thirdFlavorSpinner)
        val f3_adapter = ArrayAdapter.createFromResource(
            this, R.array.flavors_array, android.R.layout.simple_spinner_item)

        f3_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        f3_spinner.adapter = f2_adapter

        f3_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position) as? String
                if (selectedItem == "Select Option") {
                    // do nothing
                } else {
                    val item = parent?.getItemAtPosition(position) as String
                    Toast.makeText(this@OrderActivity, item, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Place Order Button Click
        findViewById<Button>(R.id.orderButton).setOnClickListener { v ->
            // Makes button slightly smaller when clicked
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100)
            }
            startActivity(Intent(this, PaymentActivity::class.java))
        }

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