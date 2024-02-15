package com.example.studentmanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.Adapter.StudentAdapter
import com.example.studentmanagement.Model.Student
import com.example.studentmanagement.databinding.ActivityStudentBinding
import com.google.firebase.database.*
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private lateinit var studentAdapter: StudentAdapter
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance();
    private val studentsRef: DatabaseReference = database.reference.child("students")

    private val EDIT_USER_REQUEST_CODE = 1


    private val sortingOptions = booleanArrayOf(false, false, false)

    private val studentList = ArrayList<Student>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        retrieveDataFromDatabase()

        studentAdapter = StudentAdapter(
            studentList,
            { student, view -> showPopupMenu(student, view) },
            { student -> startDetailStudent(student) })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StudentActivity)
            adapter = studentAdapter
        }

        binding.btnSortStudent.setOnClickListener {
            showMultiSelectSortOptionsDialog()
        }

        binding.btnSearchStudents.setOnClickListener {
            showSearchDialog()
        }


    }

    private fun startDetailStudent(student: Student) {
        val intentDetailStudent = Intent(this@StudentActivity, DetailStudentActivity::class.java);
        intentDetailStudent.putExtra("studentID", student.studentID)
        startActivity(intentDetailStudent)
    }


    private fun exportToCSV(context: Context, fileName: String) {
        val folder = File(getExternalFilesDir(null), "assets")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val filePath = "$folder/$fileName.csv"

        try {
            val csvWriter = CSVWriter(FileWriter(filePath))

            // Write student data to CSV file
            for (student in studentList) {
                val rowData = arrayOf(student.studentID, student.name, student.email, student.faculty)
                csvWriter.writeNext(rowData)
            }

            // Close the CSV writer
            csvWriter.close()

            Toast.makeText(context, "CSV file exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV file", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun readCSVFromAssets(context: Context, fileName: String) {
        try {
            // Open the CSV file using AssetManager
            val assetManager: AssetManager = context.assets
            val inputStreamReader = InputStreamReader(assetManager.open(fileName))
            val csvReader = CSVReader(inputStreamReader)

            var nextLine: Array<String>?
            while (csvReader.readNext().also { nextLine = it } != null) {
                // Process each line of the CSV file
                val student = Student(
                    studentID = nextLine?.get(0) ?: "",
                    name = nextLine?.get(1) ?: "",
                    email = nextLine?.get(2) ?: "",
                    faculty = nextLine?.get(3) ?: "",
                )
                studentList.add(student)
            }
            csvReader.close()
            for (student in studentList) {
                studentsRef.child(student.studentID).setValue(student)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error reading CSV file", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ResourceType")
    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.search_dialog, null)
        val editTextStudentId: EditText = dialogView.findViewById(R.id.editTextDialogStudentId)
        val editTextEmail: EditText = dialogView.findViewById(R.id.editTextDialogEmail)
        val editTextFaculty: EditText = dialogView.findViewById(R.id.editTextDialogFaculty)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Search") { _, _ ->
                val studentId = editTextStudentId.text.toString()
                val email = editTextEmail.text.toString()
                val faculty = editTextFaculty.text.toString()

                // Perform search based on the entered values
                searchStudents(studentId, email, faculty)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun searchStudents(studentId: String, email: String, faculty: String) {
        // Construct the Firebase query based on search criteria
        var query: Query = studentsRef

        if (studentId.isNotEmpty()) {
            if (email.isNotEmpty()) {
                if (faculty.isNotEmpty()) {
                    query = query.orderByChild("studentID_email_faculty").equalTo(studentId + email + faculty)
                    Toast.makeText(this, "Search by studentID, email and faculty", Toast.LENGTH_SHORT).show()
                } else {
                    query = query.orderByChild("studentID_email").equalTo(studentId + email)
                    Toast.makeText(this, "Search by studentID and email", Toast.LENGTH_SHORT).show()
                }
            } else if (faculty.isNotEmpty()) {
                query = query.orderByChild("studentID_faculty").equalTo(studentId + faculty)
                Toast.makeText(this, "Search by studentID and faculty", Toast.LENGTH_SHORT).show()
            } else {
                query = query.orderByChild("studentID").equalTo(studentId)
                Toast.makeText(this, "Search by studentID $studentId", Toast.LENGTH_SHORT).show()
            }
        } else if (email.isNotEmpty()) {
            if (faculty.isNotEmpty()) {
                query = query.orderByChild("email_faculty").equalTo(email + faculty)
                Toast.makeText(this, " Search by email and faculty", Toast.LENGTH_SHORT).show()
            } else {
                query = query.orderByChild("email").equalTo(email)
                Toast.makeText(this, "Search by email $email", Toast.LENGTH_SHORT).show()
            }
        } else if (faculty.isNotEmpty()) {
            query = query.orderByChild("faculty").equalTo(faculty)
            Toast.makeText(this, "Search by faculty $faculty", Toast.LENGTH_SHORT).show()
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val searchResults: List<Student> = dataSnapshot.children.map {
                    it.getValue(Student::class.java) ?: Student()
                }
                studentAdapter.updateNewList(ArrayList(searchResults))

                for (result in searchResults) {
                    Log.d("SearchResult", "Student: $result")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors if needed
            }
        })
    }

    private fun showMultiSelectSortOptionsDialog() {
        val options = arrayOf("Sort by StudentID", "Sort by Email", "Sorted By Faculty")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Sorting Options:")
            .setMultiChoiceItems(options, sortingOptions) { _, which, isChecked ->
                sortingOptions[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                applySortingOptions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun applySortingOptions() {
        // Construct the Firebase query based on selected options
        var query: Query = studentsRef

        if (sortingOptions[0]) {
            if (sortingOptions[1]) {
                if (sortingOptions[2]) {
                    query = query.orderByChild("studentID_email_faculty")
                    Toast.makeText(this, "Sorted by studentID, email and faculty", Toast.LENGTH_SHORT).show()
                } else {
                    query = query.orderByChild("studentID_email")
                    Toast.makeText(this, "Sorted by studentID and email", Toast.LENGTH_SHORT).show()
                }
            } else if (sortingOptions[2]) {
                query = query.orderByChild("studentID_faculty")
                Toast.makeText(this, "Sorted by  studentID and faculty", Toast.LENGTH_SHORT).show()
            } else {
                query = query.orderByChild("studentID")
                Toast.makeText(this, "Sorted by studentID", Toast.LENGTH_SHORT).show()
            }
        } else if (sortingOptions[1]) {
            if (sortingOptions[2]) {
                query = query.orderByChild("email_faculty")
                Toast.makeText(this, "Sorted by email and faculty", Toast.LENGTH_SHORT).show()
            } else {
                query = query.orderByChild("email")
                Toast.makeText(this, "Sorted by email", Toast.LENGTH_SHORT).show()
            }
        } else if (sortingOptions[2]) {
            query.orderByChild("faculty")
            Toast.makeText(this, "Sorted by faculty", Toast.LENGTH_SHORT).show()
        }

        // Perform the query and update the RecyclerView
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedList = dataSnapshot.children.map { it.getValue(Student::class.java) }
                // Update the RecyclerView with the sorted list
                val sortedArrayList = ArrayList(sortedList.filterNotNull())
                // Update the local dataset in the adapter
                studentAdapter.updateNewList(sortedArrayList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors if needed
            }
        })
    }

    private fun retrieveDataFromDatabase() {
        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentList.clear()

                for (dataSnapshot in snapshot.children) {
                    val student = dataSnapshot.getValue(Student::class.java)
                    if (student != null) {
                        studentList.add(student)
                    }
                }

                studentAdapter.updateNewList(studentList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.student_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_add_student -> {
                startActivity(Intent(this, AddStudentActivity::class.java))
                true
            }

            R.id.action_import_student -> {
                readCSVFromAssets(this@StudentActivity, "students.csv")
                true
            }

            R.id.action_export_student -> {
                exportToCSV(this@StudentActivity, "export_students")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun showPopupMenu(student: Student, v: View) {
        val popupMenu = PopupMenu(this, v)

        popupMenu.menuInflater.inflate(R.menu.student_context_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.student_action_edit -> {
                    val editStudentIntent = Intent(this@StudentActivity, EditStudentActivity::class.java)

                    editStudentIntent.putExtra("STUDENT_ID", student.studentID)
                    editStudentIntent.putExtra("STUDENT_NAME", student.name)
                    editStudentIntent.putExtra("STUDENT_EMAIL", student.email)
                    editStudentIntent.putExtra("STUDENT_FACULTY", student.faculty)


                    startActivityForResult(editStudentIntent, EDIT_USER_REQUEST_CODE)
                    true
                }

                R.id.student_action_delete -> {
                    showConfirmDeleteStudent(student)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showConfirmDeleteStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure want to delete student ID ${student.studentID}")
            .setPositiveButton("Delete") { _, _ ->
                deleteStudent(student)
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteStudent(student: Student) {

        studentsRef.child(student.studentID).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Student removed successfully
                    Toast.makeText(this@StudentActivity, "Student removed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Failed to remove student
                    Toast.makeText(this@StudentActivity, "Failed to remove student", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_USER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Retrieve updated user details from the result intent
            val studentID = data?.getStringExtra("STUDENT_ID")
            val updatedName = data?.getStringExtra("STUDENT_NAME")
            val updatedEmail = data?.getStringExtra("STUDENT_EMAIL")
            val updatedFaculty = data?.getStringExtra("STUDENT_FACULTY")

            val updatedStudentData = mapOf(
                "name" to updatedName,
                "email" to updatedEmail,
                "faculty" to updatedFaculty,
                "studentID_email" to studentID + updatedEmail,
                "studentID_faculty" to studentID + updatedFaculty,
                "studentID_email_faculty" to studentID + updatedEmail + updatedFaculty,
                "email_faculty" to updatedEmail + updatedFaculty
            )

            if (studentID != null) {
                studentsRef.child(studentID).updateChildren(updatedStudentData)
                    .addOnSuccessListener {
                        // Update successful
                        updatedStudentInRecyclerView(
                            studentID,
                            updatedName,
                            updatedEmail,
                            updatedFaculty,
                        )
                        Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Toast.makeText(this, "Failed to update user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }

    private fun updatedStudentInRecyclerView(
        studentID: String?,
        updatedName: String?,
        updatedEmail: String?,
        updatedFaculty: String?,
    ) {

        val position = findStudentPositionById(studentID)

        if (position != -1) {
            val updatedStudent = studentList[position].copy(
                studentID = studentID ?: studentList[position].studentID,
                name = updatedName ?: studentList[position].name,
                email = updatedEmail ?: studentList[position].email,
                faculty = updatedFaculty ?: studentList[position].faculty,

                )

            // Update the user in the list
            studentList[position] = updatedStudent

            // Notify the adapter of the data change
            studentAdapter.notifyItemChanged(position)
        }

    }


    private fun findStudentPositionById(studentID: String?): Int {
        // Implement logic to find the position of the user in your user list
        // You might loop through the userList and compare userIds
        // Return the position if found, or -1 if not found

        for ((index, user) in studentList.withIndex()) {
            if (user.studentID == studentID) {
                return index
            }
        }
        return -1;

    }

}