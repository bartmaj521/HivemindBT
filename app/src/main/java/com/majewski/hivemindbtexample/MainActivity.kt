package com.majewski.hivemindbtexample

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindbt.server.HivemindBtServer
import com.majewski.hivemindbt.server.ServerCallbacks
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var server: HivemindBtServer? = null
    private var client: HivemindBtClient? = null

    private var lol: Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_server.setOnClickListener {
            server = HivemindBtServer(this, serverCallbacks)
            if(server?.enableBt() != true) finish()
            server?.startServer()
        }

        btn_client.setOnClickListener {
            client = HivemindBtClient(this, clientCallbacks)
            if(client?.askPermissions() != true) finish()
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

    val serverCallbacks = object : ServerCallbacks {
        override fun onClientConnected(nbOfClients: Byte) {
            Log.d("lolol", "Client connected")
        }

        override fun onClientDisconnected(clientId: Byte) {
            Log.d("lolol", "Client disconnected")
        }

        override fun onDataChanged(data: ReceivedElement) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "New data: ${data.data[0]}", Toast.LENGTH_SHORT).show()
                lol = data.data[0]
            }
        }

    }

    val clientCallbacks = object : ClientCallbacks {
        override fun onServerFound(device: BluetoothDevice) {
            client?.connectDevice(device)
        }

        override fun onConnectedToServer(clientId: Byte) {
            Log.d("lolol", "Connected to server")
        }

        override fun onNumberOfClientsChanged(newNumberOfClients: Byte) {
            Log.d("lolol", "Nb of clients changed")
        }

        override fun onDataChanged(data: ReceivedElement) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "New data: ${data.data[0]}", Toast.LENGTH_SHORT).show()
                lol = data.data[0]
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
