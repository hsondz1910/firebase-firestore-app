package com.example.studentmanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.Model.Student
import com.example.studentmanagement.databinding.ActivityAddStudentBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentBinding

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance();
    private val studentsRef: DatabaseReference = database.reference.child("students")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnAddStudent.setOnClickListener {
            addNewStudent()
        }
    }

    private fun addNewStudent() {
        val studentID = binding.editTextStudentID.text.toString().trim();
        val name = binding.editTextName.text.toString().trim();
        val email = binding.editTextEmail.text.toString().trim();
        val faculty = binding.editTextFaculty.text.toString().trim();

        if (studentID.isEmpty() || name.isEmpty() || email.isEmpty() || faculty.isEmpty()) {
            Toast.makeText(this, "StudentId, name, email, and faculty is required", Toast.LENGTH_SHORT).show()

            return;
        }

        if (studentID.length != 8) {
            Toast.makeText(this, "StudentId must be at 8 characters", Toast.LENGTH_SHORT).show()
            return;
        }

        val newStudent = Student(
            studentID,
            name,
            email,
            faculty,
            emptyMap(),
        )

        studentsRef.child(studentID).setValue(newStudent)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Student added successfully
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                    finish() // Optionally close the activity after success
                } else {
                    // Failed to add student
                    Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
                }
            }
    }
}