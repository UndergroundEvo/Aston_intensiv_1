package ru.aston.serviceplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.aston.serviceplayer.Const.REQUEST_NOTIFICATION_PERMISSION

class MainActivity : AppCompatActivity() {

    private lateinit var musicService: MusicService
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), Const.REQUEST_NOTIFICATION_PERMISSION)
            }
        }

        val playButton: ImageView = findViewById(R.id.player_play)
        val pauseButton: ImageView = findViewById(R.id.player_pause)
        val nextButton: ImageView = findViewById(R.id.player_next)
        val prevButton: ImageView = findViewById(R.id.player_previous)

        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE)

        playButton.setOnClickListener {
            if (isBound) musicService.playTrack()
        }

        pauseButton.setOnClickListener {
            if (isBound) musicService.togglePlayPause()
        }

        nextButton.setOnClickListener {
            if (isBound) musicService.nextTrack()
        }

        prevButton.setOnClickListener {
            if (isBound) musicService.previousTrack()
        }
    }
    private val musicConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) unbindService(musicConnection)
    }
}