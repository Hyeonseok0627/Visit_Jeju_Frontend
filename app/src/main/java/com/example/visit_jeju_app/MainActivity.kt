package com.example.visit_jeju_app


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.Debug.getLocation2
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.visit_jeju_app.MyApplication.Companion.lat
import com.example.visit_jeju_app.MyApplication.Companion.lnt
import com.example.visit_jeju_app.accommodation.AccomActivity
import com.example.visit_jeju_app.accommodation.adapter.AccomAdapter_Main
import com.example.visit_jeju_app.accommodation.model.AccomList
import com.example.visit_jeju_app.community.activity.CommReadActivity
import com.example.visit_jeju_app.chat.ChatMainActivity
import com.example.visit_jeju_app.databinding.ActivityMainBinding
import com.example.visit_jeju_app.festival.FesActivity
import com.example.visit_jeju_app.festival.adapter.FesAdapter_Main
import com.example.visit_jeju_app.festival.model.FesList
import com.example.visit_jeju_app.gpt.GptActivity
import com.example.visit_jeju_app.login.AuthActivity
import com.example.visit_jeju_app.main.adapter.ImageSliderAdapter
import com.example.visit_jeju_app.restaurant.ResActivity
import com.example.visit_jeju_app.restaurant.adapter.ResAdapter_Main
import com.example.visit_jeju_app.restaurant.model.ResList
import com.example.visit_jeju_app.shopping.ShopActivity
import com.example.visit_jeju_app.shopping.adapter.ShopAdapter_Main
import com.example.visit_jeju_app.shopping.model.ShopList
import com.example.visit_jeju_app.tour.TourActivity
import com.example.visit_jeju_app.tour.adapter.TourAdapter_Main
import com.example.visit_jeju_app.tour.model.TourList
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    // 페이징 설정 순서1 lsy
    // 페이징, 레스트로 부터 전달 받을 데이터 저장할 임시 리스트
    lateinit var AccomData: MutableList<AccomList>
    lateinit var ResData: MutableList<ResList>
    lateinit var TourData: MutableList<TourList>
    lateinit var FesData: MutableList<FesList>
    lateinit var ShopData: MutableList<ShopList>

    // 위치 정보 위한 변수 선언 -------------------------------------
    private var fusedLocationClient: FusedLocationProviderClient? = null
    lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var handler: Handler
    private var lastUpdateTimestamp = 0L
    private val updateDelayMillis = 40000
    private val REQUEST_PERMISSION_LOCATION = 10
    // -----------------------------------------------------------

    lateinit var binding: ActivityMainBinding

    //액션버튼 토글
    lateinit var toggle: ActionBarDrawerToggle

    // 메인 비주얼
    lateinit var viewPager_mainVisual: ViewPager2

    // 현재 위치 담아 두는 변수 선언 및 초기화
//    var lat : Double = 33.2541
//    var lnt : Double = 126.5601

    //페이징처리 1
    var accomPage = 0
    var resPage = 0
    var tourPage = 0
    var fesPage = 0
    var shopPage = 0

    private lateinit var accomAdapter_Main: AccomAdapter_Main
    private lateinit var resAdapter_Main: ResAdapter_Main
    private lateinit var tourAdapter_Main: TourAdapter_Main
    private lateinit var fesAdapter_Main: FesAdapter_Main
    private lateinit var shopAdapter_Main: ShopAdapter_Main

    val TourRecycler: RecyclerView by lazy {
        binding.viewRecyclerTour
    }

    val AccomRecycler: RecyclerView by lazy {
        binding.viewRecyclerAccom
    }

    val ResRecycler: RecyclerView by lazy {
        binding.viewRecyclerRestaurant
    }

    val FesRecycler: RecyclerView by lazy {
        binding.viewRecyclerFestival
    }

    val ShopRecycler: RecyclerView by lazy {
        binding.viewRecyclerShopping
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 카테고리 버튼 참조 및 OnClickListener 설정
        findViewById<Button>(R.id.accommodationCategoryButton).setOnClickListener {
            scrollToSection(R.id.viewRecyclerAccom)
        }

        findViewById<Button>(R.id.restaurantCategoryButton).setOnClickListener {
            scrollToSection(R.id.viewRecyclerRestaurant)
        }

        findViewById<Button>(R.id.tourCategoryButton).setOnClickListener {
            scrollToSection(R.id.viewRecyclerTour)
        }

        findViewById<Button>(R.id.shoppingCategoryButton).setOnClickListener {
            scrollToSection(R.id.viewRecyclerShopping)
        }

        findViewById<Button>(R.id.festivalCategoryButton).setOnClickListener {
            scrollToSection(R.id.viewRecyclerFestival)
        }

        handler = Handler(Looper.getMainLooper())
        Log.d("lsy","Handler   Looper.getMainLooper() =====================================")

        // 투어 어댑터 초기화 및 설정
        tourAdapter_Main = TourAdapter_Main(this, mutableListOf())
        binding.viewRecyclerTour.adapter = tourAdapter_Main
        Log.d("lsy"," binding.viewRecyclerTour.adapter = tourAdapter_Main =====================================")

        // 숙박 어댑터 초기화 및 설정?
        accomAdapter_Main = AccomAdapter_Main(this, mutableListOf())
        binding.viewRecyclerAccom.adapter = accomAdapter_Main
        Log.d("lsy","    binding.viewRecyclerAccom.adapter = accomAdapter_Main =====================================")

        // 레스토랑 어댑터 초기화 및 설정?
        resAdapter_Main = ResAdapter_Main(this, mutableListOf())
        binding.viewRecyclerRestaurant.adapter = resAdapter_Main
        Log.d("lsy","    binding.viewRecyclerRestaurant.adapter = resAdapter_Main =====================================")

        // 페스티벌 어댑터 초기화 및 설정?
        fesAdapter_Main = FesAdapter_Main(this, mutableListOf())
        binding.viewRecyclerFestival.adapter = fesAdapter_Main
        Log.d("lsy","    binding.viewRecyclerFestival.adapter = fesAdapter_Main =====================================")

        // 쇼핑 어댑터 초기화 및 설정?
        shopAdapter_Main = ShopAdapter_Main(this, mutableListOf())
        binding.viewRecyclerShopping.adapter = shopAdapter_Main
        Log.d("lsy","    binding.viewRecyclerShopping.adapter = shopAdapter_Main =====================================")

        // 페이징 설정 순서2 lsy
        AccomData = mutableListOf<AccomList>()
        ResData = mutableListOf<ResList>()
        TourData = mutableListOf<TourList>()
        FesData = mutableListOf<FesList>()
        ShopData = mutableListOf<ShopList>()


        // 공유 프리퍼런스 파일이 존재하는지 확인
        val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
        val lat: Double? = pref.getString("lat", null)?.toDoubleOrNull()
        val lnt: Double? = pref.getString("lnt", null)?.toDoubleOrNull()

        Log.d("ljs", "공유 프리퍼런스 lat: $lat, lnt: $lnt")

        // 공유 프리퍼런스 파일이 존재하지 않으면 기본값으로 파일 생성
        if (lat == null || lnt == null) {
            val editor = pref.edit()
            editor.putString("lat", "기본위치값")
            editor.putString("lnt", "기본위치값")
            editor.apply()
        }


        // 위치 받아오기 위해 추가 ---------------------------------------------------------
        // 위치 권한 확인 및 요청
        // 순서1, 최초 실행시 권한이 없으로 false , 건너띄고,
        if (checkPermissionForLocation(this)) {
            // 위치 권한이 허용된 경우 위치 요청 초기화 및 업데이트 시작
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            // 1번 호출

            getLocation("Tour")
            Log.d("lsy","        getLocation(\"Tour\") =====================================")
            getLocation("Accom")
            Log.d("lsy","        getLocation(\"Accom\") =====================================")
            getLocation("Res")
            Log.d("lsy","        getLocation(\"Res\") =====================================")
            getLocation("Fes")
            Log.d("lsy","        getLocation(\"Fes\") =====================================")
            getLocation("Shop")
            Log.d("lsy","        getLocation(\"Shop\") =====================================")
            createLocationRequest()
            // 1번 호출
            createLocationCallback()
            startLocationUpdates()
        }
//        else {
//            // 위치 권한이 없는 경우 사용자에게 권한 요청
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_PERMISSION_LOCATION
//            )
//        }


        //---------------------------------------------------------

        val headerView = binding.mainDrawerView.getHeaderView(0)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.headerUserEmail)
        val headerLogoutBtn = headerView.findViewById<Button>(R.id.headerLogoutBtn)

        headerLogoutBtn.setOnClickListener {
            // 로그아웃 로직
            MyApplication.auth.signOut()
            MyApplication.email = null
            // 로그아웃 후 처리 (예: 로그인 화면으로 이동)
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "No Email"
        headerUserEmail.text = userEmail


        setSupportActionBar(binding.toolbar)

        //드로워화면 액션버튼 클릭 시 드로워 화면 나오게 하기
        toggle =
            ActionBarDrawerToggle(
                this@MainActivity,
                binding.drawerLayout,
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
                R.id.accommodation -> {
                    val intent = Intent(this, AccomActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.restaurant -> {
                    val intent = Intent(this, ResActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.tour -> {
                    val intent = Intent(this, TourActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.festival -> {
                    val intent = Intent(this, FesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.shopping -> {
                    val intent = Intent(this, ShopActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.community -> {
                    val intent = Intent(this, CommReadActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                R.id.chatting -> {
                    val intent = Intent(this, ChatMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // Bottom Navigation link
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.home -> {
                    // 홈 아이템 클릭 처리
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
                    startActivity(intent)
                    true
                }
                R.id.chat -> {
                    val intent = Intent(this@MainActivity, GptActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
                    startActivity(intent)
                    true
                }
                R.id.youtube -> {
                    val webpageUrl = "https://www.youtube.com/c/visitjeju" // 웹 페이지 링크
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webpageUrl))
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
                    startActivity(intent)
                    true
                }
                R.id.instagram -> {
                    val webpageUrl = "https://www.instagram.com/visitjeju.kr" // 웹 페이지 링크
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webpageUrl))
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val communityBanner = findViewById<ImageView>(R.id.communityBanner)

        // ImageView를 클릭했을 때 동작하는 이벤트 리스너 추가
        communityBanner.setOnClickListener {
            val intent = Intent(this@MainActivity, CommReadActivity::class.java)
            this@MainActivity.startActivity(intent)
        }

        // 메인 카테고리 더보기 링크
        val moreAccomTextView: TextView = findViewById(R.id.mainItemMoreBtn1)
        val moreRestaurantTextView: TextView = findViewById(R.id.mainItemMoreBtn2)
        val moreTourTextView: TextView = findViewById(R.id.mainItemMoreBtn3)
        val moreFestivalTextView: TextView = findViewById(R.id.mainItemMoreBtn4)
        val moreShoppingTextView: TextView = findViewById(R.id.mainItemMoreBtn5)

        // 각 "더보기" 텍스트 뷰에 클릭 리스너를 추가
        moreAccomTextView.setOnClickListener {
            // 제주 숙박 더보기 클릭 시 수행할 동작
            val intent = Intent(this, AccomActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
            startActivity(intent)
        }

        moreRestaurantTextView.setOnClickListener {
            val intent = Intent(this, ResActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
            startActivity(intent)
        }

        moreTourTextView.setOnClickListener {
            val intent = Intent(this, TourActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
            startActivity(intent)
        }

        moreFestivalTextView.setOnClickListener {
            val intent = Intent(this, FesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
            startActivity(intent)
        }

        moreShoppingTextView.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // 액티비티 새로 생성 방지
            startActivity(intent)
        }

        // 메인 비주얼
        viewPager_mainVisual = findViewById(R.id.viewPager_mainVisual)
        viewPager_mainVisual.adapter = ImageSliderAdapter(getMainvisual()) // 어댑터 생성
        viewPager_mainVisual.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로

        val NUM_PAGES = 4 // 전체 페이지 수
        var currentPage = 0

        // 자동 스크롤을 위한 Handler 생성
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                currentPage = (currentPage + 1) % NUM_PAGES // 다음 페이지로 이동
                viewPager_mainVisual.setCurrentItem(currentPage, true) // 다음 페이지로 슬라이드

                handler.postDelayed(this, 3000) // 3초 후에 다음 페이지로 이동
            }
        }
        // 자동 스크롤 시작
        handler.postDelayed(runnable, 3000) // 3초 후에 첫 번째 페이지로 이동


    } //Todo onCreate 끝

    // 싱글탑으로 메인 액티비티 재사용 시, 호출되는 데이터가 새로 반영되도록 하는 코드
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // 새 인텐트 설정

        // 예시: 로그를 찍는 것으로 시작합니다.
        Log.d("lsy", "onNewIntent 호출됨")

        // 예를 들어, 사용자의 위치 데이터를 새로고침하는 메서드 호출
        getLocation("Tour")
        getLocation("Accom")
        getLocation("Res")
        getLocation("Fes")
        getLocation("Shop")

    }

    // 위치 데이터 획득 추가 ---------------------------------------------------------
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 40000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!
    }

    // 안드로이드 기기에서 위,경도 받아오는 메서드
    @SuppressLint("MissingPermission")
    private fun getLocation(dataType: String) {
        Log.d("ljs", "getLocation: Fetching location for $dataType")
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    lat = it.latitude
                    lnt = it.longitude
                    Log.d("ljs", "현재위치 ${lat}, ${lnt}")
                    // 위치 정보를 SharedPreferences에 저장
                    saveLocationToSharedPreferences(lat, lnt)

                    val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                    val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                    val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                    Log.d("ljs", "SharedPreferences에 현재위치 불러오기 ${lat}, ${lnt}")

                    // 서버에 데이터 요청
                    when (dataType) {

                        "Tour" -> fetchTourData(lat, lnt, tourPage)
                        "Accom" -> fetchAccomData(lat, lnt, accomPage)
                        "Res" -> fetchResData(lat, lnt, resPage)
                        "Fes" -> fetchFesData(lat, lnt, fesPage)
                        "Shop" -> fetchShopData(lat, lnt, shopPage)
                        // 기타 데이터 유형에 대한 처리를 추가할 수 있음
                    }
                }
            }
            .addOnFailureListener {
                Log.d("ljs", "현재 위치 조회 실패")
            }
    }

    private fun saveLocationToSharedPreferences(lat: Double?, lnt: Double?) {
        Log.d("ljs", "[원하는 실행 순서 1]")
        Log.d("ljs", "SharedPreferences에 현재위치 저장하기 ${lat}, ${lnt}")
        // 앱 전체에서 위, 경도 값을 사용할 수 있도록 SharedPreferences 사용
        val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString("lat", "${lat}")
        editor.putString("lnt", "${lnt}")
        editor.commit()
    }

    private fun fetchTourData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Tour 데이터 요청 로직
        // 예: networkService.getTourGPS(lat, lnt, page) 호출 및 처리
        Log.d("lsy", "getLocation(Tour) 실행 됨 #######################")
        val tourListCall = (applicationContext as MyApplication).networkService.getTourGPS(lat, lnt, 4.5, tourPage)
        tourListCall.enqueue(object : Callback<MutableList<TourList>> {
            override fun onResponse(
                call: Call<MutableList<TourList>>,
                response: Response<MutableList<TourList>>

            )
            {

                // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
                val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                Log.d("ljs", "[원하는 실행 순서 2]")
                Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendTourLocationToServer1(lat,lnt,tourPage)

                // 이전 위치 정보의 페이지1부터 내용이 불러오던 것을 수정완료
                // (sendTourLocationToServer2 이것을 onCreate() 밖으로 빼서 sendTourLocationToServer 밑에 넣어 이것 다음으로 시작하도록 설계)
                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.viewRecyclerTour.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            tourPage++ // 페이지 번호 증가
                            sendTourLocationToServer2(lat, lnt, tourPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lsy", "Requesting tourPage 확인1: $tourPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<TourList>>, t: Throwable) {
                Log.d("ljs", "fail")
                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }

    private fun fetchAccomData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Accom 데이터 요청 로직
        // 예: networkService.getAccomGPS(lat, lnt, page) 호출 및 처리
        Log.d("lsy", "getLocation(Accom) 실행 됨 #######################")
        val accomListCall = (applicationContext as MyApplication).networkService.getAccomGPS(lat, lnt, 4.5, accomPage)
        accomListCall.enqueue(object : Callback<MutableList<AccomList>> {
            override fun onResponse(
                call: Call<MutableList<AccomList>>,
                response: Response<MutableList<AccomList>>

            )
            {

                // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
                val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                Log.d("ljs", "[원하는 실행 순서 2]")
                Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendAccomLocationToServer1(lat,lnt,accomPage)

                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.viewRecyclerAccom.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            accomPage++ // 페이지 번호 증가
                            sendAccomLocationToServer2(lat, lnt, accomPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lsy", "Requesting accomPage 확인1: $accomPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<AccomList>>, t: Throwable) {
                Log.d("ljs", "fail")
                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }

    private fun fetchResData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Accom 데이터 요청 로직
        // 예: networkService.getAccomGPS(lat, lnt, page) 호출 및 처리
        Log.d("lsy", "getLocation(Accom) 실행 됨 #######################")
        val resListCall = (applicationContext as MyApplication).networkService.getResGPS(lat, lnt, 4.5, resPage)
        resListCall.enqueue(object : Callback<MutableList<ResList>> {
            override fun onResponse(
                call: Call<MutableList<ResList>>,
                response: Response<MutableList<ResList>>

            )
            {

                // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
                val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                Log.d("ljs", "[원하는 실행 순서 2]")
                Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendResLocationToServer1(lat,lnt,resPage)

                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.viewRecyclerRestaurant.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            resPage++ // 페이지 번호 증가
                            sendResLocationToServer2(lat, lnt, resPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lsy", "Requesting accomPage 확인1: $resPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<ResList>>, t: Throwable) {
                Log.d("ljs", "fail")
                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }

    // fetchfesData 추가

    private fun fetchFesData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Accom 데이터 요청 로직
        // 예: networkService.getAccomGPS(lat, lnt, page) 호출 및 처리
        Log.d("lsy", "getLocation(Accom) 실행 됨 #######################")
        val fesListCall = (applicationContext as MyApplication).networkService.getFesGPS(lat, lnt, 4.5, fesPage)
        fesListCall.enqueue(object : Callback<MutableList<FesList>> {
            override fun onResponse(
                call: Call<MutableList<FesList>>,
                response: Response<MutableList<FesList>>

            )
            {

                // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
                val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                Log.d("ljs", "[원하는 실행 순서 2]")
                Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendFesLocationToServer1(lat,lnt,fesPage)

                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.viewRecyclerFestival.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            fesPage++ // 페이지 번호 증가
                            sendFesLocationToServer2(lat, lnt, fesPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lsy", "Requesting accomPage 확인1: $fesPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<FesList>>, t: Throwable) {
                Log.d("ljs", "fail")
                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }

    // fetchShopData 추가

    private fun fetchShopData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Accom 데이터 요청 로직
        // 예: networkService.getAccomGPS(lat, lnt, page) 호출 및 처리
        Log.d("lsy", "getLocation(Accom) 실행 됨 #######################")
        val shopListCall = (applicationContext as MyApplication).networkService.getShopGPS(lat, lnt, 4.5, shopPage)
        shopListCall.enqueue(object : Callback<MutableList<ShopList>> {
            override fun onResponse(
                call: Call<MutableList<ShopList>>,
                response: Response<MutableList<ShopList>>

            )
            {

                // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
                val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                Log.d("ljs", "[원하는 실행 순서 2]")
                Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendShopLocationToServer1(lat,lnt,shopPage)

                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.viewRecyclerShopping.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            shopPage++ // 페이지 번호 증가
                            sendShopLocationToServer2(lat, lnt, shopPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lsy", "Requesting accomPage 확인1: $shopPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<ShopList>>, t: Throwable) {
                Log.d("ljs", "fail")
                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }


    private fun sendTourLocationToServer1(lat: Double?, lnt: Double?, tourPage: Int?) {
        Log.d("lsy", "sendTourLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Tour", lat, lnt, tourPage)
        Log.d("lsy", "sendTourLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }

    // 숙박 데이터 요청
    private fun sendAccomLocationToServer1(lat: Double?, lnt: Double?, accomPage: Int?) {
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Accom", lat, lnt, accomPage)
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }

    private fun sendResLocationToServer1(lat: Double?, lnt: Double?, resPage: Int?) {
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Res", lat, lnt, resPage)
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }

    private fun sendFesLocationToServer1(lat: Double?, lnt: Double?, fesPage: Int?) {
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Fes", lat, lnt, fesPage)
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }

    private fun sendShopLocationToServer1(lat: Double?, lnt: Double?, shopPage: Int?) {
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Shop", lat, lnt, shopPage)
        Log.d("lsy", "sendAccomLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }

    private fun sendTourLocationToServer2(lat: Double?, lnt: Double?, tourPage: Int?) {
        Log.d("lsy", "sendTourLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Tour", lat, lnt, tourPage)
    }

    private fun sendAccomLocationToServer2(lat: Double?, lnt: Double?, accomPage: Int?) {
        Log.d("lsy", "sendAccomLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Accom", lat, lnt, accomPage)
    }

    private fun sendResLocationToServer2(lat: Double?, lnt: Double?, resPage: Int?) {
        Log.d("lsy", "sendResLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Res", lat, lnt, resPage)
    }

    private fun sendFesLocationToServer2(lat: Double?, lnt: Double?, fesPage: Int?) {
        Log.d("lsy", "sendFesLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Fes", lat, lnt, fesPage)
    }

    private fun sendShopLocationToServer2(lat: Double?, lnt: Double?, shopPage: Int?) {
        Log.d("lsy", "sendShopLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Shop", lat, lnt, shopPage)
    }


    // -----------------------------------------------------------------------------


    // Todo 확인 포인트 sendLocationTourToServer(lat, lnt)를 실행
    private fun createLocationCallback() {
        Log.d("lsy","createLocationCallback =====================================")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // 여기서 위치 정보를 사용하세요.
                    val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                    val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                    val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                    Log.d("lsy","createLocationCallback =2====================================")
                    // Todo 확인 포인트
                    // 백에서 데이터 중복으로 불러오는 부분 주석 처리
//                        sendLocationTourToServer(lat, lnt, page)
                    Log.d("lsy","createLocationCallback ==3===================================")
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------

    // 위치 정보 업데이트 ---------------------------------------------------------------------
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        Log.d("lsy","startLocationUpdates =====================================")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()

//            null
        )
    }
    //-----------------------------------------------------------------------------------------



    // 위치 정보 업데이트 중지 -------------------------------------------------------------------------
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        getLocation("Tour")
        getLocation("Accom")
        getLocation("Res")
        getLocation("Fes")
        getLocation("Shop")
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }
    //-------------------------------------------------------------------------------------------


    // 뷰 페이저에 들어갈 아이템
    private fun getMainvisual(): ArrayList<Int> {
        return arrayListOf<Int>(
            R.drawable.jeju_apec02,
            R.drawable.jeju_apec03,
            R.drawable.jeju_apec04,
            R.drawable.jeju_apec01,)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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
                Toast.makeText(this@MainActivity,"검색어가 전송됨 : ${query}", Toast.LENGTH_SHORT).show()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
//                Log.d("ljs","위치 권한 허용이 필요합니다. ")
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 위치 업데이트 시작
                startLocationUpdates()
            } else {
                // 권한이 거부된 경우 사용자에게 메시지 표시 또는 다른 조치 수행
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // 핸들러 메시지 제거
    }

    override fun onBackPressed() {
        // 여기에 뒤로가기 버튼을 눌렀을 때의 로직을 구현합니다.
        if (isTaskRoot) {
            AlertDialog.Builder(this)
                .setMessage("앱을 종료하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예") { _, _ ->
                    super.onBackPressed() // '예'를 선택한 경우, 앱 종료
                }
                .setNegativeButton("아니요", null) // '아니요'를 선택한 경우, 아무것도 하지 않음
                .show()
        } else {
            super.onBackPressed() // 다른 액티비티가 스택에 있으면, 이전 화면으로 이동
        }
    }

    // 네트워크 호출과 응답 처리 부분을 별도의 제네릭 함수로 분리
    private fun <T> fetchAndProcessData(
        call: Call<MutableList<T>>,
        processData: (MutableList<T>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        call.enqueue(object : Callback<MutableList<T>> {
            override fun onResponse(
                call: Call<MutableList<T>>,
                response: Response<MutableList<T>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { processData(it) }
                }
            }

            override fun onFailure(call: Call<MutableList<T>>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    // fetchLocationData 공통
    private fun fetchLocationData(type: String, lat: Double?, lnt: Double?, page: Int?) {
        val networkService = (applicationContext as MyApplication).networkService

        val processData: (Any) -> Unit = { dataList ->
            when (type) {
                "Tour" -> {
                    (dataList as? MutableList<TourList>)?.let { tourList ->
                        TourData.addAll(tourList)
                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d("ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendTourLocationToServer1()에 의해 실행 ")
                        Log.d("lsy", "getTourGPS로 불러온 tourList 값 : ${tourList}")
                        Log.d("lsy", "getTourGPS로 불러온 tourList 사이즈 : ${tourList.size}")
                        Log.d("lsy", "통신 후 받아온 tourList 길이 값 : ${tourList.size}")
                        Log.d("lsy", "Requesting tourPage 확인2: $page")

                        val tourLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerTour.layoutManager = tourLayoutManager
                        binding.viewRecyclerTour.adapter = TourAdapter_Main(this@MainActivity, TourData)
                    }
                }
                "Accom" -> {
                    (dataList as? MutableList<AccomList>)?.let { accomList ->
                        AccomData.addAll(accomList)
                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d("ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendAccomLocationToServer1()에 의해 실행 ")
                        Log.d("lsy", "getAccomGPS로 불러온 accomList 값 : ${accomList}")
                        Log.d("lsy", "getAccomGPS로 불러온 accomList 사이즈 : ${accomList.size}")
                        Log.d("lsy", "통신 후 받아온 accomList 길이 값 : ${accomList.size}")
                        Log.d("lsy", "Requesting accomPage 확인2: $page")

                        val accomLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerAccom.layoutManager = accomLayoutManager
                        binding.viewRecyclerAccom.adapter = AccomAdapter_Main(this@MainActivity, AccomData)
                    }
                }
                "Res" -> {
                    (dataList as? MutableList<ResList>)?.let { resList ->
                        ResData.addAll(resList)
                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d("ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendResLocationToServer1()에 의해 실행 ")
                        Log.d("lsy", "getResGPS로 불러온 resList 값 : ${resList}")
                        Log.d("lsy", "getResGPS로 불러온 resList 사이즈 : ${resList.size}")
                        Log.d("lsy", "통신 후 받아온 resList 길이 값 : ${resList.size}")
                        Log.d("lsy", "Requesting resPage 확인2: $page")

                        val resLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerRestaurant.layoutManager = resLayoutManager
                        binding.viewRecyclerRestaurant.adapter = ResAdapter_Main(this@MainActivity, ResData)
                    }
                }
                "Fes" -> {
                    (dataList as? MutableList<FesList>)?.let { fesList ->
                        FesData.addAll(fesList)
                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d("ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendFesLocationToServer1()에 의해 실행 ")
                        Log.d("lsy", "getFesGPS로 불러온 fesList 값 : ${fesList}")
                        Log.d("lsy", "getFesGPS로 불러온 fesList 사이즈 : ${fesList.size}")
                        Log.d("lsy", "통신 후 받아온 fesList 길이 값 : ${fesList.size}")
                        Log.d("lsy", "Requesting fesPage 확인2: $page")

                        val fesLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerFestival.layoutManager = fesLayoutManager
                        binding.viewRecyclerFestival.adapter = FesAdapter_Main(this@MainActivity, FesData)
                    }
                }
                "Shop" -> {
                    (dataList as? MutableList<ShopList>)?.let { shopList ->
                        ShopData.addAll(shopList)
                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d("ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendShopLocationToServer1()에 의해 실행 ")
                        Log.d("lsy", "getShopGPS로 불러온 shopList 값 : ${shopList}")
                        Log.d("lsy", "getShopGPS로 불러온 shopList 사이즈 : ${shopList.size}")
                        Log.d("lsy", "통신 후 받아온 shopList 길이 값 : ${shopList.size}")
                        Log.d("lsy", "Requesting shopPage 확인2: $page")

                        val shopLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerShopping.layoutManager = shopLayoutManager
                        binding.viewRecyclerShopping.adapter = ShopAdapter_Main(this@MainActivity, ShopData)
                    }
                }
            }
        }


        val onFailure: (Throwable) -> Unit = { t ->
            Log.d("ljs", "[원하는 실행 순서 3] $type List 통신 : fail, Error: $t")
        }

        when (type) {
            "Tour" -> {
                val tourGPSCall = networkService.getTourGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(tourGPSCall, processData, onFailure)
            }
            "Accom" -> {
                val accomGPSCall = networkService.getAccomGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(accomGPSCall, processData, onFailure)
            }
            "Res" -> {
                val resGPSCall = networkService.getResGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(resGPSCall, processData, onFailure)
            }
            "Fes" -> {
                val fesGPSCall = networkService.getFesGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(fesGPSCall, processData, onFailure)
            }
            "Shop" -> {
                val shopGPSCall = networkService.getShopGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(shopGPSCall, processData, onFailure)
            }
        }
    }

    // 네트워크 호출과 응답 처리 부분을 별도의 제네릭 함수로 분리
    private fun <T> fetchAndProcessLocationData(
        call: Call<MutableList<T>>,
        onSuccess: (MutableList<T>) -> Unit
    ) {
        call.enqueue(object : Callback<MutableList<T>> {
            override fun onResponse(
                call: Call<MutableList<T>>,
                response: Response<MutableList<T>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                }
            }

            override fun onFailure(call: Call<MutableList<T>>, t: Throwable) {
                Log.d("ljs", "[요청 실패] : $t")
                call.cancel()
            }
        })
    }
    // fetchLocationData2 공통
    private fun fetchLocationData2(type: String, lat: Double?, lnt: Double?, page: Int?) {
        val networkService = (applicationContext as MyApplication).networkService
        when (type) {
            "Tour" -> {
                val tourGPSCall = networkService.getTourGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(tourGPSCall) { newData ->
                    updateData(newData, TourData, TourRecycler)
                }
            }
            "Accom" -> {
                val accomGPSCall = networkService.getAccomGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(accomGPSCall) { newData ->
                    updateData(newData, AccomData, AccomRecycler)
                }
            }
            "Res" -> {
                val resGPSCall = networkService.getResGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(resGPSCall) { newData ->
                    updateData(newData, ResData, ResRecycler)
                }
            }
            "Fes" -> {
                val fesGPSCall = networkService.getFesGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(fesGPSCall) { newData ->
                    updateData(newData, FesData, FesRecycler)
                }
            }
            "Shop" -> {
                val shopGPSCall = networkService.getShopGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(shopGPSCall) { newData ->
                    updateData(newData, ShopData, ShopRecycler)
                }
            }
        }
    }

    // it : 새로 불러온 데이터
    // TourData : 기존 데이터 리스트
    fun <T> updateData(
        newData: MutableList<T>?,
        existingData: MutableList<T>?,
        recycler: RecyclerView
    ) {
        Log.d("lsy","updateData 함수 호출 시작.")
        Log.d("lsy","updateData 함수 호출 시작2.datasSpring size 값 : ${existingData?.size} ")

        existingData?.size?.let {
            recycler.adapter?.notifyItemInserted(it.minus(1))
        }

        if (existingData != null && newData != null) {
            existingData.addAll(newData)
        }

        recycler.adapter?.notifyDataSetChanged()
    }

    private fun scrollToSection(sectionId: Int) {
        val sectionView = findViewById<View>(sectionId)
        binding.scroll.smoothScrollTo(0, sectionView.top)
    }


}
