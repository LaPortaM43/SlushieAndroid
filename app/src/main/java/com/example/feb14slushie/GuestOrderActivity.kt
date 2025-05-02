package com.example.feb14slushie

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class GuestOrderActivity : AppCompatActivity() {

    private val branchLocations = mapOf(
        "1251 Deal Road" to Pair(40.24764, -74.07347),
        "6 Larchwood Ave" to Pair(35.782980, -78.919952)
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var addressEdit: EditText
    private lateinit var branchSpinner: Spinner
    private lateinit var orderButton: Button
    private lateinit var flavorsContainer: LinearLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val selectedFlavors = mutableListOf<String>()
    private val flavorCheckBoxes = mutableListOf<CheckBox>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_order)

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        nameEdit = findViewById(R.id.nameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        addressEdit = findViewById(R.id.addressEdit)
        branchSpinner = findViewById(R.id.branchSpinner)
        orderButton = findViewById(R.id.orderButton)
        flavorsContainer = findViewById(R.id.flavorsContainer)

        setupSpinner(branchSpinner, R.array.branch_array)
        getUserLocation()

        flavorCheckBoxes.addAll(listOf(
            findViewById(R.id.vanillaCheckBox),
            findViewById(R.id.chocolateCheckBox),
            findViewById(R.id.strawberryCheckBox),
            findViewById(R.id.blueberryCheckBox),
            findViewById(R.id.raspberryCheckBox)
        ))

        setupFlavorCheckboxes()

        orderButton.setOnClickListener {
            placeOrder()
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
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayResource: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this, arrayResource, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupFlavorCheckboxes() {
        flavorCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val flavorName = buttonView.text.toString()
                if (isChecked) {
                    if (selectedFlavors.size >= 3) {
                        buttonView.isChecked = false
                        Toast.makeText(
                            this@GuestOrderActivity,
                            "Maximum of 3 flavors allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedFlavors.add(flavorName)
                    }
                } else {
                    selectedFlavors.remove(flavorName)
                }
                updateCheckboxesEnabledState()
            }
        }
    }

    private fun updateCheckboxesEnabledState() {
        flavorCheckBoxes.forEach { checkBox ->
            checkBox.isEnabled = selectedFlavors.size < 3 || checkBox.isChecked
        }
    }

    private fun placeOrder() {
        val name = nameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val address = addressEdit.text.toString().trim()
        val branch = branchSpinner.selectedItem as? String

        if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (branch == "Select Option" || branch.isNullOrEmpty()) {
            Toast.makeText(this, "Please select a branch.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedFlavors.isEmpty()) {
            Toast.makeText(this, "Please select at least one flavor.", Toast.LENGTH_SHORT).show()
            return
        }

        val flavorMap = mapOf(
            "Vanilla" to "f1av1",
            "Chocolate" to "f1av2",
            "Strawberry" to "f1av3",
            "Blueberry" to "f1av4",
            "Raspberry" to "f1av5"
        )

        val branchId = when (branch) {
            "1251 Deal Road" -> "b1"
            "6 Larchwood Ave" -> "b2"
            else -> "unknown"
        }

        val order = hashMapOf(
            "customerID" to "guest",
            "price" to 10,
            "deliveryAddress" to address,
            "branchID" to branchId,
            "flavor1ID" to if (selectedFlavors.size > 0) flavorMap[selectedFlavors[0]] else null,
            "flavor2ID" to if (selectedFlavors.size > 1) flavorMap[selectedFlavors[1]] else null,
            "flavor3ID" to if (selectedFlavors.size > 2) flavorMap[selectedFlavors[2]] else null,
            "status" to "pending",
            "timestamp" to Date(),
            "customerName" to name,
            "customerEmail" to email
        )

        db.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("orderId", documentReference.id)
                    putExtra("customerId", "guest")
                    putExtra("customerEmail", email)
                    putExtra("customerName", name)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Please enable location services to find the nearest branch")
                .setPositiveButton("Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val closestBranch = findClosestBranch(it.latitude, it.longitude)
                recommendBranch(closestBranch)
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

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
        val adapter = branchSpinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(branchName)
        if (position != -1) {
            branchSpinner.setSelection(position)
            Toast.makeText(this, "Closest Branch: $branchName", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        }
    }
}