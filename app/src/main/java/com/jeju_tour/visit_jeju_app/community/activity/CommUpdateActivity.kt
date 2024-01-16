package com.jeju_tour.visit_jeju_app.community.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jeju_tour.visit_jeju_app.MyApplication
import com.jeju_tour.visit_jeju_app.R
import com.jeju_tour.visit_jeju_app.community.dateToString
import com.jeju_tour.visit_jeju_app.databinding.ActivityCommUpdateBinding
import java.util.Date
import java.util.UUID

class CommUpdateActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommUpdateBinding

    // 내용만 변경할 때, 파이어베이스에 반영이 안되는 문제 해결 코드
    // filePath가 초기화가 되지않아서 안된 문제
    // 기존 코드: lateinit var filePath: String
     var filePath: String=""

    // 이미지와 내용이 모두 변경되는 경우, 이미지만 변경되는 경우, 내용만 변경되는 경우인 총 3가지 경우로
    // 나누어서 파이어베이스의 스토어와 스토리지에 변경된 내용으로 저장되도록 하는 코드
    private var docId: String? = null

//    // Activity Result API를 사용하기 위한 런처
//    // 갤러리에서 이미지를 선택하기 위한 requestLauncher를 설정
//    private val requestLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                result.data?.data?.let { uri ->
//                    handleImageSelection(uri)
//                }
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 데이터 추출
        val docId = intent.getStringExtra("DocId")
        val title = intent.getStringExtra("CommunityTitle")
        val content = intent.getStringExtra("CommunityContent")
        val date = intent.getStringExtra("CommunityDate")

        // UI에 데이터 설정
        binding.CommunityDate.text = date
        binding.regTitle.setText(title)
        binding.regContent.setText(content)


//        // "사진 변경" 버튼 클릭 시 갤러리에서 사진을 선택할 수 있도록 하는 코드
//
//        binding.regImageUpdateBtn.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*"
//            )
//            requestLauncher.launch(intent)
//
//        }

//        // 이미지와 내용이 모두 변경되는 경우, 이미지만 변경되는 경우, 내용만 변경되는 경우인 총 3가지 경우로
//        // 나누어서 파이어베이스의 스토어와 스토리지에 변경된 내용으로 저장되도록 하는 코드
//        // 파이어베이스에 등록된 디테일 뷰와 동일한 사진 가져오는 관련 코드
//        // 등록한 이미지 가져 오기
//        val imgRef = MyApplication.storage.reference.child("images/${docId}.jpg")
//        imgRef.downloadUrl.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Glide.with(this)
//                    .load(task.result)
//                    .into(binding.regImageUpdateView)
//            }
//        }


        // 내용 수정한 부분을 파이어베이스에 반영하는 코드
        // 수정
        // "CommunityModify" 버튼이 클릭되었을 때 실행되는 코드 블록을 정의
        binding.CommunityModify.setOnClickListener {
            val updatedTitle = binding.regTitle.text.toString()
            val updatedContent = binding.regContent.text.toString()

            // 이미지와 내용이 모두 변경되는 경우, 이미지만 변경되는 경우, 내용만 변경되는 경우인 총 3가지 경우로
            // 나누어서 파이어베이스의 스토어와 스토리지에 변경된 내용으로 저장되도록 하는 코드
            if (filePath.isNotBlank()) {
                // 이미지가 변경된 경우
                uploadImageAndData(docId, updatedTitle, updatedContent)
            } else if (updatedTitle != title || updatedContent != content) {
                // 내용만 변경된 경우
                updateDataOnly(docId, updatedTitle, updatedContent)
            } else {
                // 변경된 내용이 없는 경우
                finish()
            }
        }

        // 수정 취소
        binding.CommunityCancel.setOnClickListener {
            finish()
        }
    }


//    // 이미지와 내용이 모두 변경되는 경우, 이미지만 변경되는 경우, 내용만 변경되는 경우인 총 3가지 경우로
//    // 나누어서 파이어베이스의 스토어와 스토리지에 변경된 내용으로 저장되도록 하는 코드
//    // 갤러리 열기
//    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.setDataAndType(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            "image/*"
//        )
//        requestLauncher.launch(intent)
//    }

//    // 갤러리에서 선택한 이미지 처리
//    private fun handleImageSelection(uri: Uri) {
//        Glide.with(applicationContext)
//            .load(uri)
//            .into(binding.regImageUpdateView)
//
//        filePath = uri.toString()
//    }


    // 이미지와 내용이 모두 변경되는 경우, 이미지만 변경되는 경우, 내용만 변경되는 경우인 총 3가지 경우로
    // 나누어서 파이어베이스의 스토어와 스토리지에 변경된 내용으로 저장되도록 하는 코드
    private fun uploadImageAndData(docId: String?, title: String, content: String) {
        if (docId != null) {
            // 이미지 업로드
            val imageRef = MyApplication.storage.reference.child("images/${docId}.jpg")
            val uploadTask = imageRef.putFile(Uri.parse(filePath))

            uploadTask.addOnSuccessListener {
                // 이미지 업로드 성공 시 데이터 업데이트
                val data = mapOf(
                    "title" to title,
                    "content" to content,
                    "date" to dateToString(Date())
                )
                MyApplication.db.collection("Communities").document(docId).update(data)
                finish()
            }.addOnFailureListener {e ->
                // 이미지 업로드 실패 시 처리 (예: Toast 메시지 출력 등)
                Log.e("CommUpdateActivity", "업로드 및 업데이트 실패: ${e.message}", e)
            }
        }
    }

    // 내용만 변경된 경우 데이터 업데이트
    private fun updateDataOnly(docId: String?, title: String, content: String) {
        if (docId != null) {
            val data = mapOf(
                "title" to title,
                "content" to content,
                "date" to dateToString(Date())
            )
            MyApplication.db.collection("Communities").document(docId).update(data)

                .addOnSuccessListener {
                    // 내용만 변경된 경우에 대한 성공 처리
                    finish()
                }
                .addOnFailureListener { e ->
                    // 내용만 변경된 경우에 대한 실패 처리 (예: Toast 메시지 출력 등)
                    Log.e("CommUpdateActivity", "업데이트 실패: ${e.message}", e)
                }
        }
    }
}

