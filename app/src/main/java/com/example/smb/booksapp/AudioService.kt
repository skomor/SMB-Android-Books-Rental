package com.example.smb.booksapp

import android.app.Service
import android.media.MediaPlayer

import android.content.Intent

import android.os.IBinder

class AudioService : Service() {
    private val player: MediaPlayer = MediaPlayer()
    lateinit var datasourcePath:String

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        datasourcePath = intent?.extras?.get("path").toString()
        player.setDataSource(
            datasourcePath
        )
        player.prepare();
        player.start();
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}