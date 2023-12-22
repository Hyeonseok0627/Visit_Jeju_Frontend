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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.Debug.getLocation
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
import com.example.visit_jeju_app.gpt.GptActivity
import com.example.visit_jeju_app.login.AuthActivity
import com.example.visit_jeju_app.main.adapter.ImageSliderAdapter
import com.example.visit_jeju_app.restaurant.ResActivity
import com.example.visit_jeju_app.shopping.ShopActivity
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
    // 투어 액티비티 페이징 처리 코드
    lateinit var pageTour: MutableList<TourList>
    // 숙박 액티비티 페이징 처리 코드
    lateinit var pageAccom: MutableList<AccomList>
    

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
    var Tourpage = 0
    var Accompage = 0

    // 투어 액티비티 페이징 처리 코드
    private lateinit var tourAdapter_Main: TourAdapter_Main
    // 숙박 액티비티 페이징 처리 코드
    private lateinit var accomAdapter_Main: AccomAdapter_Main

    val recycler: RecyclerView by lazy {
        binding.viewRecyclerTour
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())
        Log.d("lsy","Handler   Looper.getMainLooper() =====================================")

        // 투어 액티비티 페이징 처리 코드
        // 투어 어댑터 초기화 및 설정
        tourAdapter_Main = TourAdapter_Main(this, mutableListOf())
        binding.viewRecyclerTour.adapter = tourAdapter_Main

        // 숙박 액티비티 페이징 처리 코드
        accomAdapter_Main = AccomAdapter_Main(this, mutableListOf())
        binding.viewRecyclerTour.adapter = accomAdapter_Main

        
        // 페이징 설정 순서2 lsy
        // 투어 액티비티 페이징 처리 코드
        pageTour = mutableListOf<TourList>()

        // 숙박 액티비티 페이징 처리 코드
        pageAccom = mutableListOf<AccomList>()

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
        // Todo 앱터지는 원인 밑에 4개 중 하나
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        getLocation()
//        createLocationRequest()
//        createLocationJejuCallback()

        // 위치 받아오기 위해 추가 ---------------------------------------------------------
        // 위치 권한 확인 및 요청
        // 순서1, 최초 실행시 권한이 없으로 false , 건너띄고,
        if (checkPermissionForLocation(this)) {
            // 위치 권한이 허용된 경우 위치 요청 초기화 및 업데이트 시작
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            // 1번 호출
            getLocation()
            createLocationRequest()
            // 1번 호출
            createLocationJejuCallback()
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




        // 투어 끝 ====== ======== ========= ========== ========== ========== ========== ========== ================ ======== ========= ========== ========== ========== ========== ========== ======================= ======== ========= ========== ========== ========== ========== ========== ==========

//        // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
//        binding.viewRecyclerTour.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
//                    page++ // 페이지 번호 증가
//                    sendTourLocationToServer2(lat, lnt, page) // 서버에 새 페이지 데이터 요청
//                    Log.d("lsy", "Requesting page 확인1: $page")
//                }
//            }
//        })
    } //onCreate 끝

    // 싱글탑으로 메인 액티비티 재사용 시, 호출되는 데이터가 새로 반영되도록 하는 코드
    // 페이지0만 새로운 위치 반영되고, 페이지1부터는 기존꺼를 그대로 가져오는 것을 해결한 코드
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // 새 인텐트 설정

        // TODO: 여기에서 데이터를 새로고침하거나 UI를 업데이트하는 로직을 추가하세요.
        // 예시: 로그를 찍는 것으로 시작합니다.
        Log.d("lsy", "onNewIntent 호출됨")

        // 예를 들어, 사용자의 위치 데이터를 새로고침하는 메서드 호출
        getLocation()
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
    private fun getLocation() {
        Log.d("lsy","getLocation =====================================")
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
//                    Log.d("ljs", "현재 위치 조회 : lat : ${location.latitude}, lnt : ${location.longitude}")
                    lat = location.latitude
                    lnt = location.longitude
                    Log.d("ljs", "[원하는 실행 순서 1]")
                    Log.d("ljs", "shared 저장 전: 현재 위치 조회 -> lat : ${lat}, lnt : ${lnt}" +
                            " -> getLocation()에 의해 실행")

                    // 앱 전체에서 위, 경도 값을 사용할 수 있도록 SharedPreferences 사용
                    val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                    val editor = pref.edit()

                    editor.putString("lat", "${lat}")
                    editor.putString("lnt", "${lnt}")
                    editor.commit()
//                     위치 정보를 받아온 후, 서버로 전송
//                    sendLocationToServer(lat, lnt)

                    // 투어 액티비티 페이징 처리 코드
                    val tourListCall = (applicationContext as MyApplication).networkService.getTourGPS(lat,lnt,Tourpage)

                    tourListCall.enqueue(object : Callback<MutableList<TourList>> {
                        override fun onResponse(
                            call: Call<MutableList<TourList>>,
                            response: Response<MutableList<TourList>>

                        )
                        {
                            // 메인 비주얼
                            viewPager_mainVisual = findViewById(R.id.viewPager_mainVisual)
                            viewPager_mainVisual.adapter = ImageSliderAdapter(getMainvisual()) // 어댑터 생성
                            viewPager_mainVisual.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로

                            // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
//                getLocation()
                            val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                            val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                            val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                            Log.d("ljs", "[원하는 실행 순서 2]")
                            Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                                    " ->onCreate 안에서 절차대로 실행 ")

                            // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                            sendTourLocationToServer1(lat,lnt,Tourpage)

                            // 이전 위치 정보의 페이지1부터 내용이 불러오던 것을 수정완료
                            // (sendTourLocationToServer2 이것을 onCreate() 밖으로 빼서 sendTourLocationToServer 밑에 넣어 이것 다음으로 시작하도록 설계)
                            // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                            binding.viewRecyclerTour.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                                        Tourpage++ // 페이지 번호 증가
                                        sendTourLocationToServer2(lat, lnt, Tourpage) // 서버에 새 페이지 데이터 요청
                                        Log.d("lsy", "Requesting page 확인1: $Tourpage")
                                    }
                                }
                            })

                        }

                        override fun onFailure(call: Call<MutableList<TourList>>, t: Throwable) {
                            Log.d("ljs", "fail")
                            Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
                        }
                    })

                    // 숙박 액티비티 페이징 처리 코드
                    val accomListCall = (applicationContext as MyApplication).networkService.getAccomGPS(lat,lnt,Accompage)

                    accomListCall.enqueue(object : Callback<MutableList<AccomList>> {
                        override fun onResponse(
                            call: Call<MutableList<AccomList>>,
                            response: Response<MutableList<AccomList>>

                        )
                        {
                            // 메인 비주얼
                            viewPager_mainVisual = findViewById(R.id.viewPager_mainVisual)
                            viewPager_mainVisual.adapter = ImageSliderAdapter(getMainvisual()) // 어댑터 생성
                            viewPager_mainVisual.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로

                            // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
//                getLocation()
                            val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                            val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                            val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                            Log.d("ljs", "[원하는 실행 순서 2]")
                            Log.d("ljs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                                    " ->onCreate 안에서 절차대로 실행 ")

                            // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                            sendAccomLocationToServer1(lat,lnt,Accompage)

                            // 이전 위치 정보의 페이지1부터 내용이 불러오던 것을 수정완료
                            // (sendTourLocationToServer2 이것을 onCreate() 밖으로 빼서 sendTourLocationToServer 밑에 넣어 이것 다음으로 시작하도록 설계)
                            // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                            binding.viewRecyclerAccom.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                                        Accompage++ // 페이지 번호 증가
                                        sendAccomLocationToServer2(lat, lnt, Accompage) // 서버에 새 페이지 데이터 요청
                                        Log.d("lsy", "Requesting page 확인1: $Accompage")
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
            }
            .addOnFailureListener { fail ->
                Log.d("ljs", "현재 위치 조회 실패")
            }
    }



    // 백엔드 서버로 위치 데이터 전송 // -----------------------------------------------------------------------
    // Todo 확인 포인트
    // 투어 액티비티 페이징 처리 코드
    private fun sendTourLocationToServer1(lat: Double?, lnt: Double?, Tourpage: Int?) {
        val networkService = (applicationContext as MyApplication).networkService
        // 투어 액티비티 페이징 처리 코드
        val tourGPSCall = networkService.getTourGPS(lat, lnt, Tourpage)


        // 투어 액티비티 페이징 처리 코드
        tourGPSCall.enqueue(object : Callback<MutableList<TourList>> {
            override fun onResponse(
                call: Call<MutableList<TourList>>,
                response: Response<MutableList<TourList>>
            ) {

                //
                if (response.isSuccessful) {
                    val tourList = response.body()
                    tourList?.let {
                        // 받아온 데이터를 임시로 저장할 리스트를 전역 하나 만들고,
                        // 최초로 5개를 받아와서, 전역에 넣고,
                        // 페이징 되서, 2번째 페이지의 데이터 5개를 받아오면, 그 데이터를
                        // 다시, 전역에 선언한 리스트에 다시 담고
                        // 어댑터에 연결하기, 어댑터 객체에 다시 리스트를 인자로 넣고
                        // 데이터 변경 , 데이터를 추가 했을 때, ->


                        pageTour.addAll(it)
//                        getDataTR(pageTour)
//                        tourAdapter_Main.addData(it)

                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d(
                            "ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                    " -> onCreate 안에서 절차대로 실행2 \n -> sendTourLocationToServer()에 의해 실행 "
                        )

//                val tourList = response.body()

                        Log.d("lsy", "getTourGPS로 불러온 tourList 값 : ${tourList}")
                        Log.d("lsy", "getTourGPS로 불러온 tourList 사이즈 : ${tourList?.size}")
                        Log.d(
                            "lsy", "통신 후 받아온 tourList 길이 값 : ${tourList?.size}"
                        )
                        Log.d("lsy", "Requesting page 확인2: $Tourpage")

                        val currentTime = System.currentTimeMillis()

                        // 일정 시간이 지나지 않았으면 업데이트를 건너뜁니다.
                        if (currentTime - lastUpdateTimestamp < updateDelayMillis) {
                            return
                        }

                        lastUpdateTimestamp = currentTime

                        val tourLayoutManager =
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        binding.viewRecyclerTour.layoutManager = tourLayoutManager
                        binding.viewRecyclerTour.adapter =
                            TourAdapter_Main(this@MainActivity, pageTour)
//                        binding.viewRecyclerTour.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<TourList>>, t: Throwable) {
                Log.d("ljs", "[원하는 실행 순서 3] : fail")
                call.cancel()
            }
        })
    }
        // 숙박 액티비티 페이징 처리 코드
        private fun sendAccomLocationToServer1(lat: Double?, lnt: Double?, Accompage: Int?) {
            val networkService = (applicationContext as MyApplication).networkService

        // 숙박 액티비티 페이징 처리 코드
        val accomGPSCall = networkService.getAccomGPS(lat, lnt, Accompage )

        // 숙박 액티비티 페이징 처리 코드
        accomGPSCall.enqueue(object : Callback<MutableList<AccomList>> {
            override fun onResponse
                        (call: Call<MutableList<AccomList>>,
                         response: Response<MutableList<AccomList>>) {

                //
                if (response.isSuccessful) {
                    val accomList = response.body()
                    accomList?.let {
                        // 받아온 데이터를 임시로 저장할 리스트를 전역 하나 만들고,
                        // 최초로 5개를 받아와서, 전역에 넣고,
                        // 페이징 되서, 2번째 페이지의 데이터 5개를 받아오면, 그 데이터를
                        // 다시, 전역에 선언한 리스트에 다시 담고
                        // 어댑터에 연결하기, 어댑터 객체에 다시 리스트를 인자로 넣고
                        // 데이터 변경 , 데이터를 추가 했을 때, ->


                        pageAccom.addAll(it)
//                        getDataTR(pageTour)
//                        tourAdapter_Main.addData(it)

                        Log.d("ljs", "[원하는 실행 순서 3]")
                        Log.d(
                            "ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                    " -> onCreate 안에서 절차대로 실행2 \n -> sendTourLocationToServer()에 의해 실행 "
                        )

//                val tourList = response.body()

                        Log.d("lsy", "getAccomGPS로 불러온 accomList 값 : ${accomList}")
                        Log.d("lsy", "getAccomGPS로 불러온 accomList 사이즈 : ${accomList?.size}")
                        Log.d(
                            "lsy", "통신 후 받아온 accomList 길이 값 : ${accomList?.size}"
                        )
                        Log.d("lsy", "Requesting page 확인2: $Accompage")

                        val currentTime = System.currentTimeMillis()

                        // 일정 시간이 지나지 않았으면 업데이트를 건너뜁니다.
                        if (currentTime - lastUpdateTimestamp < updateDelayMillis) {
                            return
                        }

                        lastUpdateTimestamp = currentTime

                        val accomLayoutManager =
                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.viewRecyclerAccom.layoutManager = accomLayoutManager
                        binding.viewRecyclerAccom.adapter = AccomAdapter_Main(this@MainActivity, pageAccom)
//                        binding.viewRecyclerTour.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<AccomList>>, t: Throwable) {
                Log.d("ljs", "[원하는 실행 순서 3] : fail")
                call.cancel()
            }
        })


    }

    // 투어 액티비티 페이징 처리 코드
    private fun sendTourLocationToServer2(lat: Double?, lnt: Double?, Tourpage: Int?) {
        val networkService = (applicationContext as MyApplication).networkService
        val tourGPSCall = networkService.getTourGPS(lat, lnt, Tourpage )

        tourGPSCall.enqueue(object : Callback<MutableList<TourList>> {
            override fun onResponse
                        (call: Call<MutableList<TourList>>,
                         response: Response<MutableList<TourList>>) {

                //
                if (response.isSuccessful) {
                    val tourList = response.body()
                    tourList?.let {
                        // 받아온 데이터를 임시로 저장할 리스트를 전역 하나 만들고,
                        // 최초로 5개를 받아와서, 전역에 넣고,
                        // 페이징 되서, 2번째 페이지의 데이터 5개를 받아오면, 그 데이터를
                        // 다시, 전역에 선언한 리스트에 다시 담고
                        // 어댑터에 연결하기, 어댑터 객체에 다시 리스트를 인자로 넣고
                        // 데이터 변경 , 데이터를 추가 했을 때, ->


//                        pageTour.addAll(it)
                        getDataTR(it)
//                        tourAdapter_Main.addData(it)

//                        Log.d("ljs", "[원하는 실행 순서 3]")
//                        Log.d(
//                            "ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
//                                    " -> onCreate 안에서 절차대로 실행2 \n -> sendTourLocationToServer()에 의해 실행 "
//                        )

//                val tourList = response.body()

//                        Log.d("lsy", "getTourGPS로 불러온 tourList 값 : ${tourList}")
//                        Log.d("lsy", "getTourGPS로 불러온 tourList 사이즈 : ${tourList?.size}")
//                        Log.d(
//                            "lsy", "통신 후 받아온 tourList 길이 값 : ${tourList?.size}"
//                        )
//                        Log.d("lsy", "Requesting page 확인2: $page")
//
//                        val currentTime = System.currentTimeMillis()
//
//                        // 일정 시간이 지나지 않았으면 업데이트를 건너뜁니다.
//                        if (currentTime - lastUpdateTimestamp < updateDelayMillis) {
//                            return
//                        }
//
//                        lastUpdateTimestamp = currentTime
//
//                        val tourLayoutManager =
//                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//                        binding.viewRecyclerTour.layoutManager = tourLayoutManager
//                        binding.viewRecyclerTour.adapter = TourAdapter_Main(this@MainActivity, pageTour)
//                        binding.viewRecyclerTour.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<TourList>>, t: Throwable) {
                Log.d("ljs", "[원하는 실행 순서 3] : fail")
                call.cancel()
            }
        })

    }

    // 숙박 액티비티 페이징 처리 코드
    private fun sendAccomLocationToServer2(lat: Double?, lnt: Double?, Accompage: Int?) {
        val networkService = (applicationContext as MyApplication).networkService
        val accomGPSCall = networkService.getAccomGPS(lat, lnt, Accompage )

        accomGPSCall.enqueue(object : Callback<MutableList<AccomList>> {
            override fun onResponse
                        (call: Call<MutableList<AccomList>>,
                         response: Response<MutableList<AccomList>>) {

                //
                if (response.isSuccessful) {
                    val accomList = response.body()
                    accomList?.let {
                        // 받아온 데이터를 임시로 저장할 리스트를 전역 하나 만들고,
                        // 최초로 5개를 받아와서, 전역에 넣고,
                        // 페이징 되서, 2번째 페이지의 데이터 5개를 받아오면, 그 데이터를
                        // 다시, 전역에 선언한 리스트에 다시 담고
                        // 어댑터에 연결하기, 어댑터 객체에 다시 리스트를 인자로 넣고
                        // 데이터 변경 , 데이터를 추가 했을 때, ->


//                        pageTour.addAll(it)
                        getDataAC(it)
//                        tourAdapter_Main.addData(it)

//                        Log.d("ljs", "[원하는 실행 순서 3]")
//                        Log.d(
//                            "ljs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
//                                    " -> onCreate 안에서 절차대로 실행2 \n -> sendTourLocationToServer()에 의해 실행 "
//                        )

//                val tourList = response.body()

//                        Log.d("lsy", "getTourGPS로 불러온 tourList 값 : ${tourList}")
//                        Log.d("lsy", "getTourGPS로 불러온 tourList 사이즈 : ${tourList?.size}")
//                        Log.d(
//                            "lsy", "통신 후 받아온 tourList 길이 값 : ${tourList?.size}"
//                        )
//                        Log.d("lsy", "Requesting page 확인2: $page")
//
//                        val currentTime = System.currentTimeMillis()
//
//                        // 일정 시간이 지나지 않았으면 업데이트를 건너뜁니다.
//                        if (currentTime - lastUpdateTimestamp < updateDelayMillis) {
//                            return
//                        }
//
//                        lastUpdateTimestamp = currentTime
//
//                        val tourLayoutManager =
//                            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//                        binding.viewRecyclerTour.layoutManager = tourLayoutManager
//                        binding.viewRecyclerTour.adapter = TourAdapter_Main(this@MainActivity, pageTour)
//                        binding.viewRecyclerTour.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<AccomList>>, t: Throwable) {
                Log.d("ljs", "[원하는 실행 순서 3] : fail")
                call.cancel()
            }
        })

    }
    
    // -----------------------------------------------------------------------------

    // Todo 확인 포인트 sendLocationTourToServer(lat, lnt)를 실행
    
    private fun createLocationJejuCallback() {
        Log.d("lsy","createLocationJejuCallback =====================================")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // 여기서 위치 정보를 사용하세요.
                    val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                    val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                    val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                    Log.d("lsy","createLocationJejuCallback =2====================================")
                    // Todo 확인 포인트
                    // 백에서 자동 불러오는 거 방지 해결 코드
//                        sendLocationTourToServer(lat, lnt, page)
                    Log.d("lsy","createLocationJejuCallback ==3===================================")
                }
            }
        }
    }
    // -----------------------------------------------------------------------------

    // 백엔드 서버로 위치 데이터 전송 하는 매서드 -----------------------------------------------------------------------
    // Todo 확인 포인트 -> 왜 두번이나 전송?
//    private fun sendLocationTourToServer(lat: Double?, lnt: Double?, page: Int?) {
//        val networkService = (applicationContext as MyApplication).networkService
//        val tourGPSCall = networkService.getTourGPS(lat, lnt, page)
////        val accomGPSCall = networkService.getAccomGPS(lat, lnt )
//
//        tourGPSCall.enqueue(object : Callback<MutableList<TourList>> {
//            override fun onResponse(call: Call<MutableList<TourList>>, response: Response<MutableList<TourList>>) {
//                Log.d("ljs", "[원하는 실행 순서 4]")
//                Log.d("ljs", "현재 위치 업데이트 성공2: lat : ${lat}, lnt : ${lnt}" +
//                        " -> createLocationJejuCallback()에 의해 실행")
//            }
//
//            override fun onFailure(call: Call<MutableList<TourList>>, t: Throwable) {
//                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
//            }
//        })
//
////        accomGPSCall.enqueue(object : Callback<List<AccomList>> {
////            override fun onResponse(call: Call<List<AccomList>>, response: Response<List<AccomList>>) {
////                Log.d("ljs", "[원하는 실행 순서 4]")
////                Log.d("ljs", "현재 위치 업데이트 성공2: lat : ${lat}, lnt : ${lnt}" +
////                        " -> createLocationJejuCallback()에 의해 실행")
////            }
////
////            override fun onFailure(call: Call<List<AccomList>>, t: Throwable) {
////                Log.d("ljs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
////            }
////        })
//    }

    //--------------------------------------------------------------------------------------------
    // 투어 액티비티 페이징 처리 코드
    fun getDataTR(datas1: MutableList<TourList>?) {
        Log.d("lsy","getDataTR 함수 호출 시작.")
        Log.d("lsy","getDataTR 함수 호출 시작2.pageTour size 값 : ${pageTour?.size} ")
        pageTour?.size?.let {
            recycler.adapter?.notifyItemInserted(
                it.minus(1)
            )
        }
        if (pageTour?.size != null){
            pageTour?.addAll(datas1 as Collection<TourList>)
        }
        recycler.adapter?.notifyDataSetChanged()

    }

    // 숙박 액티비티 페이징 처리 코드
    fun getDataAC(datas2: MutableList<AccomList>?) {
        Log.d("lsy","getDataTR 함수 호출 시작.")
        Log.d("lsy","getDataTR 함수 호출 시작2.pageTour size 값 : ${pageAccom?.size} ")
        pageAccom?.size?.let {
            recycler.adapter?.notifyItemInserted(
                it.minus(1)
            )
        }
        if (pageAccom?.size != null){
            pageAccom?.addAll(datas2 as Collection<AccomList>)
        }
        recycler.adapter?.notifyDataSetChanged()

    }

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
        getLocation()
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

    // ... 기타 메서드들 ...
}
