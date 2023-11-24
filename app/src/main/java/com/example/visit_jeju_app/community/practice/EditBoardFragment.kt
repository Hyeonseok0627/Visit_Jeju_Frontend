package com.example.visit_jeju_app.community.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.visit_jeju_app.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_board.view.*

class EditBoardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_board, container, false)

        val docId = arguments?.getString("docId")

        if (docId != null) {
            loadBoardData(docId)
        }

        view.btnSaveEdit.setOnClickListener {
            // Save edited data to Firestore
        }

        return view
    }

    private fun loadBoardData(docId: String) {
        db.collection("boards").document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val boardData = document.toObject(BoardData::class.java)
                    // Update UI with boardData for editing
                    // e.g., view.editTitle.setText(boardData?.title)
                    // view.editContent.setText(boardData?.content)
                    // ...
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
}