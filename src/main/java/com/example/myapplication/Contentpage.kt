package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values

class Contentpage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contentpage)
        val imgbtn: ImageButton = findViewById(R.id.imageButton2)
        val titletext: TextView = findViewById(R.id.titletext)
        val contenttxt: TextView = findViewById(R.id.contenttext)
        val datakey = intent.getStringExtra("key")
        imgbtn.setOnClickListener() {
            val intent = Intent(this@Contentpage, Community::class.java)
            startActivity(intent)
        }
        val database = FirebaseDatabase.getInstance().reference

        database.child("게시판").child(datakey.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // "title" 값 가져오기
                val title = snapshot.child("title").getValue(String::class.java)
                // "content" 값 가져오기
                val content = snapshot.child("content").getValue(String::class.java)
                // 텍스트뷰에 값 설정
                titletext.text = title
                contenttxt.text = content
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터베이스 오류 처리
                Log.e("ContentPage", "Failed to read data", error.toException())
            }
        })
    }

}
