package com.example.studentmanagement.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.Model.Student
import com.example.studentmanagement.databinding.StudentItemBinding

class StudentAdapter(
    private var studentList: ArrayList<Student>,
    private val onItemLongClick: (Student, View) -> Unit,
    private val onItemClick:(Student) -> Unit
): RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(private val binding: StudentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(student: Student) {

            binding.textViewName.text = student.name
            binding.textViewStudentID.text = "StudentID: ${student.studentID}"
            binding.textViewEmail.text = student.email
            binding.textViewFaculty.text = "Faculty:${student.faculty}"

            binding.root.setOnLongClickListener {
                onItemLongClick(student, it)
                true
            }

            binding.root.setOnClickListener {
                onItemClick(student)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = StudentItemBinding.inflate(LayoutInflater.from(parent.context),parent,false);
        return StudentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        if(studentList.isNotEmpty()) return studentList.size
        return 0
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        if(studentList.isNotEmpty()) {
            val student = studentList[position]
            holder.bind(student)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNewList(newList: ArrayList<Student>) {
        studentList = newList
        notifyDataSetChanged()
    }
}