package com.example.studentmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.databinding.ActivityDetailStudentBinding
import com.google.firebase.database.*

class DetailStudentActivity : AppCompatActivity() {

    private lateinit var  binding:ActivityDetailStudentBinding

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance();
    private val studentsRef: DatabaseReference = database.reference.child("students")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentID = intent.getStringExtra("studentID")

        updateProfileStudent(studentID)

        binding.btnViewCertificates.setOnClickListener {
            val certificateIntent = Intent(this@DetailStudentActivity, CertificateActivity::class.java)
            certificateIntent.putExtra("studentID", studentID)
            startActivity(certificateIntent)

        }


    }

    private fun updateProfileStudent(sid: String?) {
        if (sid != null) {
            studentsRef.child(sid).addListenerForSingleValueEvent(object: ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val name = snapshot.child("name").value.toString()
                        val email =  snapshot.child("email").value.toString()
                        val studentID =  snapshot.child("studentID").value.toString()
                        val faculty =  snapshot.child("faculty").value.toString()

                        binding.textViewEmail.text ="Email: $email"
                        binding.textViewFaculty.text = "Faculty: $faculty"
                        binding.textViewName.text = "Name: $name"
                        binding.textViewStudentID.text = "StudentID: $studentID"

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching student data: ${error.message}")
                }

            })

        }
    }
 }