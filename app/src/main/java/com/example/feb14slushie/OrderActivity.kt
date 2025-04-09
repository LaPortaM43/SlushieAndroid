package com.example.feb14slushie

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var branchSpinner: Spinner
    private lateinit var orderButton: Button
    private lateinit var flavorsContainer: LinearLayout
    private val selectedFlavors = mutableListOf<String>()
    private val flavorCheckBoxes = mutableListOf<CheckBox>()

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

        branchSpinner = findViewById(R.id.branchSpinner)
        orderButton = findViewById(R.id.orderButton)
        flavorsContainer = findViewById(R.id.flavorsContainer)

        setupSpinner(branchSpinner, R.array.branch_array)

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
        // Map branch addresses to IDs
        val branchId = when (branch) {
            "123 Monmouth Street" -> "b1"
            "234 Monmouth Street" -> "b2"
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

        // Add flavor fields
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
}