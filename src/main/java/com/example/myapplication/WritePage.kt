package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WritePage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var editText: EditText
    private lateinit var pwText: EditText
    private lateinit var pageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_writepage)
        database = FirebaseDatabase.getInstance().reference
        val addBtn: Button = findViewById(R.id.button6)
        editText = findViewById(R.id.editTextText2)
        pwText = findViewById(R.id.editTextTextPassword1)
        pageText = findViewById(R.id.contentwirte)


        addBtn.setOnClickListener(){
            writepage()
            val intent = Intent(this@WritePage, Community::class.java)
            startActivity(intent)
        }
    }

    private fun writepage(){
        val titlevalue =  editText.text.toString()
        val pwvalue =  pwText.text.toString()
        val pagewirte = pageText.text.toString()
        val now = System.currentTimeMillis()
        val date = Date(now)
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val getTime = dateFormat.format(date)
        val postId = database.child("게시판").push().key
        if (postId != null) {
            val postValues = mapOf(
                "title" to titlevalue,
                "time" to getTime,
                "password" to pwvalue,
                "content" to pagewirte,
                "조회수" to 0
            )
            database.child("게시판").child(postId).setValue(postValues)

        }
    }
}