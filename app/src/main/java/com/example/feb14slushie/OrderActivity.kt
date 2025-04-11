package com.example.feb14slushie

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class OrderActivity : AppCompatActivity() {

    private val branchLocations = mapOf(
        "1251 Deal Road" to Pair(40.24764, -74.07347),
        // Prucha Place, Cary, NC 27523
        "6 Larchwood Ave" to Pair(35.782980, -78.919952)
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var branchSpinner: Spinner
    private lateinit var orderButton: Button
    private lateinit var flavorsContainer: LinearLayout
    private val selectedFlavors = mutableListOf<String>()
    private val flavorCheckBoxes = mutableListOf<CheckBox>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

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

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        branchSpinner = findViewById(R.id.branchSpinner)
        orderButton = findViewById(R.id.orderButton)
        flavorsContainer = findViewById(R.id.flavorsContainer)

        setupSpinner(branchSpinner, R.array.branch_array)
        getUserLocation()

        branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        flavorCheckBoxes.addAll(
            listOf(
                findViewById(R.id.vanillaCheckBox),
                findViewById(R.id.chocolateCheckBox),
                findViewById(R.id.strawberryCheckBox),
                findViewById(R.id.blueberryCheckBox),
                findViewById(R.id.raspberryCheckBox)
            )
        )

        setupFlavorCheckboxes()

        orderButton.setOnClickListener {
            placeOrder()
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
                            this@OrderActivity,
                            "Maximum 3 flavors allowed",
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
        val branch = branchSpinner.selectedItem as? String

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

        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("customers").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val customerName = document.getString("customerName") ?: "Customer"
                        val customerEmail = document.getString("customerEmail") ?: "No Email"
                        val deliveryAddress =
                            document.getString("customerAddress") ?: "Not specified"

                        createOrder(
                            branch = branch,
                            flavorMap = flavorMap,
                            customerId = currentUser.uid,
                            customerEmail = customerEmail,
                            customerName = customerName,
                            deliveryAddress = deliveryAddress
                        )
                    } else {
                        createOrder(
                            branch = branch,
                            flavorMap = flavorMap,
                            customerId = currentUser.uid,
                            customerEmail = currentUser.email ?: "No Email",
                            customerName = "Customer",
                            deliveryAddress = "Not specified"
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
                    createOrder(
                        branch = branch,
                        flavorMap = flavorMap,
                        customerId = currentUser.uid,
                        customerEmail = currentUser.email ?: "No Email",
                        customerName = "Customer",
                        deliveryAddress = "Not specified"
                    )
                }
        } else {
            createOrder(
                branch = branch,
                flavorMap = flavorMap,
                customerId = "guest",
                customerEmail = "guest@example.com",
                customerName = "Guest",
                deliveryAddress = "Not specified"
            )
        }
    }

    private fun createOrder(
        branch: String,
        flavorMap: Map<String, String>,
        customerId: String,
        customerEmail: String,
        customerName: String,
        deliveryAddress: String
    ) {
        val branchId = when (branch) {
            "1251 Deal Road" -> "b1"
            "6 Larchwood Ave" -> "b2"
            else -> "unknown"
        }

        val order = hashMapOf<String, Any>(
            "price" to 10,
            "branchID" to branchId,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp(),
            "customerID" to customerId,
            "customerEmail" to customerEmail,
            "customerName" to customerName,
            "deliveryAddress" to deliveryAddress
        )

        if (selectedFlavors.size > 0) {
            order["flavor1ID"] = flavorMap[selectedFlavors[0]] ?: ""
        }
        if (selectedFlavors.size > 1) {
            order["flavor2ID"] = flavorMap[selectedFlavors[1]] ?: ""
        }
        if (selectedFlavors.size > 2) {
            order["flavor3ID"] = flavorMap[selectedFlavors[2]] ?: ""
        }

        db.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("orderId", documentReference.id)
                    putExtra("customerEmail", customerEmail)
                    putExtra("customerName", customerName)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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