package com.example.outerforservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log

class MyMessengerService : Service() {

    lateinit var messenger: Messenger        //Service(외부)앱으로부터 메시지를 전달받을 메신져
    lateinit var replyMessenger: Messenger  //Service(외부)앱으로 메시지를 전달하는 메신져
    lateinit var player: MediaPlayer        //Service(외부)앱이 넘긴 메시지를 수행(음악 on/off/정보전달)

    //바인더 생성하는 클래스(메신저 바인더를 만들어서 재생)
    inner class IncomingHandler(
        context: Context,
        val applicationContext: Context = context.applicationContext
    ) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                10 -> {
                    Log.d("OutterforService", "${msg.what}")

                    replyMessenger = msg.replyTo //서비스로부터 리플라이메신저를 전달 받음

                    try {
                        if (!player.isPlaying) {
                            //음악재생하기
                            player = MediaPlayer.create(this@MyMessengerService, R.raw.song)
                            player.start()
                            //음원 정보를 번들로 담아 serviceForOuter 답장 보내기
                            val message = Message()
                            message.what = 10
                            val bundle = Bundle()
                            bundle.putInt("duration", player.duration)
                            message.obj = bundle
                            replyMessenger.send(message)
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d("OutterforService", " ???")
                    }
                }
                20 -> {
                    Log.d("OutterforService", "${msg.what} = ${msg.obj}")
                    if (!player.isPlaying) {
                        player.stop()
                    }
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

    override fun onCreate() {
        Log.d("OutterforService", "onCreate 성공")
        player = MediaPlayer()
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(IncomingHandler(this))
        Log.d("outerforservice", "onBind 성공")
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("outerforservice", "onUnBind 성공")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("outerforservice", "onDestroy 성공")
        player.release()
    }
}