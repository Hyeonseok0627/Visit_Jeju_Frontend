package com.example.visit_jeju_app.community.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.visit_jeju_app.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_board_list.view.*

class BoardListFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val boardList = mutableListOf<BoardData>()
    private lateinit var boardAdapter: BoardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_board_list, container, false)

        boardAdapter = BoardAdapter(boardList) { boardData ->
            // Handle item click (e.g., open detail fragment or activity)
        }

        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = boardAdapter
        }

        view.fabAdd.setOnClickListener {
            // Open add board fragment or activity
        }

        loadBoardData()

        return view
    }

    private fun loadBoardData() {
        db.collection("boards")
            .get()
            .addOnSuccessListener { result ->
                boardList.clear()
                for (document in result) {
                    val boardData = document.toObject(BoardData::class.java)
                    boardData.docId = document.id
                    boardList.add(boardData)
                }
                boardAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
}
