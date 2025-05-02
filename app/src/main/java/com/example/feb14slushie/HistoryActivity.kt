package com.example.feb14slushie

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: View
    private lateinit var loginPromptTextView: TextView
    private lateinit var noOrdersTextView: TextView
    private lateinit var ordersContainer: LinearLayout

    private val flavorMap = mapOf(
        "f1av1" to "Vanilla",
        "f1av2" to "Chocolate",
        "f1av3" to "Strawberry",
        "f1av4" to "Blueberry",
        "f1av5" to "Raspberry"
    )

    private val branchMap = mapOf(
        "b1" to "1251 Deal Road",
        "b2" to "6 Larchwood Ave"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        progressBar = findViewById(R.id.progressBar)
        loginPromptTextView = findViewById(R.id.loginPromptTextView)
        noOrdersTextView = findViewById(R.id.noOrdersTextView)
        ordersContainer = findViewById(R.id.ordersContainer)

        if (auth.currentUser == null) {
            showLoginPrompt()
        } else {
            fetchOrders()
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

    private fun showLoginPrompt() {
        progressBar.visibility = View.GONE
        loginPromptTextView.visibility = View.VISIBLE
        noOrdersTextView.visibility = View.GONE
        ordersContainer.visibility = View.GONE
    }

    private fun fetchOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showLoginPrompt()
            return
        }

        progressBar.visibility = View.VISIBLE
        loginPromptTextView.visibility = View.GONE
        noOrdersTextView.visibility = View.GONE
        ordersContainer.visibility = View.GONE

        db.collection("orders")
            .whereEqualTo("customerID", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                progressBar.visibility = View.GONE

                if (querySnapshot.isEmpty) {
                    noOrdersTextView.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                ordersContainer.visibility = View.VISIBLE
                ordersContainer.removeAllViews()

                for (document in querySnapshot.documents) {
                    val order = document.data
                    order?.let {
                        addOrderToView(document.id, it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Error fetching orders: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addOrderToView(orderId: String, orderData: Map<String, Any>) {
        val orderView = layoutInflater.inflate(R.layout.item_order, ordersContainer, false)

        val orderIdTextView = orderView.findViewById<TextView>(R.id.orderIdTextView)
        val branchTextView = orderView.findViewById<TextView>(R.id.branchTextView)
        val flavorsTextView = orderView.findViewById<TextView>(R.id.flavorsTextView)
        val dateTextView = orderView.findViewById<TextView>(R.id.dateTextView)
        val statusTextView = orderView.findViewById<TextView>(R.id.statusTextView)

        orderIdTextView.text = "Order ID: ${orderId.take(8)}..."

        val branchId = orderData["branchID"] as? String
        branchTextView.text = "Branch: ${branchMap[branchId] ?: "Unknown"}"

        val flavors = listOf(
            orderData["flavor1ID"] as? String,
            orderData["flavor2ID"] as? String,
            orderData["flavor3ID"] as? String
        ).filterNotNull().map { flavorMap[it] ?: "Unknown" }

        flavorsTextView.text = "Flavors: ${flavors.joinToString(", ")}"

        val timestamp = orderData["timestamp"] as? com.google.firebase.Timestamp
        dateTextView.text = timestamp?.toDate()?.toString()?.substring(0, 16) ?: "Date unknown"

        val status = orderData["status"] as? String ?: "unknown"
        statusTextView.text = "Status: ${status.capitalize()}"

        ordersContainer.addView(orderView)
    }
}