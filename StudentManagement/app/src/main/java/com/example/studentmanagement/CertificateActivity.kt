package com.example.studentmanagement

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.Adapter.CertificateAdapter
import com.example.studentmanagement.Model.Certificate
import com.example.studentmanagement.databinding.ActivityCertificateBinding
import com.google.firebase.database.*
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.*

class CertificateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCertificateBinding

    private lateinit var studentId: String
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance();
    private val studentsRef: DatabaseReference = database.reference.child("students")

    private lateinit var certificateAdapter: CertificateAdapter

    private val certificateList = ArrayList<Certificate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        studentId = intent.getStringExtra("studentID").toString()

        retrieveCertificatesData()

        certificateAdapter = CertificateAdapter(
            certificateList,
        ) { certificate, view -> showPopupMenu(certificate, view) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CertificateActivity)
            adapter = certificateAdapter
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.certificate_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_add_certificate -> {
                showAddCertificateDialog()
                true
            }

            R.id.action_import_certificate -> {
                readCSVFromAssets(this@CertificateActivity, "certificates.csv")
                true
            }

            R.id.action_export_certificate -> {
                exportToCSV(this@CertificateActivity, "export_certificate")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun showAddCertificateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_new_certificate, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Certificate")

        val dialog = dialogBuilder.create()

        // Handle button click inside the dialog
        val addButton = dialogView.findViewById<Button>(R.id.buttonAddCertificate)
        addButton.setOnClickListener {
            // Retrieve data from EditText fields
            val name = dialogView.findViewById<EditText>(R.id.editTextCertificateName).text.toString()
            val type = dialogView.findViewById<EditText>(R.id.editTextCertificateType).text.toString()
            val level = dialogView.findViewById<EditText>(R.id.editTextCertificateLevel).text.toString()
            val date = dialogView.findViewById<EditText>(R.id.editTextCertificateDate).text.toString()
            val score = dialogView.findViewById<EditText>(R.id.editTextCertificateScore).text.toString()

            val certificateID = UUID.randomUUID().toString()

            val newCertificate = Certificate(certificateID, name, date, type, score, level)

            // Add the new certificate to the database
            studentsRef.child(studentId).child("certificates").child(certificateID).setValue(newCertificate)
                .addOnSuccessListener {
                    // Certificate added successfully
                    showToast("New certificate added successfully")
                }
                .addOnFailureListener { e ->
                    // Handle failure, show an error message or log the error
                    showToast("Failed to add new certificate: ${e.message}")
                }

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun showPopupMenu(certificate: Certificate, v: View) {
        val popupMenu = PopupMenu(this, v)

        popupMenu.menuInflater.inflate(R.menu.certificate_context_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.certificate_action_delete -> {
                    showConfirmDeleteCertificate(certificate)
                    true
                }

                R.id.certificate_action_edit -> {
                    showEditCertificateDialog(certificate)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }


    @SuppressLint("CutPasteId")
    private fun showEditCertificateDialog(certificate: Certificate) {
        val dialogView = layoutInflater.inflate(R.layout.add_new_certificate, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Certificate")

        // Initialize EditText fields with current values
        val editTextCertificateName = dialogView.findViewById<EditText>(R.id.editTextCertificateName)
        editTextCertificateName.setText(certificate.name)

        val editTextCertificateLevel = dialogView.findViewById<EditText>(R.id.editTextCertificateLevel)
        editTextCertificateLevel.setText(certificate.level)

        val editTextCertificateDate = dialogView.findViewById<EditText>(R.id.editTextCertificateDate)
        editTextCertificateDate.setText(certificate.date)

        val editTextCertificateType = dialogView.findViewById<EditText>(R.id.editTextCertificateType)
        editTextCertificateType.setText(certificate.type)

        val editTextCertificateScore = dialogView.findViewById<EditText>(R.id.editTextCertificateScore)
        editTextCertificateScore.setText(certificate.score)

        val dialog = dialogBuilder.create()

        // Handle button click inside the dialog
        val updateButton = dialogView.findViewById<Button>(R.id.buttonAddCertificate)
        updateButton.setOnClickListener {
            // Retrieve updated values from EditText fields

            val name = dialogView.findViewById<EditText>(R.id.editTextCertificateName).text.toString()
            val type = dialogView.findViewById<EditText>(R.id.editTextCertificateType).text.toString()
            val level = dialogView.findViewById<EditText>(R.id.editTextCertificateLevel).text.toString()
            val date = dialogView.findViewById<EditText>(R.id.editTextCertificateDate).text.toString()
            val score = dialogView.findViewById<EditText>(R.id.editTextCertificateScore).text.toString()
            // Retrieve other updated attributes

            val updatedValues = mapOf(
                "name" to name,
                "type" to type,
                "level" to level,
                "date" to date,
                "score" to score
            )

            studentsRef.child(studentId).child("certificates").child(certificate.id)
                .updateChildren(updatedValues)
                .addOnSuccessListener {
                    // Certificate updated successfully
                    showToast("Certificate updated successfully")
                }
                .addOnFailureListener { e ->
                    // Handle failure, show an error message or log the error
                    showToast("Failed to update certificate: ${e.message}")
                }
            // Dismiss the dialog
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showConfirmDeleteCertificate(certificate: Certificate) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure want to delete certificate ${certificate.name}")
            .setPositiveButton("Delete") { _, _ ->
                deleteCertificate(certificate)
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCertificate(certificate: Certificate) {

        studentsRef.child(studentId).child("certificates").child(certificate.id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Student removed successfully
                    Toast.makeText(this@CertificateActivity, "Certificate removed successfully", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Failed to remove student
                    Toast.makeText(this@CertificateActivity, "Failed to remove certificate", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun retrieveCertificatesData() {

        // Set up a ValueEventListener to listen for data changes
        studentsRef.child(studentId).child("certificates").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called when the data changes

                certificateList.clear()

                for (certificateSnapshot in dataSnapshot.children) {
                    val certificate = certificateSnapshot.getValue(Certificate::class.java)
                    if (certificate != null) {
                        certificateList.add(certificate)
                    }
                }

                // Now you have the list of certificates, you can update your RecyclerView adapter
                certificateAdapter.updateNewList(certificateList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Toast.makeText(applicationContext, "Failed to retrieve certificates", Toast.LENGTH_SHORT).show()
            }
        })
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
            for (certificate in certificateList) {
                val rowData =
                    arrayOf(certificate.name, certificate.date, certificate.level, certificate.score, certificate.type)
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
                val certificate = Certificate(
                    id = UUID.randomUUID().toString(),
                    name = nextLine?.get(0) ?: "",
                    date = nextLine?.get(1) ?: "",
                    type = nextLine?.get(2) ?: "",
                    score = nextLine?.get(3) ?: "",
                    level = nextLine?.get(4) ?: ""
                )
                certificateList.add(certificate)
            }
            csvReader.close()
            for (certificate in certificateList) {
                studentsRef.child(studentId).child("certificates").child(certificate.id).setValue(certificate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error reading CSV file ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}