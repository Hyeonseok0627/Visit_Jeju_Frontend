package com.example.visit_jeju_app.accommodation

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.visit_jeju_app.MainActivity
import com.example.visit_jeju_app.MyApplication
import com.example.visit_jeju_app.MyApplication.Companion.lat
import com.example.visit_jeju_app.MyApplication.Companion.lnt
import com.example.visit_jeju_app.R
import com.example.visit_jeju_app.accommodation.AccomRegionNmActivity
import com.example.visit_jeju_app.accommodation.adapter.AccomAdapter
import com.example.visit_jeju_app.accommodation.model.AccomList
import com.example.visit_jeju_app.chat.ChatMainActivity
import com.example.visit_jeju_app.community.activity.CommReadActivity
import com.example.visit_jeju_app.databinding.ActivityAccomBinding
import com.example.visit_jeju_app.festival.FesActivity
import com.example.visit_jeju_app.festival.model.FesList
import com.example.visit_jeju_app.gpt.GptActivity
import com.example.visit_jeju_app.login.AuthActivity
import com.example.visit_jeju_app.main.adapter.ImageSliderAdapter
import com.example.visit_jeju_app.restaurant.ResActivity
import com.example.visit_jeju_app.restaurant.model.ResList
import com.example.visit_jeju_app.shopping.ShopActivity
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


class AccomActivity : AppCompatActivity() {

    // 페이징 설정 순서1
    // 페이징, 레스트로 부터 전달 받을 데이터 저장할 임시 리스트
    lateinit var AccomData: MutableList<AccomList>

    // 위치 정보 위한 변수 선언 -------------------------------------
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mlocationCallback: LocationCallback
    private lateinit var handler: Handler
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null //현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    private var lastUpdateTimestamp = 0L
    private val updateDelayMillis = 40000
    //리사이클러 뷰 업데이트 딜레이 업데이트 주기 생성

    lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    private var mapX : String = ""
    private var mapY : String= ""
    private var coords: String = ""

    //액션버튼 토글
    lateinit var toggle: ActionBarDrawerToggle

    //페이징처리 1
    var accomPage = 0

    private lateinit var accomAdapter: AccomAdapter

    val AccomRecycler: RecyclerView by lazy {
        binding.recyclerView
    }

    // URL link
    private fun openWebPage(url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


    lateinit var binding: ActivityAccomBinding

    // 서브메인에서 위치변경 없을 시, 백엔드에 데이터 요청 방지
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        // 추가
        binding.pageChange.setOnClickListener {
            val intent = Intent(this@AccomActivity, AccomRegionNmActivity::class.java)
            startActivity(intent)
        }

//        mLocationRequest = LocationRequest.create().apply {
//
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            interval = 30000
//            //초기화 시간 30초
//
//        }
//
//        if (checkPermissionForLocation(this)) {
//            startLocationUpdates()
//
//        }

        // 숙박 어댑터 초기화 및 설정?
        accomAdapter = AccomAdapter(this, mutableListOf())
        binding.recyclerView.adapter = accomAdapter
        Log.d("lhs","    binding.viewRecyclerAccom.adapter = accomAdapter =====================================")

        // LinearLayoutManager를 세로 방향으로 설정
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        // 페이징 설정 순서2 lsy
        AccomData = mutableListOf<AccomList>()

        // 공유 프리퍼런스 파일이 존재하는지 확인
        val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
        val lat: Double? = pref.getString("lat", null)?.toDoubleOrNull()
        val lnt: Double? = pref.getString("lnt", null)?.toDoubleOrNull()

        Log.d("lhs", "공유 프리퍼런스 lat: $lat, lnt: $lnt")

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

            getLocation("Accom")
            Log.d("lsy","        getLocation(\"Accom\") =====================================")
            createLocationRequest()
            // 1번 호출
            createLocationCallback()
            startLocationUpdates()
        }

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

        // 액션바
        setSupportActionBar(binding.toolbar)

        //드로워화면 액션버튼 클릭 시 드로워 화면 나오게 하기
        toggle =
            ActionBarDrawerToggle(this@AccomActivity, binding.drawerLayout, R.string.open, R.string.close)

        binding.drawerLayout.addDrawerListener(toggle)
        //화면 적용하기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //버튼 클릭스 동기화 : 드로워 열어주기
        toggle.syncState()

        // NavigationView 메뉴 아이템 클릭 리스너 설정
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
                    startActivity(Intent(this, ChatMainActivity::class.java))
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
                    val intent = Intent(this@AccomActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.chat -> {
                    val intent = Intent(this@AccomActivity, GptActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.youtube -> {
                    openWebPage("https://www.youtube.com/c/visitjeju")
                    true
                }
                R.id.instagram -> {
                    openWebPage("https://www.instagram.com/visitjeju.kr")
                    true
                }
                else -> false
            }
        }

    }//oncreate

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { onLocationChanged(it) }
        }
    }

    // 서브메인에서 위치변경 없을 시, 백엔드에 데이터 요청 방지
    private fun onLocationChanged(location: Location) {
        if (lastKnownLocation == null || isLocationChanged(location, lastKnownLocation!!)) {
            mLastLocation = location
            lastKnownLocation = location
            val coords = "${mLastLocation.longitude},${mLastLocation.latitude}"
            getAccomListWithinRadius(coords)
        }
    }

    // 서브메인에서 위치변경 없을 시, 백엔드에 데이터 요청 방지
    private fun isLocationChanged(newLocation: Location, lastLocation: Location): Boolean {
        return newLocation.latitude != lastLocation.latitude || newLocation.longitude != lastLocation.longitude
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // 반경 필터링
        val R = 6371.0 // 지구의 반지름 (단위: km)

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    private fun getAccomListWithinRadius(coords: String) {

        val networkService = (applicationContext as MyApplication).networkService
        val accomListCall = networkService.getAccomGPS(lat,lnt,7.0,page)

        accomListCall.enqueue(object : Callback<MutableList<AccomList>> {
            override fun onResponse(
                call: Call<MutableList<AccomList>>,
                accomponse: Response<MutableList<AccomList>>

            ) {
                val accomList = accomponse.body()

                Log.d("ljs","accomModel 값 : ${accomList}")

                val centerLatitude = mLastLocation.latitude
                val centerLongitude = mLastLocation.longitude
                val radius = 5.0 // 5km 반경


                val accomistSpotsWithinRadius = accomList?.mapNotNull { spot ->
                    val distance = haversineDistance(
                        centerLatitude, centerLongitude,
                        spot.itemsLatitude, spot.itemsLongitude
                    )
                    if (distance <= radius) {
                        spot // 관광지 데이터 객체 자체를 반환
                    } else {
                        null
                    }
                }

                val currentTime = System.currentTimeMillis()

                // 일정 시간이 지나지 않았으면 업데이트를 건너뜁니다.
                if (currentTime - lastUpdateTimestamp < updateDelayMillis) {
                    return
                }

                lastUpdateTimestamp = currentTime

                val layoutManager = LinearLayoutManager(this@AccomActivity)

                binding.recyclerView.layoutManager = layoutManager

                binding.recyclerView.adapter =
                    AccomAdapter(this@AccomActivity,accomList)


                binding.recyclerView.addItemDecoration(
                    DividerItemDecoration(this@AccomActivity, LinearLayoutManager.VERTICAL)
                )

            }


            override fun onFailure(call: Call<List<AccomList>>, t: Throwable) {
                Log.d("lsy", "fail")
                call.cancel()
            }
        })
    }

    // 싱글탑으로 메인 액티비티 재사용 시, 호출되는 데이터가 새로 반영되도록 하는 코드
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // 새 인텐트 설정

        // 예시: 로그를 찍는 것으로 시작합니다.
        Log.d("lhs", "onNewIntent 호출됨")

        // 예를 들어, 사용자의 위치 데이터를 새로고침하는 메서드 호출
        getLocation("Accom")


    }

    // 위치 데이터 획득 추가 ---------------------------------------------------------
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30000
            //초기화 시간 30초

        }!!
    }

    // 안드로이드 기기에서 위,경도 받아오는 메서드
    @SuppressLint("MissingPermission")
    private fun getLocation(dataType: String) {
        Log.d("lhs", "getLocation: Fetching location for $dataType")
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    MyApplication.lat = it.latitude
                    MyApplication.lnt = it.longitude
                    Log.d("lhs", "현재위치 ${MyApplication.lat}, ${MyApplication.lnt}")
                    // 위치 정보를 SharedPreferences에 저장
                    saveLocationToSharedPreferences(MyApplication.lat, MyApplication.lnt)

                    val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
                    val lat : Double? = pref.getString("lat", "Default값")?.toDoubleOrNull()
                    val lnt : Double? = pref.getString("lnt", "Default값")?.toDoubleOrNull()
                    Log.d("lhs", "SharedPreferences에 현재위치 불러오기 ${lat}, ${lnt}")

                    // 서버에 데이터 요청
                    when (dataType) {

                        "Accom" -> fetchAccomData(lat, lnt, accomPage)
                        // 기타 데이터 유형에 대한 처리를 추가할 수 있음
                    }
                }
            }
            .addOnFailureListener {
                Log.d("lhs", "현재 위치 조회 실패")
            }
    }

    private fun saveLocationToSharedPreferences(lat: Double?, lnt: Double?) {
        Log.d("lhs", "[원하는 실행 순서 1]")
        Log.d("lhs", "SharedPreferences에 현재위치 저장하기 ${lat}, ${lnt}")
        // 앱 전체에서 위, 경도 값을 사용할 수 있도록 SharedPreferences 사용
        val pref = getSharedPreferences("latlnt", MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString("lat", "${lat}")
        editor.putString("lnt", "${lnt}")
        editor.commit()
    }

    private fun fetchAccomData(lat: Double?, lnt: Double?, page: Int) {
        // [변경 사항][공통] 현재 위치 위도, 경도 받아오기 == 카테고리끼리 공유 => 수정 필요없음
        // Accom 데이터 요청 로직
        // 예: networkService.getAccomGPS(lat, lnt, page) 호출 및 처리
        Log.d("lhs", "getLocation(Accom) 실행 됨 #######################")
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
                Log.d("lhs", "[원하는 실행 순서 2]")
                Log.d("lhs", "shared 불러온 후 lat : ${lat}, lnt : ${lnt}" +
                        " ->onCreate 안에서 절차대로 실행 ")

                // [변경 사항] 제주 투어 받아온 데이터 백으로 보내기
                sendAccomLocationToServer1(lat,lnt,accomPage)

                // RecyclerView에 스크롤 리스너 추가(오른쪽 끝에 닿았을 때, page 1씩 증가)
                binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!recyclerView.canScrollHorizontally(1)) { // 목록의 끝에 도달했는지 확인
                            accomPage++ // 페이지 번호 증가
                            sendAccomLocationToServer2(lat, lnt, accomPage) // 서버에 새 페이지 데이터 요청
                            Log.d("lhs", "Requesting accomPage 확인1: $accomPage")
                        }
                    }
                })

            }

            override fun onFailure(call: Call<MutableList<AccomList>>, t: Throwable) {
                Log.d("lhs", "fail")
                Log.d("lhs", "현재 위치 업데이트 실패: lat : ${lat}, lnt : ${lnt}")
            }
        })
    }

    // 숙박 데이터 요청
    private fun sendAccomLocationToServer1(lat: Double?, lnt: Double?, accomPage: Int?) {
        Log.d("lhs", "sendAccomLocationToServer1 실행 됨 전 @@@@@@@@@@@@@@@@@")
        fetchLocationData("Accom", lat, lnt, accomPage)
        Log.d("lhs", "sendAccomLocationToServer1 실행 됨 후 @@@@@@@@@@@@@@@@@")
    }
    private fun sendAccomLocationToServer2(lat: Double?, lnt: Double?, accomPage: Int?) {
        Log.d("lhs", "sendAccomLocationToServer2 실행 됨 #################")
        fetchLocationData2( "Accom", lat, lnt, accomPage)
    }

    // -----------------------------------------------------------------------------


    // Todo 확인 포인트 sendLocationTourToServer(lat, lnt)를 실행
    private fun createLocationCallback() {
        Log.d("lsy","createLocationCallback =====================================")
        mlocationCallback = object : LocationCallback() {
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest,
            mlocationCallback,
            Looper.myLooper()
        )
    }

    //-------------------------------------------------------------------------------------------

    // 위치 정보 업데이트 중지 -------------------------------------------------------------------------
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        getLocation("Accom")
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(mlocationCallback)
    }
    //-------------------------------------------------------------------------------------------

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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // 핸들러 메시지 제거
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
                "Accom" -> {
                    (dataList as? MutableList<AccomList>)?.let { accomList ->
                        AccomData.addAll(accomList)
                        Log.d("lhs", "[원하는 실행 순서 3]")
                        Log.d("lhs", "현재 위치 업데이트 성공1: lat : ${lat}, lnt : ${lnt}" +
                                " -> onCreate 안에서 절차대로 실행2 \n -> sendAccomLocationToServer1()에 의해 실행 ")
                        Log.d("lhs", "getAccomGPS로 불러온 accomList 값 : ${accomList}")
                        Log.d("lhs", "getAccomGPS로 불러온 accomList 사이즈 : ${accomList.size}")
                        Log.d("lhs", "통신 후 받아온 accomList 길이 값 : ${accomList.size}")
                        Log.d("lhs", "Requesting accomPage 확인2: $page")

                        val accomLayoutManager =
                            LinearLayoutManager(this@AccomActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.recyclerView.layoutManager = accomLayoutManager
                        binding.recyclerView.adapter = AccomAdapter(this@AccomActivity, AccomData)
                    }
                }
            }
        }


        val onFailure: (Throwable) -> Unit = { t ->
            Log.d("lhs", "[원하는 실행 순서 3] $type List 통신 : fail, Error: $t")
        }

        when (type) {
            "Accom" -> {
                val accomGPSCall = networkService.getAccomGPS(lat, lnt, 4.5, page)
                fetchAndProcessData(accomGPSCall, processData, onFailure)
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
                Log.d("lhs", "[요청 실패] : $t")
                call.cancel()
            }
        })
    }
    // fetchLocationData2 공통
    private fun fetchLocationData2(type: String, lat: Double?, lnt: Double?, page: Int?) {
        val networkService = (applicationContext as MyApplication).networkService
        when (type) {
            "Accom" -> {
                val accomGPSCall = networkService.getAccomGPS(lat, lnt, 4.5, page)
                fetchAndProcessLocationData(accomGPSCall) { newData ->
                    updateData(newData, AccomData, AccomRecycler)
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
        Log.d("lhs","updateData 함수 호출 시작.")
        Log.d("lhs","updateData 함수 호출 시작2.datasSpring size 값 : ${existingData?.size} ")

        existingData?.size?.let {
            recycler.adapter?.notifyItemInserted(it.minus(1))
        }

        if (existingData != null && newData != null) {
            existingData.addAll(newData)
        }

        recycler.adapter?.notifyDataSetChanged()
    }



    // menu 기능
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
                Toast.makeText(this@AccomActivity,"검색어가 전송됨 : ${query}", Toast.LENGTH_SHORT).show()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

}
