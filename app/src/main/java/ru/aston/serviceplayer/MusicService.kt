package ru.aston.serviceplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class MusicService : Service() {
    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var trackIndex = 0

    private val tracks = listOf(
        R.raw.digitalism_pogo,
        R.raw.michael_jackson_billie_jean,
        R.raw.arkells_kflay_you_can_get_it,
        R.raw.dua_lipa_levitating,
        R.raw.toto_africa
    )

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Const.CHANNEL_ID, Const.CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val playIntent = Intent(this, MusicService::class.java).apply { action = "ACTION_PLAY" }
        val pauseIntent = Intent(this, MusicService::class.java).apply { action = "ACTION_PAUSE" }
        val nextIntent = Intent(this, MusicService::class.java).apply { action = "ACTION_NEXT" }
        val prevIntent = Intent(this, MusicService::class.java).apply { action = "ACTION_PREV" }

        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val remoteView = RemoteViews(packageName, R.layout.notification_layout).apply {
            setOnClickPendingIntent(R.id.notification_play, playPendingIntent)
            setOnClickPendingIntent(R.id.notification_pause, pausePendingIntent)
            setOnClickPendingIntent(R.id.notification_next, nextPendingIntent)
            setOnClickPendingIntent(R.id.notification_previos, prevPendingIntent)
        }

        return NotificationCompat.Builder(this, Const.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_play_arrow_24)
            //.setContent(remoteView)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.baseline_play_arrow_24,applicationContext.getString(R.string.play_button),playPendingIntent)
            .addAction(R.drawable.baseline_pause_24,applicationContext.getString(R.string.pause_button),pausePendingIntent)
            .addAction(R.drawable.baseline_skip_next_24,applicationContext.getString(R.string.next_button),nextPendingIntent)
            .addAction(R.drawable.baseline_skip_previous_24,applicationContext.getString(R.string.previous_button),prevPendingIntent)

            .build()
    }

    fun togglePlayPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    fun nextTrack() {
        trackIndex = (trackIndex + 1) % tracks.size
        playTrack()
    }

    fun previousTrack() {
        trackIndex = if (trackIndex - 1 < 0) tracks.size - 1 else trackIndex - 1
        playTrack()
    }

    fun playTrack() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, tracks[trackIndex])
        mediaPlayer?.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_PLAY" -> playTrack()
            "ACTION_PAUSE" -> togglePlayPause()
            "ACTION_NEXT" -> nextTrack()
            "ACTION_PREV" -> previousTrack()
        }
        return START_NOT_STICKY
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}