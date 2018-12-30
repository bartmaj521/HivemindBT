package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.connection.ClientConnection
import com.majewski.hivemindbt.client.data.ClientData

class HivemindBtClient(context: Context, private val clientCallbacks: ClientCallbacks? = null) {

    private val data = ClientData()
    private val mClientConnection = ClientConnection(context, data, clientCallbacks)

    fun askPermissions(): Boolean = mClientConnection.askPermissions()

    fun startScan() {
        mClientConnection.startScan()
    }

    fun stopScan() {
        mClientConnection.stopScan()
    }

    fun connectDevice(device: BluetoothDevice) {
        mClientConnection.connectDevice(device)
    }

    fun sendData(data: ByteArray) {
        mClientConnection.sendData(data)
    }
}