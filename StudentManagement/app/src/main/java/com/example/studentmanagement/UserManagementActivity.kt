package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.Adapter.UserManagementAdapter
import com.example.studentmanagement.FireStoreService.UserManagementFireStoreService
import com.example.studentmanagement.Model.User
import com.example.studentmanagement.databinding.ActivityUserBinding


private lateinit var binding: ActivityUserBinding

class UserManagementActivity : AppCompatActivity() {
    private lateinit var userManagementAdapter: UserManagementAdapter
    private var userList = mutableListOf<User>()
    private val EDIT_USER_REQUEST_CODE = 1

    private  val userFirebaseFirestore = UserManagementFireStoreService(this@UserManagementActivity)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater);
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = "Users Management"

        userFirebaseFirestore.getUsers { users: List<User> -> userList = users.toMutableList() }

        userManagementAdapter = UserManagementAdapter(ArrayList(userList)) { user, view -> showPopupMenu(user, view) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter = userManagementAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_add_user -> {
                startActivity(Intent(this, AddUserActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun showPopupMenu(user: User, v: View) {
        val popupMenu = PopupMenu(this, v)

        popupMenu.menuInflater.inflate(R.menu.user_context_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val editIntent = Intent(this, EditUserActivity::class.java)
                    editIntent.putExtra("user", user)
                    startActivityForResult(editIntent, EDIT_USER_REQUEST_CODE)
                    true
                }

                R.id.action_delete -> {
                    confirmDeleteUser(user)
                    true
                }

                R.id.action_view_history -> {
                    val intent = Intent(this@UserManagementActivity, HistoryLoginActivity::class.java)
                    intent.putExtra("userName", user.fullName)
                    intent.putExtra("userID", user.userId)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setMessage("Are you sure delete user ${user.fullName}")
            .setTitle("Confirm Deletion")
            .setPositiveButton("Delete") { _, _ ->
                userManagementAdapter.deleteUser(user.userId)
                userFirebaseFirestore.deleteUser(user.userId)
            }.setNegativeButton("Cancel", null)
            .show()

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve updated user details from the result intent
            val updatedUser = data?.getSerializableExtra("updatedUser") as? User

            if (updatedUser != null) {
                // Update the user in the RecyclerView
                updateUserInRecyclerView(updatedUser)
                userFirebaseFirestore.updateUser(updatedUser)
            }

        }
    }

    private fun updateUserInRecyclerView(updatedUser: User) {
        val position = userManagementAdapter.userList.indexOfFirst { it.userId == updatedUser.userId }
        if (position != -1) {
            userManagementAdapter.userList[position] = updatedUser
            userManagementAdapter.notifyItemChanged(position)
        }
    }


}