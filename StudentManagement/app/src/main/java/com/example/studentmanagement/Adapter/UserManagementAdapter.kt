package com.example.studentmanagement.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.Model.User
import com.example.studentmanagement.databinding.UserItemBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class UserManagementAdapter(
    val userList: ArrayList<User>,
    private val onItemLongClick: (User,View) -> Unit

): RecyclerView.Adapter<UserManagementAdapter.UserViewHolder>() {

    private var listenerRegistration: ListenerRegistration

    init {
        val firestore = FirebaseFirestore.getInstance()
        val usersCollection = firestore.collection("users")

        listenerRegistration = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                handleSnapshot(snapshot)
            }
        }

    }

    private fun handleSnapshot(snapshot: QuerySnapshot) {
        for (change in snapshot.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    val user = change.document.toObject(User::class.java)
                    userList.add(user)
                    notifyItemInserted(userList.size - 1)
                }
                DocumentChange.Type.MODIFIED -> {
                    val user = change.document.toObject(User::class.java)
                    val index = userList.indexOfFirst { it.userId == user.userId }
                    if (index != -1) {
                        userList[index] = user
                        notifyItemChanged(index)
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    val user = change.document.toObject(User::class.java)
                    val index = userList.indexOfFirst { it.userId == user.userId }
                    if (index != -1) {
                        userList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserItemBinding.inflate(inflater,parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if(userList.isNotEmpty()){
            val user = userList[position];
            holder.bind(user)

        }
    }

    override fun getItemCount(): Int {
        if(userList.isNotEmpty()) return userList.size
         return 0
    }

    fun deleteUser(userId: String) {
        val position = userList.indexOfFirst { it.userId == userId }
        if (position != -1) {
            userList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class UserViewHolder (private val binding: UserItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.usernameTextView.text = user.fullName
            binding.emailTextView.text= user.email
            binding.phoneTextView.text= user.phoneNumber

            binding.root.setOnLongClickListener{
                onItemLongClick(user, it)
                true
            }
        }
    }

}