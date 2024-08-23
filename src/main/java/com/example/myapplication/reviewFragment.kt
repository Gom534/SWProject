package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class reviewFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var buttoncontainer: LinearLayout
    private lateinit var reviewLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        buttoncontainer = view.findViewById(R.id.buttonContainerreview)
        database = FirebaseDatabase.getInstance().reference.child("김해금관가야").child("리뷰페이지").child("BHC")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                buttoncontainer.removeAllViews() // 기존 뷰를 모두 제거

                for (postSnapshot in snapshot.children.reversed()) {
                    val title = postSnapshot.child("title").value as? String
                    val password = postSnapshot.child("password").value as? String
                    val content = postSnapshot.child("content").value as? String
                    val ratingValue = postSnapshot.child("rating").value as? Long
                    val imagesSnapshot = postSnapshot.child("images")
                    val time = postSnapshot.child("time").value as? String
                    val postKey = postSnapshot.key
                    val removeref = FirebaseDatabase.getInstance().reference.child("김해금관가야")
                        .child("리뷰페이지").child("BHC").child("$postKey")

                    val answer = postSnapshot.child("답변").child("사장님").child("내용").value as? String
                    val answertime = postSnapshot.child("답변").child("사장님").child("시간").value as? String

                    if (title != null && password != null && content != null && postKey != null) {
                        // 각 뷰에 집어넣기
                        // 기본적으로 정보가 있어야 시작
                        reviewLayout = LinearLayout(requireActivity()).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 16, 16, 16)
                            }
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_green)
                        }

                        // Horizontal layout for title + content on the left, time + delete on the right
                        val mainLayout = LinearLayout(requireActivity()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // 타이틀 불러오기 && 내용불러오기
                        val titleContentTextView = TextView(requireActivity()).apply {
                            text = "$title \n\n$content"
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.0f)
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            gravity = Gravity.START
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f // Weight of 1 to occupy most space
                            )
                        }

                        val timeAndDeleteLayout = LinearLayout(requireActivity()).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.END
                            }
                        }

                        // TextView for time
                        val timeTextView = TextView(requireActivity()).apply {
                            text = "$time \n"
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                            gravity = Gravity.END
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        //이미지 버튼 추가 및 이미지버튼 (쓰레기통 이미지)누를 시 삭제 메소드 실행
                        val imageButton = ImageButton(requireContext()).apply {
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

                        imageButton.setOnClickListener {
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle("삭제 확인")
                                setMessage("정말로 이 항목을 삭제하시겠습니까?")
                                setPositiveButton("예") { dialog, _ ->
                                    buttoncontainer.removeView(reviewLayout)
                                    removeref.removeValue()
                                    dialog.dismiss()
                                }
                                setNegativeButton("아니오") { dialog, _ ->
                                    dialog.dismiss()
                                }
                            }.show()
                        }

                        timeAndDeleteLayout.addView(timeTextView)
                        timeAndDeleteLayout.addView(imageButton)

                        // Add titleContentTextView and timeAndDeleteLayout to mainLayout
                        mainLayout.addView(titleContentTextView)
                        mainLayout.addView(timeAndDeleteLayout)

                        // Add mainLayout to reviewLayout
                        reviewLayout.addView(mainLayout)

                        val ratingBarView = RatingBar(ContextThemeWrapper(requireActivity(), R.style.MyRatingBarStyle), null, 0).apply {
                            numStars = 5
                            stepSize = 1f
                            rating = ratingValue?.toFloat() ?: 0f
                            max = 5
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setBackgroundColor(Color.TRANSPARENT)
                        }
                        reviewLayout.addView(ratingBarView)

                        // 이미지뷰 추가
                        if (imagesSnapshot.exists()) {
                            //가로모양 스크롤뷰
                            val horizontalScrollView = HorizontalScrollView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                isHorizontalScrollBarEnabled = false
                            }
                            val horizontalLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }
                            // 이미지뷰 스냅샷 찍어서 자식들 불러오대 자식들의 값만 불러와서 이미지의 URL만 불러옴
                            // 이미지뷰의 형식은 키는 0 벨류는 uri
                            for (imageSnapshot in imagesSnapshot.children) {
                                val imageUri = imageSnapshot.value as? String
                                if (imageUri != null) {
                                    val imageView = ImageView(requireContext()).apply {
                                        Glide.with(requireContext())
                                            .load(imageUri)
                                            .override(400, 400)
                                            .into(this)
                                        layoutParams = LinearLayout.LayoutParams(
                                            250,
                                            250
                                        ).apply {
                                            setMargins(8, 8, 8, 8)
                                        }
                                    }
                                    imageView.setOnClickListener {
                                        val intent = Intent(requireContext(), MyImageView::class.java)
                                        intent.putExtra("uri", imageUri)
                                        startActivity(intent)
                                    }
                                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                                    horizontalLayout.addView(imageView)
                                }
                            }
                            horizontalScrollView.addView(horizontalLayout)
                            reviewLayout.addView(horizontalScrollView)
                        }
                    }
                    // 콘테이너에 추가
                    buttoncontainer.addView(reviewLayout)

                    // 답변 생성
                    if (answer != null && answertime != null) {
                        val answertextView = TextView(requireActivity()).apply {
                            text = "사장님  $answertime \n\n $answer  "
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.0f)
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            ).apply {
                                setMargins(16, 32, 16, 32)
                            }
                            // 말풍선 모양의 배경을 설정합니다.
                            val backgroundDrawable = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 20f // 모서리를 둥글게 만듭니다.
                                setColor(Color.parseColor("#f1f3f5")) // 배경색을 설정합니다.
                            }
                            background = backgroundDrawable
                            // 패딩을 추가하여 말풍선 모양을 더 강조합니다.
                            setPadding(50, 50, 50, 50)
                        }

                        reviewLayout.addView(answertextView)
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {
                //실패시 호출
            }
        })
    }
}


   /* private fun fetchPosts() {
        database.child("김해금관가야").child("리뷰페이지").child("BHC").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                for (postSnapshot in snapshot.children){
                    val backgroudrawble: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
                    val title = postSnapshot.child("title").value as String?
                    val password = postSnapshot.child("password").value as String?
                    val content = postSnapshot.child("content").value as String?
                    val rating = postSnapshot.child("rating").value as Long?
                    
                    if (title != null && password != null && content != null) {
                        val postKey = postSnapshot.key // 자식 노드의 키 가져오기
                        //버튼 생성 및 양식 지정
                        val postButton = Button(requireActivity()).apply {
                            text = title +"\n"+content
                            setOnClickListener {
                                Log.d("fbkey", postKey.toString())
                                val intent = Intent(requireActivity(), Contentpage::class.java)
                                intent.putExtra("key" , postKey)
                                startActivity(intent)
                            }
                            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->  }
                            textAlignment = left
                            gravity= left
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP,15.0f)

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setBackgroundColor(Color.TRANSPARENT)
                            params.setMargins(0, 16, 0, 16) // 왼쪽, 위, 오른쪽, 아래 마진 설정 (픽셀 단위)
                            layoutParams = params
                            val paddingInDp = 16
                            val paddingInPx = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, paddingInDp.toFloat(), resources.displayMetrics
                            ).toInt()
                            setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                            background = backgroudrawble
                        }
                        
                        buttoncontainer.addView(postButton)

                    }
                }
            }
            override fun onCancelled(error: DatabaseError){

            }
        })
    }*/
