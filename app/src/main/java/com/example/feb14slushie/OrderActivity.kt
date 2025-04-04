
// OrderActivity.kt

package com.example.feb14slushie

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class OrderActivity : AppCompatActivity() {
    private val branchLocations = mapOf(
        "1251 Deal Road" to Pair(40.24764, -74.07347), // Replace with actual coordinates
        "6 Larchwood Ave" to Pair(40.28629, -74.01211)  // Replace with actual coordinates
    )

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var br_spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Branch Spinner
        br_spinner = findViewById(R.id.branchSpinner)
        val br_adapter = ArrayAdapter.createFromResource(
            this, R.array.branch_array, android.R.layout.simple_spinner_item)

        br_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        br_spinner.adapter = br_adapter

        // Request location and recommend nearest branch
        getUserLocation()


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
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
            return
        }

        // Fetch last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val closestBranch = findClosestBranch(it.latitude, it.longitude)
                recommendBranch(closestBranch)
            }
        }
    }

    // Find the closest branch to the user's location
    private fun findClosestBranch(userLat: Double, userLon: Double): String {
        var closestBranch = "Select Option"
        var minDistance = Double.MAX_VALUE

        for ((branch, coordinates) in branchLocations) {
            val distance = calculateDistance(userLat, userLon, coordinates.first, coordinates.second)
            if (distance < minDistance) {
                minDistance = distance
                closestBranch = branch
            }
        }

        return closestBranch
    }

    private fun recommendBranch(branchName: String) {
        val adapter = br_spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(branchName)
        if (position != -1) {
            br_spinner.setSelection(position)
            Toast.makeText(this, "Closest Branch: $branchName", Toast.LENGTH_LONG).show()
        }
    }

    // Calculate distance between two geographical points
    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble() // Distance in meters
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        }
    }
}
