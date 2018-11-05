package com.majewski.hivemindbtexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.majewski.hivemindbt.server.HivemindBtServer


class MainActivity : AppCompatActivity() {

    private lateinit var server: HivemindBtServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        server = HivemindBtServer(this)
        server.enableBt()
    }

    override fun onResume() {
        super.onResume()
        server.startServer()
    }

    override fun onPause() {
        super.onPause()
        server.pauseServer()
    }
}
