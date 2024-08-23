package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Store : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var count: Long = 0
    private var average: Float = 0f
    private var restname : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        restname = intent.getStringExtra("restname").toString()

        // Firebase 데이터 로드 및 이후 작업 수행
        loadDataFromFirebase {
            initializeUI()
        }

        val imagbtn : ImageButton = findViewById(R.id.imageButton2)
        imagbtn.setOnClickListener(){
            val intent = Intent(this@Store, MainActivity::class.java)
            startActivity(intent)
        }
    }



    private fun initializeUI() {
        val backgroudrawble: Drawable? = ContextCompat.getDrawable(this, R.drawable.rounded_button)
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        Log.d("restname", restname)
        database = FirebaseDatabase.getInstance().reference
        val databaseref = database.child(restname)

        databaseref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (refsnapshot in snapshot.children){
                    if(refsnapshot.key.equals("리뷰페이지")){
                        continue
                    }
                    val categoryname = refsnapshot.key.toString()
                    val button = Button(this@Store).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setOnClickListener {
                            // 받아온 데이터 정리 및 설정
                            val intent = Intent(context, Packaging::class.java)
                            intent.putExtra("restname", restname)
                            startActivity(intent)
                        }

                        // 버튼 꾸미기
                        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        text = "$categoryname \n\n 운영시간 \n08시 ~ 16시"
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
                        val paddingInDp = 150
                        val scale = resources.displayMetrics.density
                        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
                        setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                        background = backgroudrawble
                    }

                    // 이미지의 크기를 조정하는 함수 호출
                    val drawable = resizeDrawable(this@Store, R.drawable.bhc1, 100, 80)
                    button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

                    val ratingBarView = RatingBar(
                        ContextThemeWrapper(this@Store, R.style.MyRatingBarStyle), null, 0
                    ).apply {
                        numStars = 5
                        stepSize = 1f
                        rating = average
                        max = 5
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }

                    buttonContainer.addView(ratingBarView)
                    buttonContainer.addView(button)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        // 새로운 버튼을 생성합니다.

    }

    private fun loadDataFromFirebase(onDataLoaded: () -> Unit) {
        database = FirebaseDatabase.getInstance().reference
        val ref = database.child("$restname").child("리뷰페이지")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                count = snapshot.child("BHC").childrenCount
                val sum = snapshot.child("총점").child("BHC").getValue(Float::class.java) ?: 0f
                Log.d("aver", sum.toString())
                average = sum / count
                Log.d("aver", average.toString())
                onDataLoaded()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
                Toast.makeText(this@Store, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Drawable 크기 조정 함수
    private fun resizeDrawable(context: Context, resId: Int, width: Int, height: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return BitmapDrawable(context.resources, resizedBitmap)
    }
}
