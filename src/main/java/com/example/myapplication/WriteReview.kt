package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WriteReview : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var editText: EditText
    private lateinit var pwText: EditText
    private lateinit var pageText: EditText
    private lateinit var ratingBar: RatingBar
    private var ratingpoint: Float = 0f
    private var postId: String = ""
    private var imageUris: MutableList<Uri> = mutableListOf()  // 여러 개의 이미지 URI를 저장할 리스트
    private lateinit var restname: String
    private var sum: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write_review)
        database = FirebaseDatabase.getInstance().reference
        val addBtn: Button = findViewById(R.id.button7)
        editText = findViewById(R.id.editTextText1)
        pwText = findViewById(R.id.editTextTextPassword12)
        pageText = findViewById(R.id.contentwirte1)
        val gallerybtn: ImageButton = findViewById(R.id.gallery)
        var count = 0
        restname = intent.getStringExtra("restname") ?: ""

        gallerybtn.setOnClickListener {
            openGallery()
            count += 1
        }

        database.child("김해금관가야").child("리뷰페이지").child("총점").child("BHC").get()
            .addOnSuccessListener { dataSnapshot ->
                val value = dataSnapshot.value.toString()
                sum = value.toFloatOrNull() ?: 0.0f
                Log.i("firebase", "Got value: $sum")
            }
            .addOnFailureListener { exception ->
                Log.e("firebase", "Error getting data", exception)
            }

        addBtn.setOnClickListener {
            sum += ratingpoint
            writepage()
            if (count == 1 && imageUris.isNotEmpty()) {
                uploadImagesToFirebaseStorage()
                count = 0
            }
            val intent = Intent(this@WriteReview, Packaging::class.java)
            intent.putExtra("restname", restname)
            startActivity(intent)
        }

        ratingBar = findViewById(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratingpoint = rating
        }
    }

    private fun writepage() {
        val titlevalue = editText.text.toString()
        val pwvalue = pwText.text.toString()
        val pagewirte = pageText.text.toString()
        val now = System.currentTimeMillis()
        val date = Date(now)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val getTime = dateFormat.format(date)
        postId = database.child("김해금관가야").child("리뷰페이지").child("BHC").push().key.toString()

        if (postId != null) {
            val postValues = mapOf(
                "time" to getTime,
                "title" to titlevalue,
                "password" to pwvalue,
                "content" to pagewirte,
                "rating" to ratingpoint
            )
            val mapId = mapOf(
                "BHC" to sum
            )
            database.child("김해금관가야").child("리뷰페이지").child("BHC").child(postId).setValue(postValues)
            database.child("김해금관가야").child("리뷰페이지").child("총점").setValue(mapId)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // 여러 이미지를 선택할 수 있도록 설정
        galleryLauncher.launch(galleryIntent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val imgContainer: LinearLayout = findViewById(R.id.imagecheckcontainer)
                if (it.clipData != null) {
                    // 여러 이미지를 선택한 경우
                    val count = it.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = it.clipData!!.getItemAt(i).uri
                        imageUris.add(imageUri)

                        // ImageView 생성 및 추가
                        val imageView = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(400, 400).apply {
                                setMargins(8, 8, 8, 8) // 공백 제거
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setImageURI(imageUri)
                        }
                        imgContainer.addView(imageView)
                    }
                } else {
                    // 단일 이미지를 선택한 경우
                    val imageUri = it.data
                    imageUri?.let { uri ->
                        imageUris.add(uri)

                        // ImageView 생성 및 추가
                        val imageView = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                                setMargins(0, 0, 0, 0) // 공백 제거
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setImageURI(uri)
                        }
                        imgContainer.addView(imageView)
                    }
                }
            }
        }
    }



    private fun uploadImagesToFirebaseStorage() {
        val imageUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    imageUrls.add(downloadUri.toString())
                    // 모든 이미지가 업로드된 후, Firebase Database에 URL 리스트를 저장
                    if (index == imageUris.size - 1) {
                        saveImageUrlsToDatabase(imageUrls)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("Upload", "Failed to upload image", exception)
            }
        }
    }

    private fun saveImageUrlsToDatabase(imageUrls: List<String>) {
        val galleryUriMap = mapOf("images" to imageUrls)
        database.child("김해금관가야").child("리뷰페이지").child("BHC").child(postId)
            .updateChildren(galleryUriMap)
            .addOnSuccessListener {
                Log.d("Database", "Image URLs saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Database", "Failed to save image URLs", exception)
            }
    }
}
