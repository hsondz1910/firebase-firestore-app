package com.example.studentmanagement.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.Model.Certificate
import com.example.studentmanagement.databinding.CertificateItemBinding

class CertificateAdapter(
    private var certificateList: ArrayList<Certificate>,
    private val onItemLongClick: (Certificate, View) -> Unit
) : RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder>() {


    inner class CertificateViewHolder(private val binding: CertificateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(certificate: Certificate) {
            binding.textViewCertificateName.text = "Name: ${certificate.name}"
            binding.textViewCertificateDate.text = "Date: ${certificate.date}"
            binding.textViewCertificateLevel.text = "Lever: ${certificate.level}"
            binding.textViewCertificateScore.text = "Score: ${certificate.score}"
            binding.textViewCertificateType.text = "Type:${certificate.type}"

            binding.root.setOnLongClickListener {
                onItemLongClick(certificate, it)
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val binding = CertificateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CertificateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        if (certificateList.isNotEmpty()) return certificateList.size
        return 0

    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        if (certificateList.isNotEmpty()) {
            val certificate = certificateList[position]
            holder.bind(certificate)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNewList(newList: ArrayList<Certificate>) {
        certificateList = newList
        notifyDataSetChanged()
    }
}