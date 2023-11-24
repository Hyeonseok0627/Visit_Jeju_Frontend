package com.example.visit_jeju_app.community.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_board.view.*
import com.example.visit_jeju_app.R

class AddBoardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_board, container, false)

        view.btnSave.setOnClickListener {
            val title = view.editTitle.text.toString()
            val content = view.editContent.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                addBoardToFirestore(title, content)
            }
        }

        return view
    }

    private fun addBoardToFirestore(title: String, content: String) {
        val boardData = BoardData(title, content, getCurrentDate())
        db.collection("boards")
            .add(boardData)
            .addOnSuccessListener { documentReference ->
                // Handle successful addition
            }
            .addOnFailureListener { e ->
                // Handle errors
            }
    }

    private fun getCurrentDate(): String {
        // Implement a function to get the current date
        return ""
    }
}