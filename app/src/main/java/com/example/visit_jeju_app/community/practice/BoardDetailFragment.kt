package com.example.visit_jeju_app.community.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.visit_jeju_app.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_board_detail.view.*

class BoardDetailFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_board_detail, container, false)

        val docId = arguments?.getString("docId")

        if (docId != null) {
            loadBoardDetail(docId)
        }

        return view
    }

    private fun loadBoardDetail(docId: String) {
        db.collection("boards").document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val boardData = document.toObject(BoardData::class.java)
                    // Update UI with boardData
                    // e.g., view.title.text = boardData?.title
                    // ...

                    // Set up edit button click listener
                    view?.btnEdit?.setOnClickListener {
                        // Open edit board fragment or activity
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
}