package com.example.feb14slushie

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
    private lateinit var firstFlavorSpinner: Spinner
    private lateinit var secondFlavorSpinner: Spinner
    private lateinit var thirdFlavorSpinner: Spinner
    private lateinit var orderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_order)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        nameEdit = findViewById(R.id.nameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        addressEdit = findViewById(R.id.addressEdit)
        branchSpinner = findViewById(R.id.branchSpinner)
        firstFlavorSpinner = findViewById(R.id.firstFlavorSpinner)
        secondFlavorSpinner = findViewById(R.id.secondFlavorSpinner)
        thirdFlavorSpinner = findViewById(R.id.thirdFlavorSpinner)
        orderButton = findViewById(R.id.orderButton)

        // Set up branch spinner
        setupSpinner(branchSpinner, R.array.branch_array)

        // Set up flavor spinners
        setupSpinner(firstFlavorSpinner, R.array.flavors_array)
        setupSpinner(secondFlavorSpinner, R.array.flavors_array)
        setupSpinner(thirdFlavorSpinner, R.array.flavors_array)

        // Handle order button click
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

    private fun placeOrder() {
        // Collect input values
        val name = nameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val address = addressEdit.text.toString().trim()
        val branch = branchSpinner.selectedItem as? String
        val flavor1 = firstFlavorSpinner.selectedItem as? String
        val flavor2 = secondFlavorSpinner.selectedItem as? String
        val flavor3 = thirdFlavorSpinner.selectedItem as? String

        // Validate input
        if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (branch == "Select Option" || branch.isNullOrEmpty()) {
            Toast.makeText(this, "Please select a branch.", Toast.LENGTH_SHORT).show()
            return
        }

        if (flavor1 == "Select Option" && flavor2 == "Select Option" && flavor3 == "Select Option") {
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
            "flavor1ID" to flavorMap[flavor1],
            "flavor2ID" to flavorMap[flavor2],
            "flavor3ID" to flavorMap[flavor3],
            "status" to "pending",
            "timestamp" to Date(),
            "customerName" to name,
            "customerEmail" to email
        )

        // Submit order to Firestore
        db.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
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