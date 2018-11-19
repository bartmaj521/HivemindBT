package com.majewski.hivemindbtexample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.server.HivemindBtServer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var server: HivemindBtServer? = null
    private var client: HivemindBtClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_server.setOnClickListener {
            server = HivemindBtServer(this)
            if(server?.enableBt() != true) finish()
            server?.startServer()
        }

        btn_client.setOnClickListener {
            client = HivemindBtClient(this)
            if(client?.askPermissions() != true) finish()
            client?.startScan()
        }
    }

    override fun onResume() {
        super.onResume()
        server?.startServer()
    }

    override fun onPause() {
        super.onPause()
        server?.stopServer()
    }
}
