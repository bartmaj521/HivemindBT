package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.connection.ClientConnection
import com.majewski.hivemindbt.data.SharedData

class HivemindBtClient(context: Context, clientCallbacks: ClientCallbacks? = null) {

    private val mClientData = SharedData()
    private val mClientConnection = ClientConnection(context, mClientData, clientCallbacks)

    val clientId: Byte
    get() = mClientData.nbOfClients

    val numberOfClients: Byte
    get() = mClientData.nbOfClients

    fun startScan() {
        mClientConnection.startScan()
    }

    fun stopScan() {
        mClientConnection.stopScan()
    }

    fun connectDevice(device: BluetoothDevice) {
        mClientConnection.connectDevice(device)
    }

    fun disconnect() {
        mClientConnection.disconnect()
    }

    fun addData(name: String, id: Byte) {
        mClientData.addElement(name, id)
    }

    fun sendData(data: ByteArray, elementName: String) {
        val elementId = mClientData.getElementId(elementName) ?: throw NoSuchElementException()
        mClientData.setElementValue(elementId, data)
        mClientConnection.sendData(data, elementId)
    }

    fun getData(elementName: String, clientId: Byte): ByteArray? {
        return mClientData.getElementValue(elementName, clientId)
    }
}