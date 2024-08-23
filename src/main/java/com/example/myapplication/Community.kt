package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values
import org.w3c.dom.Text

class Community : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var layout : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community)

        database = FirebaseDatabase.getInstance().reference
        val writeBtn: ImageButton = findViewById(R.id.imageButton5)

        writeBtn.setOnClickListener(){
            val intent = Intent(this@Community, WritePage::class.java)
            startActivity(intent)
        }
        fetchPosts()
    }
    private fun fetchPosts() {

        layout = findViewById(R.id.layout)
        database.child("게시판").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                layout.removeAllViews()

                for (postSnapshot in snapshot.children.reversed()) {
                    val title = postSnapshot.child("title").value as? String
                    val password = postSnapshot.child("password").value as? String
                    val content = postSnapshot.child("content").value as? String
                    val time = postSnapshot.child("time").value as? String
                    val postKey = postSnapshot.key
                    val removeref = FirebaseDatabase.getInstance().reference
                        .child("게시판")
                        .child("$postKey")

                    if (title != null && password != null && content != null && postKey != null) {
                        // Layout to hold each post
                        val postLayout = LinearLayout(this@Community).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 16, 16, 16)
                            }
                            val paddingInDp = 25
                            val paddingInPx = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, paddingInDp.toFloat(), resources.displayMetrics
                            ).toInt()
                            setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                            background = ContextCompat.getDrawable(this@Community, R.drawable.rounded_button_green)
                        }

                        // Layout for title and time + delete button
                        val titleLayout = LinearLayout(this@Community).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                300
                            )
                        }

                        val titleContentTextView = TextView(this@Community).apply {
                            text = "$title\n\n$content"
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.0f)
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            gravity = Gravity.START
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        // Vertical layout for time and delete button
                        val timeAndDeleteLayout = LinearLayout(this@Community).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.END
                            }
                        }

                        // TextView for time
                        val timeTextView = TextView(this@Community).apply {
                            text = "$time \n"
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                            gravity = Gravity.END
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // Delete button
                        val deleteButton = ImageButton(this@Community).apply {
                            setImageResource(R.drawable.trashcan)
                            layoutParams = LinearLayout.LayoutParams(
                                100,
                                100
                            ).apply {
                                gravity = Gravity.END
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setBackgroundColor(Color.TRANSPARENT)
                        }

                        // Handle delete button click
                        deleteButton.setOnClickListener {
                            AlertDialog.Builder(this@Community).apply {
                                setTitle("삭제 확인")
                                setMessage("정말로 이 항목을 삭제하시겠습니까?")
                                setPositiveButton("예") { dialog, _ ->
                                    layout.removeView(postLayout)
                                    removeref.removeValue()
                                    dialog.dismiss()
                                }
                                setNegativeButton("아니오") { dialog, _ ->
                                    dialog.dismiss()
                                }
                            }.show()
                        }

                        // Add time and delete button to their vertical layout
                        timeAndDeleteLayout.addView(timeTextView)
                        timeAndDeleteLayout.addView(deleteButton)

                        // Add views to the title layout
                        titleLayout.addView(titleContentTextView)
                        titleLayout.addView(timeAndDeleteLayout)

                        // Add title layout to the post layout
                        postLayout.addView(titleLayout)

                        // Add the post layout to the main layout
                        layout.addView(postLayout)
                    }
                }
            }


            override fun onCancelled(error: DatabaseError){

        }
    })
}
}
