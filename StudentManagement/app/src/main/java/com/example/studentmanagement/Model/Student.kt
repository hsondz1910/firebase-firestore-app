package com.example.studentmanagement.Model

data class Student(
    val studentID: String = "",
    val name: String = "",
    val email: String = "",
    val faculty: String = "",
    val certificates: Map<String,Certificate> = emptyMap(),
) {


}