package com.example.visit_jeju_app.community.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.visit_jeju_app.MyApplication
import com.example.visit_jeju_app.R
import com.example.visit_jeju_app.community.practice.BoardAdapter
import com.example.visit_jeju_app.community.practice.BoardData
import com.example.visit_jeju_app.databinding.ActivityCommReadBinding

class CommReadActivity : AppCompatActivity() {

    lateinit var binding : ActivityCommReadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommReadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myCheckPermission(this)
        makeRecyclerView()

        binding.add.setOnClickListener {
            startActivity(Intent(this, CommWriteActivity::class.java))
        }

    }
    private fun makeRecyclerView(){
        MyApplication.db.collection("Boards")
            .get()
            .addOnSuccessListener {result ->
                val itemList = mutableListOf<BoardData>()
                for(document in result){
                    val item = document.toObject(BoardData::class.java)
                    item.docId=document.id
                    itemList.add(item)
                }
                binding.boardRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.boardRecyclerView.adapter = BoardAdapter(this, itemList)
            }
            .addOnFailureListener{exception ->
                Log.d("kkang", "error.. getting document..", exception)
                Toast.makeText(this, "서버 데이터 획득 실패", Toast.LENGTH_SHORT).show()
            }
    }

}