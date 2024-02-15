package com.example.studentmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.Model.User
import com.example.studentmanagement.databinding.ActivityEditUserBinding

class EditUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditUserBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = (intent.getSerializableExtra("user") as? User)!!

        // binding data


        binding.editTextUsername.setText(user.fullName)
        binding.editTextAge.setText(user.age.toString())
        binding.editTextPhoneNumber.setText(user.phoneNumber)
        when (user.status) {
            "Normal" -> binding.radioButtonNormal.isChecked = true
            "Locked" -> binding.radioButtonLocked.isChecked = true

            else -> binding.radioButtonNormal.isChecked = true
        }

        when (user.role) {
            "Management" -> binding.radioButtonManagement.isChecked = true
            "User" -> binding.radioButtonUser.isChecked = true

            else -> binding.radioButtonUser.isChecked = true
        }



        binding.buttonEditUser.setOnClickListener() {

            editUser()


        }
    }

    private fun editUser() {
        val updatedName = binding.editTextUsername.text.toString()
        val updatedAge = binding.editTextAge.text.toString().toInt()
        val updatedPhone = binding.editTextPhoneNumber.text.toString()

        val updatedStatus = when (binding.radioGroupStatus.checkedRadioButtonId) {
            R.id.radioButtonNormal -> "Normal"
            R.id.radioButtonLocked -> "Locked"
            else -> "Normal" // Default to "Normal" if none is selected
        }

        val updatedRole = when (binding.radioGroupRole.checkedRadioButtonId) {
            R.id.radioButtonManagement -> "Management"
            R.id.radioButtonLocked -> "User"
            else -> "User" // Default to "Normal" if none is selected
        }

        if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(this, "Email, password, username  phoneNumber is required", Toast.LENGTH_SHORT).show()
            return;
        }

        if (updatedPhone.length != 10) {
            Toast.makeText(this, "phone number must be equal ten number", Toast.LENGTH_SHORT).show()
            return;
        }

        if (updatedAge > 100 || updatedAge < 0) {
            Toast.makeText(this, "Age is invalid", Toast.LENGTH_SHORT).show()
            return;
        }


        val resultIntent = Intent()

        val updatedUser = User(user.userId, user.email, user.password, updatedName,updatedAge, updatedPhone, updatedStatus, updatedRole)

        resultIntent.putExtra("updatedUser", updatedUser)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}