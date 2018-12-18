package com.majewski.hivemindbtexample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.server.HivemindBtServer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var server: HivemindBtServer? = null
    private var client: HivemindBtClient? = null

    private var lol: Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_server.setOnClickListener {
            server = HivemindBtServer(this)
            if(server?.enableBt() != true) finish()
            server?.startServer()
            server?.onDataReceived =  {
                runOnUiThread {
                    Toast.makeText(this, "New data: ${it}", Toast.LENGTH_SHORT).show()
                    lol = it
                }
            }
        }

        btn_client.setOnClickListener {
            client = HivemindBtClient(this)
            if(client?.askPermissions() != true) finish()
            client?.onDataChanged = {
                runOnUiThread {
                    Toast.makeText(this, "New data: ${it}", Toast.LENGTH_SHORT).show()
                    lol = it as Byte
                }
            }
            client?.startScan()
        }

        btn_send_data.setOnClickListener {
            server?.let {
                lol++
                it.sendData(lol)
                Toast.makeText(this, "lol: $lol", Toast.LENGTH_SHORT).show()
            }

            client?.let {
                lol++
                it.sendData(lol)
                Toast.makeText(this, "lol: $lol", Toast.LENGTH_SHORT).show()
            }

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
