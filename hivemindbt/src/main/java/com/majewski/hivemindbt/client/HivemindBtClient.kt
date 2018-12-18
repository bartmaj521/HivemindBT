package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.connection.ClientConnection
import com.majewski.hivemindbt.client.data.ClientData

class HivemindBtClient(context: Context) {

    var onDataChanged: ((data: Any)->Unit)? = null
    set(value) {
        mClientConnection.onDataChanged = value
    }

    private val data = ClientData()
    private val mClientConnection = ClientConnection(context, data)

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

    fun sendData(data: Byte) {
        mClientConnection.sendData(data)
    }
}