package com.majewski.hivemindbtexample

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import com.majewski.hivemindbt.HivemindBt
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
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                return@setOnClickListener
            }
            server = HivemindBtServer(this, serverCallbacks)
            server?.addData("test", 0)
            server?.addData("seekbar", 1)
            server?.startServer()
        }

        btn_client.setOnClickListener {
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                return@setOnClickListener
            }
            client = HivemindBtClient(this, clientCallbacks)
            client?.addData("test", 0)
            client?.addData("seekbar", 1)
            client?.startScan()
        }

        btn_send_data.setOnClickListener {
            server?.let {
                lol++
                it.sendData("testServer".toByteArray(), "test")
                Toast.makeText(this, "lol: $lol", Toast.LENGTH_SHORT).show()
            }

            client?.let {
                lol++
                it.sendData("testClient".toByteArray(), "test")
                Toast.makeText(this, "lol: $lol", Toast.LENGTH_SHORT).show()
            }
        }

        btn_cached_data.setOnClickListener {
            val text = et_clientid.text
            if (text != null) {
                server?.let {
                    Toast.makeText(
                        this,
                        String(it.getData("test", text.toString().toByte()) ?: byteArrayOf()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                client?.let {
                    Toast.makeText(
                        this,
                        String(it.getData("test", text.toString().toByte()) ?: byteArrayOf()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        sb_test.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var touching = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (touching) {
                    server?.sendData(byteArrayOf(progress.toByte()), "seekbar")
                    client?.sendData(byteArrayOf(progress.toByte()), "seekbar")
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                touching = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                touching = false
            }

        })
    }

    private val serverCallbacks = object : ServerCallbacks {

        override fun onClientConnected(nbOfClients: Byte) {
            Log.d("lolol", "Client connected")
        }

        override fun onClientDisconnected(clientId: Byte) {
            Log.d("lolol", "Client disconnected")
        }

        override fun onDataChanged(data: ReceivedElement) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "New data: ${String(data.data)}, from ${data.from}, with name: ${data.name}",
                    Toast.LENGTH_SHORT
                ).show()
                lol = data.data[0]
            }
        }

        override fun onServerStarted() {
            Log.d("lolol", "Server started")
        }

        override fun onServerFailed(errorCode: Int) {
            Log.d("lolol", "Server failed")
        }

    }

    private val clientCallbacks = object : ClientCallbacks {
        override fun onServerFound(device: BluetoothDevice) {
            client?.stopScan()
            client?.connectDevice(device)
        }

        override fun onConnectedToServer(clientId: Byte) {
            Log.d("lolol", "Connected to server")
        }

        override fun onDisconnectedFromServer(device: BluetoothDevice) {
            Log.d("lolol", "Disconnected from server")
        }

        override fun onNumberOfClientsChanged(newNumberOfClients: Byte) {
            Log.d("lolol", "Nb of clients changed")
        }

        override fun onDataChanged(data: ReceivedElement) {
            runOnUiThread {
                if (data.dataId == 1.toByte()) {
                    sb_test.progress = data.data[0].toInt()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "New data: ${String(data.data)}, from ${data.from}, with name: ${data.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    lol = data.data[0]
                }
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
