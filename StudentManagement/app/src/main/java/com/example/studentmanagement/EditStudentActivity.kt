package com.example.studentmanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.databinding.ActivityEditStudentBinding

class EditStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStudentBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentID = intent.getStringExtra("STUDENT_ID")
        val name = intent.getStringExtra("STUDENT_NAME")
        val email = intent.getStringExtra("STUDENT_EMAIL")
        val faculty = intent.getStringExtra("STUDENT_FACULTY")

        binding.editTextStudentID.text = "StudentID: $studentID"
        binding.editTextEmail.setText (email)
        binding.editTextName.setText (name)
        binding.editTextFaculty.setText (faculty)

        binding.btnEditStudent.setOnClickListener {
            if (studentID != null) {
                editStudentHandler(studentID)
            }
        }

    }

    private fun editStudentHandler(studentID:String) {

        val updatedName = binding.editTextName.text.toString().trim()
        val updatedEmail = binding.editTextEmail.text.toString().trim()
        val updatedFaculty = binding.editTextFaculty.text.toString().trim()

        if( updatedName.isEmpty()|| updatedEmail.isEmpty() || updatedFaculty.isEmpty()) {
            Toast.makeText(this, "StudentId, name, email, and faculty is required", Toast.LENGTH_SHORT).show()
            return;
        }

        val resultIntent = Intent()

        resultIntent.putExtra("STUDENT_ID", studentID)
        resultIntent.putExtra("STUDENT_NAME", updatedName)
        resultIntent.putExtra("STUDENT_EMAIL",updatedEmail )
        resultIntent.putExtra("STUDENT_FACULTY", updatedFaculty)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}