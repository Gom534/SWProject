package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.marginLeft
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


class Packaging : AppCompatActivity() {
    private lateinit var restname: String
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentmenu: menuFragment
    private lateinit var fragmentreview: reviewFragment
    private lateinit var transaction: FragmentTransaction
    private lateinit var menubtn: Button
    private lateinit var reviewbtn: Button
    private lateinit var pencilbtn: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.packaging)
        createNotificationChannel()
        requestNotificationPermission()
        val titletextview: TextView = findViewById(R.id.textView5)
        fragmentManager = supportFragmentManager

        // Fragment 인스턴스 생성
        fragmentmenu = menuFragment()
        fragmentreview = reviewFragment()

        // 초기 Fragment 설정
        transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragmentmenu).commit()
        pencilbtn = findViewById(R.id.pencil)

        val myButton: ImageButton = findViewById(R.id.imageButton2)
        myButton.setOnClickListener {
            val intent = Intent(this@Packaging, MainActivity::class.java)
            startActivity(intent)
        }
        restname = intent.getStringExtra("restname") ?: ""
        menubtn = findViewById(R.id.menubtn)
        reviewbtn = findViewById(R.id.reviewbtn)
        clickHandler(menubtn)
        pencilbtn.setOnClickListener() {
            val intent = Intent(this@Packaging, WriteReview::class.java)
            intent.putExtra("restname", restname)
            startActivity(intent)
        }
        menubtn.setOnClickListener {
            clickHandler(menubtn)
            pencilbtn.isVisible = false
            titletextview.text = "메뉴 선택"
        }
        reviewbtn.setOnClickListener {
            createNotificationChannel()
            pencilbtn.isVisible = true
            clickHandler(reviewbtn)
            titletextview.text = "리뷰 페이지"
        }
    }

    private fun clickHandler(view: View) {
        val transaction = fragmentManager.beginTransaction()
        when (view.id) {
            R.id.menubtn -> {
                val bundle = Bundle().apply {
                    putString("restname", restname)
                }
                fragmentmenu.arguments = bundle // Fragment에 데이터 전달
                transaction.replace(R.id.frameLayout, fragmentmenu)
                menubtn.setBackgroundResource(R.drawable.buttonpackging)
                reviewbtn.setBackgroundResource(R.drawable.buttonpackging2)
            }

            R.id.reviewbtn -> {
                val bundle = Bundle().apply {
                    putString("restname", restname)
                }
                fragmentreview.arguments = bundle // Fragment에 데이터 전달
                transaction.replace(R.id.frameLayout, fragmentreview)
                menubtn.setBackgroundResource(R.drawable.buttonpackging2)
                reviewbtn.setBackgroundResource(R.drawable.buttonpackging)
            }
        }
        transaction.commit()
    }

    // 알림기능 한번 써보기
    // 알림기능을 써보면서 누르면 다시 내 메인페이지르 오는거까지 구현
    private fun createNotificationChannel() {
        // 알림 채널 ID와 이름을 설정
        val channelId = "my_channel_id"
        val channelName = "My Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "My notification channel description"
            }

            // NotificationManager를 통해 채널 등록
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(context: Context) {
        val channelId = "my_channel_id" // 위에서 생성한 채널 ID

        // 알림을 클릭했을 때 실행될 액티비티 설정
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // 알림을 구성
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.project4) // 작은 아이콘 설정
            .setContentTitle("주문이 완료되었습니다")
            .setContentText("이건 아직 개발안됐어요!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 인텐트 설정
            .setAutoCancel(true) // 알림 클릭 시 자동 삭제
            .setColor(ContextCompat.getColor(this@Packaging, androidx.appcompat.R.color.primary_material_light))

        // 알림 매니저를 통해 알림을 표시
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 이상
            val postNotificationPermission = "android.permission.POST_NOTIFICATIONS"

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    postNotificationPermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 허용된 경우
                    sendNotification(this)
                }
                shouldShowRequestPermissionRationale(postNotificationPermission) -> {
                    // 사용자가 이전에 권한 요청을 거부한 경우 설명을 제공
                    // 사용자에게 권한이 필요한 이유를 설명하는 UI를 표시할 수 있음
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(postNotificationPermission)
                }
            }
        } else {
            // Android 12 이하에서는 권한이 필요하지 않으므로 바로 알림을 보낼 수 있음
            sendNotification(this)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한이 허용되었을 때 알림을 전송
            sendNotification(this)
        } else {
            // 권한이 거부되었을 때 처리
            // 알림을 사용할 수 없음을 사용자에게 알리거나 다른 처리
        }
    }
}
