package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.connection.ClientConnection
import com.majewski.hivemindbt.client.data.ClientData

class HivemindBtClient(context: Context) {

    val clientId: Byte
        get() = mClientData.clientId


    private val mClientData = ClientData()
    private val mClientConnection = ClientConnection(context, mClientData)


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
}