package com.example.visit_jeju_app.community.recycler

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.visit_jeju_app.community.activity.CommDetailActivity
import com.example.visit_jeju_app.community.model.CommunityData
import com.example.visit_jeju_app.databinding.CommunityItemBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat


class CommunityViewHolder(val binding: CommunityItemBinding) : RecyclerView.ViewHolder(binding.root) {

}
// crud된 파이어베이스 데이터가 activiy_comm_read.xml 뷰에 자동반영되도록 하는 코드
class CommunityAdapter(val context: Context, private var itemList: MutableList<CommunityData>) :
    RecyclerView.Adapter<CommunityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CommunityViewHolder(CommunityItemBinding.inflate(layoutInflater))

    }
    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        // crud된 파이어베이스 데이터가 activiy_comm_read.xml 뷰에 자동반영되도록 하는 코드
        val data = itemList[position]


        holder.binding.run {
            itemTitleView.text=data.title
            itemContentView.text=data.content

            // 파이어베이스에 저장된 timestamp형의 데이터를 불러와서
            // activity_comm_read.xml에 최신순으로 나타나도록하는 관련코드
            // Timestamp를 문자열로 변환하여 표시
            // timestamp형이 아닌 string이면서 "yyyy-MM-dd HH:mm"포맷으로 파이어베이스 저장 및 조회 관련 코드
            itemDateView.text = data.date

            // 카테고리를 파이어베이스에 저장하는 코드
            itemCategoryView.text = "${data.category}" // 추가된 라인

        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CommDetailActivity::class.java)
            intent.putExtra("DocId", data.docId)
            intent.putExtra("CommunityTitle", data.title)
            intent.putExtra("CommunityContent", data.content)
            intent.putExtra("CommunityDate", data.date)
            intent.putExtra("Comment", data.comment)

            // 카테고리를 파이어베이스에 저장하는 코드
            intent.putExtra("Category", data.category) // 추가된 라인
            context.startActivity(intent)
        }
    }

    // crud된 파이어베이스 데이터가 activiy_comm_read.xml 뷰에 자동반영되도록 하는 코드
    fun updateData(newItemList: MutableList<CommunityData>) {
        itemList = newItemList
        notifyDataSetChanged()
    }


//    // Timestamp를 문자열로 변환하는 함수
//    private fun timestampToString(timestamp: Timestamp): String {
//        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(timestamp.toDate())
//    }
}