package com.example.studentmanagement


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.studentmanagement.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private var loadingDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater);
        setContentView(binding.root)


        binding.loginButton.setOnClickListener {
            loginHandler()
        }

    }

    private fun loginHandler() {
        showLoading()
        val email = binding.emailEditText.text.toString().trim();
        val password = binding.passwordlEditText.text.toString().trim();


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Email or password is required", Toast.LENGTH_SHORT).show()
            hideLoading()
            return;
        }

        if (password.length < 6) {
            Toast.makeText(this@LoginActivity, "Password must be more or equal 6 characters", Toast.LENGTH_SHORT).show()
            hideLoading()
            return;
        }

        if (email == "admin@gmail.com" && password == "admin123") {
            hideLoading()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
        } else {
            signIn(email, password)
        }

    }

    private fun signIn(email: String, password: String) {

        val userRef = FirebaseFirestore.getInstance().collection("users")
        userRef.get()
            .addOnSuccessListener { querySnapshot ->
                // Loop through the documents
                for (document in querySnapshot.documents) {
                    val userId = document.id
                    val emailUser = document.getString("email")
                    val passwordUser = document.getString("password")
                    val userRole = document.getString("role")

                    val deviceDetails = getDeviceDetails()
                    val currentDateTime = Calendar.getInstance()
                    val currentCurrentMinute = currentDateTime.get(Calendar.MINUTE)
                    val currentHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
                    val currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
                    val currentMonth = currentDateTime.get(Calendar.MONTH) + 1 // Month is zero-based
                    val currentYear = currentDateTime.get(Calendar.YEAR)



                    val loginInfo = "Time: $currentDay/$currentMonth/$currentYear $currentHour Hour:$currentCurrentMinute Minutes, Device: $deviceDetails"
                    val confirmPassword = BCrypt.verifyer().verify(password.toCharArray(), passwordUser)
                    if (confirmPassword.verified && emailUser == email) {

                        when (userRole) {
                            "Management" -> {
                                hideLoading()
                                userRef.document(userId).update("loginHistory", FieldValue.arrayUnion(loginInfo))
                                val intent = Intent(this, ManagementActivity::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                                finish()
                                return@addOnSuccessListener Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
                            }

                            "User" -> {
                                hideLoading()
                                userRef.document(userId).update("loginHistory", FieldValue.arrayUnion(loginInfo))
                                val intent = Intent(this, UserActivity::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                                finish()
                                return@addOnSuccessListener Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                hideLoading()
                                Toast.makeText(this, "Email or password went wrong", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                }

                hideLoading()
                Toast.makeText(this, "Email or password went wrong", Toast.LENGTH_SHORT).show()

            }

            .addOnFailureListener { _ ->
                hideLoading()
                Toast.makeText(this, "Email or password went wrong", Toast.LENGTH_SHORT).show()
            }

        return;

    }

    private fun showLoading() {
        loadingDialog = ProgressDialog(this)
        loadingDialog?.setMessage("Logging in...")
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun getDeviceDetails(): String {
        val device = Build.MANUFACTURER + " " + Build.MODEL
        val version = "Android " + Build.VERSION.RELEASE
        return "$device, $version"
    }

}