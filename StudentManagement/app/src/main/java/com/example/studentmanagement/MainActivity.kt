package com.example.studentmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.databinding.ActivityMainBinding

@SuppressLint("StaticFieldLeak")
private lateinit var binding:ActivityMainBinding
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStudentManage.setOnClickListener {
            changeActivity(StudentActivity::class.java)
        }

        binding.btnUserManage.setOnClickListener {
            changeActivity(UserManagementActivity::class.java)
        }

        binding.btnLogOut.setOnClickListener{
            logoutHandler()
        }

    }

    private fun logoutHandler() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java);
        startActivity(loginIntent)
        finish()
    }


    private fun changeActivity(targetActivity: Class<*>) {
        val intent = Intent(this,targetActivity)
        startActivity(intent)
    }
}