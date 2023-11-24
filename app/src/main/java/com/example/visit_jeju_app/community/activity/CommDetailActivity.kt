package com.example.visit_jeju_app.community.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.visit_jeju_app.MyApplication
import com.example.visit_jeju_app.R
import com.example.visit_jeju_app.community.model.CommunityData
import com.example.visit_jeju_app.community.recycler.CommentAdapter
import com.example.visit_jeju_app.databinding.ActivityCommDetailBinding
import java.text.SimpleDateFormat

class CommDetailActivity : AppCompatActivity() {

    lateinit var binding : ActivityCommDetailBinding
    data class comment(val comment : String, val time : String)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val docId = intent.getStringExtra("DocId")
        val title = intent.getStringExtra("CommunityTitle")
        val content = intent.getStringExtra("CommunityContent")
        val date = intent.getStringExtra("CommunityDate")

        binding.CommunityTitle.text = title
        binding.CommunityDate.text = date
        binding.CommunityContent.text = content

        var commentlist = mutableListOf<comment>()
        var count = 0
        if (docId != null) {
            MyApplication.db.collection("Communities").document(docId).collection("Comments")
                .get()
                .addOnSuccessListener { result ->
                    val itemList = mutableListOf<CommunityData>()
                    for (document in result) {
                        //val item = document.toObject(BoardData::class.java)
                        //item.comment=document.id
                        //itemList.add(item)
                        commentlist.add(comment(document.data.get("comment").toString(), document.data.get("timestamp").toString()))
                        count++
                        if(result.size() == count) {
                            Log.d("test", "$commentlist")
                            binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
                            binding.commentRecyclerView.adapter = CommentAdapter(this, commentlist)
                        }
                    }

                }
        }



        // 수정
        binding.CommunityModify.setOnClickListener {
            val intent = Intent(this, CommUpdateActivity::class.java)
            intent.putExtra("DocId", docId)
            intent.putExtra("CommunityTitle", title)
            intent.putExtra("CommunityContent", content)
            intent.putExtra("CommunityDate", date)
            overridePendingTransition(0, 0) //인텐트 효과 없애기
            startActivity(intent) //액티비티 열기
            overridePendingTransition(0, 0) //인텐트 효과 없애기
            finish()
        }

        // 삭제
        binding.CommunityDelete.setOnClickListener {
            if (docId != null) {
                MyApplication.db.collection("Communities").document(docId)
                    .delete()
                val intent = intent //인텐트
                startActivity(intent) //액티비티 열기
                overridePendingTransition(0, 0) //인텐트 효과 없애기
                finish() //인텐트 효과 없애기
            }
        }

        // 등록한 이미지 가져 오기
        val imgRef = MyApplication.storage.reference.child("images/${docId}.jpg")
        imgRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(binding.ImageView)
            }
        }

        val datenow = SimpleDateFormat("yyyy-MM-dd HH:mm")
        // 댓글 등록
        binding.CommentWrite.setOnClickListener {
            val commentData = mapOf(
                "comment" to binding.CommunityComment.text.toString(),
                "timestamp" to datenow.format(System.currentTimeMillis()).toString()
            )
            if (docId != null) {
                MyApplication.db.collection("Communities").document(docId)
                    .collection("Comments").add(commentData)
            }
            finish()
            overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = intent //인텐트
            startActivity(intent) //액티비티 열기
            overridePendingTransition(0, 0) //인텐트 효과 없애기
        }
    }
}