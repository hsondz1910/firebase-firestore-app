package com.example.studentmanagement

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.studentmanagement.databinding.ActivityEditProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class EditProfileActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference

    private lateinit var userId: String

    private var loadingDialog: ProgressDialog? = null

    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference = FirebaseStorage.getInstance().reference.child("Images")

        userId = intent.getStringExtra("userId").toString()


        loadUserProfile()

        binding.btnImageView.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024) //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.buttonEditUser.setOnClickListener {
            updateProfileHandler()
        }
    }

    private fun updateProfileHandler() {
        showLoading()
        val updatedName = binding.editTextUsername.text.toString()
        val updatedAge = binding.editTextAge.text.toString().toInt()
        val updatedPhone = binding.editTextPhoneNumber.text.toString()

        val updatedUser = mapOf(
            "phoneNumber" to updatedPhone,
            "age" to updatedAge,
            "fullName" to updatedName,
        )

        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.update(updatedUser)
            .addOnCompleteListener {
                hideLoading()
                Toast.makeText(this, "Successful to update profile", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.data.also {
            if (it != null) {
                binding.btnImageView.setImageURI(it)
                updateAvatar(it)
            }
        }

    }

    private fun updateAvatar(avatar:Uri) {

        val avatarRef = storageReference.child("avatars/${userId}")

        avatarRef.putFile(avatar)
            .addOnSuccessListener {
                // Get the downloadable URL of the uploaded image
                avatarRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update the Firestore document with the new avatar URL
                    updateFirestoreAvatar(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload avatar: ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun updateFirestoreAvatar(avatarUrl: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("avatar", avatarUrl)

    }

    private fun loadUserProfile() {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val avatarUrl = documentSnapshot.getString("avatar")
                    val name = documentSnapshot.getString("fullName")
                    val age = documentSnapshot.get("age")
                    val phone = documentSnapshot.getString("phoneNumber")


                    // Load and display the avatar image using ImagePicker
                    if (avatarUrl!= null) {
                        Glide.with(this@EditProfileActivity)
                            .load(avatarUrl)
                            .into(binding.btnImageView)
                    }
                    else {
                        Glide.with(this@EditProfileActivity)
                            .load(R.drawable.ic_student)
                            .into(binding.btnImageView)
                    }

                    binding.editTextAge.setText(age.toString())
                    binding.editTextPhoneNumber.setText(phone)
                    binding.editTextUsername.setText(name)
                }
            }

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
}

