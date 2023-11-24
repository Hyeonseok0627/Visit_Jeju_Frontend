package com.example.visit_jeju_app.community.practice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.visit_jeju_app.R
import kotlinx.android.synthetic.main.item_board.view.*

class BoardAdapter(
    private val boardList: List<BoardData>,
    private val onItemClick: (BoardData) -> Unit
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(boardList[position])
    }

    override fun getItemCount(): Int = boardList.size

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(boardData: BoardData) {
            itemView.tvTitle.text = boardData.title
            itemView.tvContent.text = boardData.content
            itemView.tvDate.text = boardData.date

            itemView.setOnClickListener { onItemClick(boardData) }
        }
    }
}