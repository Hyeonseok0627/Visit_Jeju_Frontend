package com.example.visit_jeju_app.community.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.visit_jeju_app.MainActivity
import com.example.visit_jeju_app.MyApplication.Companion.db
import com.example.visit_jeju_app.MyApplication.Companion.storage
import com.example.visit_jeju_app.R
import com.example.visit_jeju_app.chat.ChatActivity
import com.example.visit_jeju_app.community.dateToString
import com.example.visit_jeju_app.databinding.ActivityCommWriteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.Date

class CommWriteActivity : AppCompatActivity() {
    lateinit var binding : ActivityCommWriteBinding

    // 공통 메인 레이아웃 적용 코드
    //액션버튼 토글
    lateinit var toggle: ActionBarDrawerToggle

    lateinit var filePath: String

    // 디테일 뷰 중 작성자에 해당 커뮤니티 작성 이메일 불러오는 코드
    lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 공통 메인 레이아웃 적용 코드
        setSupportActionBar(binding.toolbar)
        //드로워화면 액션버튼 클릭 시 드로워 화면 나오게 하기
        toggle =
            ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,  // 세 번째 매개변수로 툴바 전달
                R.string.open,
                R.string.close
            )

        binding.drawerLayout.addDrawerListener(toggle)
        //화면 적용하기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //버튼 클릭스 동기화 : 드로워 열어주기
        toggle.syncState()
        // NavigationView 메뉴 아이템 클릭 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.community -> {
                    // '커뮤니티' 메뉴 아이템 클릭 시 CommReadActivity로 이동
                    startActivity(Intent(this, CommReadActivity::class.java))
                    true
                }
                // 다른 메뉴 아이템에 대한 처리 추가

                else -> false
            }
        }

        // 디테일 뷰 중 작성자에 해당 커뮤니티 작성 이메일 불러오는 코드
        // FirebaseAuth를 통해 사용자 이메일 가져오기
        val currentUser = FirebaseAuth.getInstance().currentUser
        userEmail = currentUser?.email ?: "unknown"

        binding.postbtn.setOnClickListener {
            val radioButtonUsed = findViewById<RadioButton>(R.id.radioUsed)
            val radioButtonNotUsed = findViewById<RadioButton>(R.id.radioNotused)

            // 카테고리를 파이어베이스에 저장하는 코드
            val categoryRadioGroup = findViewById<RadioGroup>(R.id.categoryRadioGroup) // 추가된 라인

            val status = if (radioButtonUsed.isChecked) {
                "사용"
            } else if (radioButtonNotUsed.isChecked) {
                "비사용"
            } else {
                // 카테고리를 파이어베이스에 저장하는 코드
                Toast.makeText(this, "라디오 버튼을 선택하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategoryId = categoryRadioGroup.checkedRadioButtonId
            val selectedCategory =
                if (selectedCategoryId != -1) findViewById<RadioButton>(selectedCategoryId).text.toString()
                else ""

            saveStore(status, selectedCategory)
        }

        binding.upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            requestLauncher.launch(intent)

        }

    }//onCreate

    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode === android.app.Activity.RESULT_OK) {
            Glide
                .with(applicationContext)
                .load(it.data?.data)
                .apply(RequestOptions().override(250, 200))
                .centerCrop()
                .into(binding.imageView)
            val cursor = contentResolver.query(
                it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null
            )
            cursor?.moveToFirst().let {
                filePath = cursor?.getString(0) as String
            }
        }
    }

    // 카테고리를 파이어베이스에 저장하는 코드
    private fun saveStore(status: String, category: String) {
        // 현재 날짜와 시간을 문자열로 변환
        val timestampString = dateToString(Date())

        // 카테고리를 파이어베이스에 저장하는 코드
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "unknown"


        val writerEmail =
            if (binding.radioNotused.isChecked) "비공개"
            else userEmail ?: "unknown" // 사용자 이메일 추가 및 null 체크

        val data = mapOf(
            "title" to binding.title.text.toString(),
            "content" to binding.addEditView.text.toString(),

            // timestamp형이 아닌 string이면서 "yyyy-MM-dd HH:mm"포맷으로 파이어베이스 저장 및 조회 관련 코드
            "date" to timestampString, // 문자열로 저장

            // 디테일 뷰 중 작성자에 해당 커뮤니티 작성 이메일 불러오는 코드
            // (사용안함 선택 시, 이메일이 아닌 "비공개" 문자열이 파이어베이스에 저장)
            "writerEmail" to writerEmail, // 사용자 이메일 추가
            "status" to status,

            // 카테고리를 파이어베이스에 저장하는 코드
            "category" to category // 추가된 라인

        )
        db.collection("Communities")
            .add(data)
            .addOnSuccessListener {
                uploadImage(it.id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "오류가 발생했습니다!!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(docId: String) {
        val storageRef = storage.reference
        val imgRef = storageRef.child("images/${docId}.jpg")

        val file = Uri.fromFile(File(filePath))
        imgRef.putFile(file)
            .addOnSuccessListener {
                Toast.makeText(this, "업로드 성공했습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "업로드 실패했습니다", Toast.LENGTH_SHORT).show()

            }
    }

}