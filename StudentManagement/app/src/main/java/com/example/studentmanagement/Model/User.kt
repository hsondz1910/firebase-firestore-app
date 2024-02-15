package com.example.studentmanagement.Model
import java.io.Serializable


data class User(
    val userId: String = "",
    val email: String = "",
    val password:String = "",
    val fullName: String= "",
    val age: Int = 0,
    val phoneNumber: String = "",
    val status: String = "Normal", // 'Normal' or 'Locked'
    val role: String = "Management",// 'Management' or 'User'
    val loginHistory: List<String> = mutableListOf(),
    val avatar: String =""
) : Serializable