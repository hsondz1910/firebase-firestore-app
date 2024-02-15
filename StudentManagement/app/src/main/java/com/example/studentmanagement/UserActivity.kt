package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.databinding.ActivityProfileUserBinding

class UserActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")

        binding.btnLogOut.setOnClickListener{
            logoutHandler()
        }

        binding.btnViewProfile.setOnClickListener {
            changeProfileHandler(userId)
        }
    }

    private fun changeProfileHandler(userId: String?) {
        val editProfileIntent = Intent(this@UserActivity, EditProfileActivity::class.java);
        editProfileIntent.putExtra("userId", userId)
        startActivity(editProfileIntent)
    }

    private fun logoutHandler() {
        val loginIntent = Intent(this@UserActivity, LoginActivity::class.java);
        startActivity(loginIntent)
        finish()
    }
}