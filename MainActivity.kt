package com.example.extra

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val info = TextView(this).apply {
            text = "Extra Voice Control\n\nStart চাপুন, তারপর বলুন:\n\"Extra, call Rahim\"\n\"Extra, কল করো রহিম\""
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val startBtn = Button(this).apply {
            text = "Start"
            setOnClickListener {
                requestPermissions(permissions, 1)
                startForegroundService(Intent(this@MainActivity, VoiceService::class.java))
            }
        }

        val stopBtn = Button(this).apply {
            text = "Stop"
            setOnClickListener {
                stopService(Intent(this@MainActivity, VoiceService::class.java))
            }
        }

        layout.addView(info)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }
}
