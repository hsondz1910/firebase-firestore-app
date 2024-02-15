package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.databinding.ActivityManagementBinding

class ManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")


        binding.btnLogOut.setOnClickListener {
            logoutHandler()
        }

        binding.btnViewProfile.setOnClickListener{
            changeProfileHandler(userId)
        }

        binding.btnStudentManage.setOnClickListener{
            val studentIntent = Intent(this@ManagementActivity, StudentActivity::class.java);
            startActivity(studentIntent)
        }


    }

    private fun changeProfileHandler(userId: String?) {
        val editProfileIntent = Intent(this@ManagementActivity, EditProfileActivity::class.java);
        editProfileIntent.putExtra("userId", userId)
        startActivity(editProfileIntent)
    }

    private fun logoutHandler() {
        val loginIntent = Intent(this@ManagementActivity, LoginActivity::class.java);
        startActivity(loginIntent)
        finish()
    }
}