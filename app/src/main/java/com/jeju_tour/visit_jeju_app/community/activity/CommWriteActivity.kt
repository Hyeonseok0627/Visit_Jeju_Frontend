package com.jeju_tour.visit_jeju_app.community.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jeju_tour.visit_jeju_app.MainActivity
import com.jeju_tour.visit_jeju_app.MyApplication
import com.jeju_tour.visit_jeju_app.MyApplication.Companion.db
import com.jeju_tour.visit_jeju_app.MyApplication.Companion.storage
import com.jeju_tour.visit_jeju_app.R
import com.jeju_tour.visit_jeju_app.accommodation.AccomActivity
import com.jeju_tour.visit_jeju_app.chat.ChatActivity
import com.jeju_tour.visit_jeju_app.community.dateToString
import com.jeju_tour.visit_jeju_app.databinding.ActivityCommWriteBinding
import com.jeju_tour.visit_jeju_app.festival.FesActivity
import com.jeju_tour.visit_jeju_app.gpt.GptActivity
import com.jeju_tour.visit_jeju_app.login.AuthActivity
import com.jeju_tour.visit_jeju_app.restaurant.ResActivity
import com.jeju_tour.visit_jeju_app.shopping.ShopActivity
import com.jeju_tour.visit_jeju_app.tour.TourActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.Date

class CommWriteActivity : AppCompatActivity() {
    lateinit var binding : ActivityCommWriteBinding

    // 공통 메인 레이아웃 적용 코드
    //액션버튼 토글 설정 코드
    // ActionBarDrawerToggle은 액션바에 가로줄 3개 아이콘 및 네비게이션 드로어를 제어하는 데 사용되는 클래스
    lateinit var toggle: ActionBarDrawerToggle

    // 선택된 이미지 파일 경로를 저장하는 변수
    lateinit var filePath: String

    // 디테일 뷰 중 작성자에 해당 커뮤니티 작성 이메일 불러오는 코드
    // 작성자의 이메일을 저장하는 변수
    lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //(공통 레이아웃 코드)
        // 네비게이션 드로어의 헤더 뷰에서 이메일과 로그아웃 버튼 등을 초기화
        val headerView = binding.mainDrawerView.getHeaderView(0)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.headerUserEmail)
        val headerLogoutBtn = headerView.findViewById<Button>(R.id.headerLogoutBtn)

        // 로그아웃 버튼에 클릭 리스너 추가
        headerLogoutBtn.setOnClickListener {
            // Firebase에서 로그아웃 로직
            MyApplication.auth.signOut()
            MyApplication.email = null
            // 로그아웃 후 처리 (예: 로그인 화면으로 이동)
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Intent에서 전달된 사용자 이메일을 가져와서 네비게이션 드로어 헤더에 표시
        val userEmail1 = intent.getStringExtra("USER_EMAIL") ?: "No Email"
        headerUserEmail.text = userEmail1


        // 공통 메인 레이아웃 적용 코드
        // 액션바 설정
        setSupportActionBar(binding.toolbar)

        //드로워화면 액션버튼 클릭 시 드로워 화면 나오게 하기
        // ActionBarDrawerToggle 초기화 및 드로워 활성화
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

        // NavigationView 메뉴 아이템 클릭 리스너 설정(공통 레이아웃 코드)
        // 네비게이션 드로어 메뉴 아이템 클릭 시 이벤트 처리
        binding.mainDrawerView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accommodation -> {
                    startActivity(Intent(this, AccomActivity::class.java))
                    true
                }
                R.id.restaurant -> {
                    startActivity(Intent(this, ResActivity::class.java))
                    true
                }
                R.id.tour -> {
                    startActivity(Intent(this, TourActivity::class.java))
                    true
                }
                R.id.festival -> {
                    startActivity(Intent(this, FesActivity::class.java))
                    true
                }
                R.id.shopping -> {
                    startActivity(Intent(this, ShopActivity::class.java))
                    true
                }
                R.id.community -> {
                    // '커뮤니티' 메뉴 아이템 클릭 시 CommReadActivity로 이동
                    startActivity(Intent(this, CommReadActivity::class.java))
                    true
                }
                R.id.chatting -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // 디테일 뷰 중 작성자에 해당 커뮤니티 작성 이메일 불러오는 코드
        // FirebaseAuth를 통해 사용자 이메일 가져오기
        val currentUser = FirebaseAuth.getInstance().currentUser
        userEmail = currentUser?.email ?: "unknown"

        // 게시물 작성 버튼에 클릭 리스너 추가
        binding.postbtn.setOnClickListener {
            val radioButtonUsed = findViewById<RadioButton>(R.id.radioUsed)
            val radioButtonNotUsed = findViewById<RadioButton>(R.id.radioNotused)

            // 카테고리를 파이어베이스에 저장하는 코드
            // 카테고리 라디오 그룹 가져오기
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

            // 게시물 저장 및 이미지 업로드
            saveStore(status, selectedCategory)
        }

//        // 이미지 업로드 버튼에 클릭 리스너 추가
//        binding.upload.setOnClickListener {
//            // 갤러리에서 이미지 선택하는 인텐트 실행
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*"
//            )
//            requestLauncher.launch(intent)
//
//        }

    }//onCreate

    // 함수 구현 ---------------------------------------------------------------------------

    // 툴바의 검색 뷰(공통 레이아웃)
    // onCreateOptionsMenu 메서드: 액션바에 검색 기능 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)

        // 검색 뷰에, 이벤트 추가하기.
        val menuItem = menu?.findItem(R.id.menu_toolbar_search)
        // menuItem 의 형을 SearchView 타입으로 변환, 형변환
        val searchView = menuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                //검색어가 변경시 마다, 실행될 로직을 추가.
                Log.d("kmk","텍스트 변경시 마다 호출 : ${newText} ")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색어가 제출이 되었을 경우, 연결할 로직.
                // 사용자 디비, 검색을하고, 그 결과 뷰를 출력하는 형태.
                Toast.makeText(this@CommWriteActivity,"검색어가 전송됨 : ${query}", Toast.LENGTH_SHORT).show()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    // requestLauncher: 이미지 선택 및 결과 처리를 위한 ActivityResultContracts의 인스턴스
    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode === android.app.Activity.RESULT_OK) {
            // 선택된 이미지를 Glide를 사용하여 ImageView에 로드
            Glide
                .with(applicationContext)
                .load(it.data?.data)
                .apply(RequestOptions().override(250, 200))
                .centerCrop()
                .into(binding.imageView)

            // 선택된 이미지의 파일 경로를 가져와 filePath 변수에 저장
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
    // saveStore 메서드: 커뮤니티 정보를 Firebase에 저장하고 이미지를 업로드하는 메서드
    private fun saveStore(status: String, category: String) {
        // 현재 날짜와 시간을 문자열로 변환
        val timestampString = dateToString(Date())

        // 카테고리를 파이어베이스에 저장하는 코드
        // 작성자 이메일 설정
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "unknown"


        val writerEmail =
            if (binding.radioNotused.isChecked) "비공개"
            else userEmail ?: "unknown" // 사용자 이메일 추가 및 null 체크

        // 커뮤니티 정보를 담는 Map 생성
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

        // "Communities" 컬렉션에 저장하고 저장된 문서의 ID를 얻어옴
        db.collection("Communities")
            .add(data)
            .addOnSuccessListener {
                // 성공적으로 저장되면 이미지 업로드 함수 호출
                uploadImage(it.id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "오류가 발생했습니다!!", Toast.LENGTH_SHORT).show()
            }
    }

    // uploadImage 메서드: 이미지를 Firebase Storage에 업로드하는 메서드
    private fun uploadImage(docId: String) {
        // Firebase 스토리지 참조 생성
        val storageRef = storage.reference
        // 업로드할 이미지의 스토리지 경로 설정
        val imgRef = storageRef.child("images/${docId}.jpg")

        // 선택된 이미지 파일을 Uri 형태로 변환
        val file = Uri.fromFile(File(filePath))
        // 이미지를 업로드하고 성공 또는 실패에 따라 처리
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