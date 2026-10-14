package com.example.extra

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.app.ServiceCompat

class VoiceService : Service() {

    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    // Wake word: ekhane nickname bodlate paro
    private val wakeWords = listOf("extra", "এক্সট্রা", "এক্সট্রা")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "extra_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Extra", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Extra shunche...")
            .setContentText("\"Extra\" bole kotha shuru koro")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        ServiceCompat.startForeground(
            this, 1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )

        if (!running) {
            running = true
            listen()
        }
        return START_STICKY
    }

    private fun listen() {
        if (!running) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    list?.forEach { handleSpeech(it) }
                    restart(400)
                }

                override fun onError(error: Int) {
                    restart(1200)
                }

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer?.startListening(intent)
    }

    private fun restart(delayMs: Long) {
        handler.postDelayed({ listen() }, delayMs)
    }

    private fun handleSpeech(spoken: String) {
        val text = spoken.lowercase().trim()
        val wake = wakeWords.firstOrNull { text.contains(it) } ?: return
        val command = text.substringAfter(wake).trim()
        toast("Shunlam: $command")

        // CALL command: "call rahim" / "কল করো রহিম"
        val callWords = listOf("call", "কল")
        if (callWords.any { command.contains(it) }) {
            var name = command
            val remove = listOf("call", "কল", "koro", "করো", "কর", "করুন", "ke", "কে", "to", "ko")
            remove.forEach { name = name.replace(it, " ") }
            name = name.trim()
            if (name.isNotEmpty()) callContact(name)
        }
    }

    private fun callContact(name: String) {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val number = it.getString(0)
                val contactName = it.getString(1)
                toast("Call kortechi: $contactName")
                try {
                    val telecom = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    telecom.placeCall(Uri.parse("tel:$number"), null)
                } catch (e: SecurityException) {
                    toast("Call permission dao")
                }
                return
            }
        }
        toast("Contact pelam na: $name")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        super.onDestroy()
    }
}
