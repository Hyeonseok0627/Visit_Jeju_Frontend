package com.example.visit_jeju_app.community.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.visit_jeju_app.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_delete_board.view.*

class DeleteBoardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_board, container, false)

        val docId = arguments?.getString("docId")

        view.btnDelete.setOnClickListener {
            if (docId != null) {
                deleteBoard(docId)
            }
        }

        return view
    }

    private fun deleteBoard(docId: String) {
        db.collection("boards").document(docId)
            .delete()
            .addOnSuccessListener {
                // Handle successful deletion
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
}