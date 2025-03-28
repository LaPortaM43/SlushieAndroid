package com.example.feb14slushie

// GuestOrderActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class GuestOrderActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var addressEdit: EditText
    private lateinit var branchSpinner: Spinner
    private lateinit var orderButton: Button
    private lateinit var flavorsContainer: LinearLayout
    private val selectedFlavors = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_order)

        db = FirebaseFirestore.getInstance()

        nameEdit = findViewById(R.id.nameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        addressEdit = findViewById(R.id.addressEdit)
        branchSpinner = findViewById(R.id.branchSpinner)
        orderButton = findViewById(R.id.orderButton)
        flavorsContainer = findViewById(R.id.flavorsContainer)

        setupSpinner(branchSpinner, R.array.branch_array)

        setupFlavorCheckboxes()

        orderButton.setOnClickListener {
            placeOrder()
        }

        // Initialize BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
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
        val flavors = resources.getStringArray(R.array.flavors_array)
            .filter { it != "Select Option" }

        flavors.forEach { flavorName ->
            val checkBox = CheckBox(this).apply {
                text = flavorName
                textSize = 16f
                setOnCheckedChangeListener { buttonView, isChecked ->
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

                    // Update all checkboxes' enabled state
                    updateCheckboxesEnabledState()
                }
            }
            flavorsContainer.addView(checkBox)
        }
    }

    private fun updateCheckboxesEnabledState() {

        for (i in 0 until flavorsContainer.childCount) {
            val checkBox = flavorsContainer.getChildAt(i) as CheckBox
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

        // Map flavors to IDs
        val flavorMap = mapOf(
            "Vanilla" to "f1av1",
            "Chocolate" to "f1av2",
            "Strawberry" to "f1av3",
            "Blueberry" to "f1av4",
            "Raspberry" to "f1av5"
        )

        // Prepare order data
        val order = hashMapOf(
            "customerID" to "guest",
            "price" to 10,
            "deliveryAddress" to address,
            "branchID" to branch,
            "flavor1ID" to if (selectedFlavors.size > 0) flavorMap[selectedFlavors[0]] else null,
            "flavor2ID" to if (selectedFlavors.size > 1) flavorMap[selectedFlavors[1]] else null,
            "flavor3ID" to if (selectedFlavors.size > 2) flavorMap[selectedFlavors[2]] else null,
            "status" to "pending",
            "timestamp" to Date(),
            "customerName" to name,
            "customerEmail" to email
        )

        // Submit order to Firestore
        db.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                // Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("orderId", documentReference.id)
                    putExtra("customerId", "guest")
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

