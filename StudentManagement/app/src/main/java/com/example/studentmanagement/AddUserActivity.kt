package com.example.studentmanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.studentmanagement.FireStoreService.UserManagementFireStoreService
import com.example.studentmanagement.Model.User
import com.example.studentmanagement.databinding.ActivityAddUserBinding
import java.util.*

class AddUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUserBinding
    private val userManagementFireStoreService = UserManagementFireStoreService(this@AddUserActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddUserBinding.inflate(layoutInflater);
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        binding.buttonAddUser.setOnClickListener {
            addUser()
        }
    }


    private fun addUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val username = binding.editTextUsername.text.toString().trim()
        val age = binding.editTextAge.text.toString().trim().toIntOrNull() ?: 20
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val status = when (binding.radioGroupStatus.checkedRadioButtonId) {
            R.id.radioButtonNormal -> "Normal"
            R.id.radioButtonLocked -> "Locked"
            else -> "Normal" // Default to "Normal" if none is selected
        }

        val role = when (binding.radioGroupRole.checkedRadioButtonId) {
            R.id.radioButtonManagement -> "Management"
            R.id.radioButtonUser -> "User"
            else -> "User" // Default to "User" if none is selected
        }

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Email, password, username or phoneNumber is required", Toast.LENGTH_SHORT).show()
            return;
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least than 6 and less than 20 characters", Toast.LENGTH_SHORT)
                .show()
            return;
        }

        if (phoneNumber.length != 10) {
            Toast.makeText(this, "phone number must be equal ten number", Toast.LENGTH_SHORT).show()
            return;
        }

        if (age > 100 || age < 0) {
            Toast.makeText(this, "Age is invalid", Toast.LENGTH_SHORT).show()
            return;
        }

        val hashedPassword = BCrypt.withDefaults().hashToString(10, password.toCharArray())
        val userId  = UUID.randomUUID().toString()

        val user = User(userId, email, hashedPassword, username, age, phoneNumber, status, role)
        userManagementFireStoreService.addUserToFirestore(userId, user)
        finish()

    }


}