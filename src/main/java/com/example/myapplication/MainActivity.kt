package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

import android.Manifest
import android.widget.Switch
import com.example.myapplication.fcm.FCMActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.EventListener

class MainActivity : AppCompatActivity() {
    data class GasStation(
        @SerializedName("serviceAreaName") val name: String,
        @SerializedName("gasolinePrice") val gasolinePrice: String,
        @SerializedName("diselPrice") val dieselPrice: String,
        @SerializedName("lpgPrice") val lpgPrice: String
    )
    data class ApiResponse(
        @SerializedName("list") val gasStations: List<GasStation>
    )
    private lateinit var  textview : TextView
    private lateinit var tvNearestRestStop:TextView
    private lateinit var requestPermissionsUtil: RequestPermissionsUtil
    private lateinit var database  : DatabaseReference
    private var restname  =""
    private val locationUtils by lazy { LocationUtils(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_practice)
        requestPermissionsUtil = RequestPermissionsUtil(this)
        val myButton: ImageButton = findViewById(R.id.button)
        val mapbutton : ImageButton = findViewById(R.id.button2)
        val cartimg : ImageButton = findViewById(R.id.imageButton4)
        val cumButton: ImageButton = findViewById(R.id.button1)
        tvNearestRestStop = findViewById(R.id.textView)
        val searchbtn : ImageButton = findViewById(R.id.seachbtn)
        var textViewmain1 :TextView = findViewById(R.id.textView11)
        var textViewmain2 :TextView = findViewById(R.id.textView13)
        var textViewmain3 :TextView = findViewById(R.id.textView14)
        searchbtn.setOnClickListener(){
            val intent = Intent(this@MainActivity, Search::class.java)
            startActivity(intent)
        }

        database = FirebaseDatabase.getInstance().reference
        database.child("게시판").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 1
                for (postSnapshot in snapshot.children.reversed()) {

                    val title = postSnapshot.child("title").value.toString()

                    when (count) {
                        1 -> textViewmain1.text = title
                        2 -> textViewmain2.text = title
                        3 -> textViewmain3.text = title
                    }
                    count++
                    if (count > 3) break // 세 번째 항목까지 처리하면 루프를 종료
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        mapbutton.setOnClickListener{
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }
        cumButton.setOnClickListener{
            val intent = Intent(this@MainActivity, Community::class.java)
            startActivity(intent)

        }

        myButton.setOnClickListener {
                // 버튼 클릭 페이지 이동
                val intent = Intent(this@MainActivity, Store::class.java);
                intent.putExtra("restname", restname)
            Log.d("Main restname", restname)
            startActivity(intent)
        }
        cartimg.setOnClickListener{
            val intent = Intent(this@MainActivity, Cart::class.java);
            startActivity(intent)
        }
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = MyAdapter(this)
        initialize()

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // API 요청을 보내고 응답을 처리하는 함수
    private fun fetchGasStationPrices(apiKey: String) {
        val client = OkHttpClient()
        val url = "https://data.ex.co.kr/openapi/business/curStateStation?key=$apiKey&type=json&serviceAreaName=$restname"
        Log.d("TAG", "주유소 이름 : $restname")

        val request = Request.Builder()
            .url(url)
            .build()
        // 주유 금액 받아오는 메소드
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    val gson = Gson()
                    val apiResponse = gson.fromJson(json, ApiResponse::class.java)
                    textview = findViewById(R.id.textView4)
                    var digeltextview : TextView= findViewById(R.id.textView5)
                    var lpgtextview : TextView= findViewById(R.id.textView2)
                    for (station in apiResponse.gasStations) {
                        textview.text = "가솔린 :\n " + station.gasolinePrice
                        digeltextview.text = "디젤 :\n " +station.dieselPrice
                        lpgtextview.text = "LPG : \n" + station.lpgPrice

                    }
                }
            }
        })
    }
    private fun oilPrice(apiKey: String) {
        fetchGasStationPrices(apiKey)
    }
    private fun nearStop(callback: () -> Unit) {
        locationUtils.getLastLocation().addOnSuccessListener { location ->
            val userLocation = LatLng(location.latitude, location.longitude)
            // 가장 가까운 휴게소 찾기
            val nearestRestStop = locationUtils.findNearestRestStop(userLocation)
            // 휴게소 이름 UI에 표시하기
            nearestRestStop?.let {
                tvNearestRestStop.text = it.name + "휴게소"
                restname = it.name
                Log.d("TAG", "주유소 이름 : $restname")
                // nearStop 작업이 완료되었을 때 콜백 호출
                callback()
            }
        }
    }
    // 이 메서드에서 nearStop과 oilPrice 호출
    private fun initialize() {
        val apiKey = BuildConfig.oilapikey
        //고차함수와{}치는게 고차함수
        // nearStop자체도 함수인데 그안에 함수를 집어넣어 순서를 결정 이렇게 안하면 콜백으로 인하여 니어스탑보다 oilprice가 우선실행되어
        // 내가원하는 값이 안나옴
        nearStop {
            oilPrice(apiKey)
        }
    }
}







