package com.example.studentmanagement.FireStoreService

import android.content.Context
import android.widget.Toast
import com.example.studentmanagement.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserManagementFireStoreService(private val context:Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun getUsers(callback: (List<User>) -> Unit) {
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                callback(users)
            }
            .addOnFailureListener { exception ->
                showToastError(exception.message.toString())
            }
    }


     fun addUserToFirestore(uid: String, user:User) {
        // Save user information to Firestore with UID as document ID
        usersCollection.document(uid).set(user)
            .addOnSuccessListener {
                // User information saved successfully
                showToastSuccess("add new user")
            }
            .addOnFailureListener { exception ->
                // Handle failure to save user information
                showToastError(exception.message.toString())
            }
    }

    private fun showToastError(message:String) {
        return Toast.makeText(context, "Error : $message", Toast.LENGTH_SHORT).show()
    }

    private fun showToastSuccess(message:String) {
        return Toast.makeText(context, "Successfully $message", Toast.LENGTH_SHORT).show()
    }

    fun deleteUser(userId: String) {
        usersCollection.document(userId).delete()
            .addOnSuccessListener {
                showToastSuccess("delete a user")
            }
            .addOnFailureListener { exception ->
                showToastError(exception.message.toString())
            }
    }

    fun updateUser( updatedUser: User) {
        usersCollection.document(updatedUser.userId).set(updatedUser, SetOptions.merge())
            .addOnSuccessListener {
                showToastSuccess("update a user ${updatedUser.userId}")
            }
            .addOnFailureListener { exception ->
                showToastError(exception.message.toString())
            }
    }
}