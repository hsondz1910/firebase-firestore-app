package com.example.studentmanagement

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.Model.User
import com.example.studentmanagement.databinding.ActivityHistoryLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class HistoryLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryLoginBinding
    private lateinit var listViewLoginHistory: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userID = intent.getStringExtra("userID")
        val userName = intent.getStringExtra("userName")

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = "History Login Of $userName"

        listViewLoginHistory = binding.listViewLoginHistory


        if (userID != null) {
            retrieveLoginHistory(userID)
        }
    }

    private fun retrieveLoginHistory(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    val loginHistoryList = user?.loginHistory ?: emptyList<String>()

                    val adapter =
                        ArrayAdapter(this@HistoryLoginActivity, android.R.layout.simple_list_item_1, loginHistoryList)
                    listViewLoginHistory.adapter = adapter
                }
            }
    }
}