package com.majewski.hivemindbt.server.connection

import android.bluetooth.*
import android.util.Log
import java.util.*

class GattServerCallback : BluetoothGattServerCallback() {

    companion object {
        private val SERVICE_UUID = UUID(0L, 300L)
        private val CHARACTERISTIC_CLIENT_ID_UUID = UUID(0L, 301L)
    }

    var gattServer: BluetoothGattServer? = null

    private val mConnectedDevices = ArrayList<BluetoothDevice>()
    private val mClientsAddresses = HashMap<String, Int>()
    private var mCurrentClientId = 0

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            if(mClientsAddresses[device.address] == null) {
                mCurrentClientId++
                mClientsAddresses[device.address] = mCurrentClientId
            }
            mConnectedDevices.add(device)
            Log.d("HivemindServer", "Device connected")
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectedDevices.remove(device)
            Log.d("HivemindServer", "Device disconnected")
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        if(characteristic?.uuid == CHARACTERISTIC_CLIENT_ID_UUID) {
            Log.d("HivemindServer", "ReadRequest, id: ${mClientsAddresses[device?.address]?.toByte()}")
            gattServer?.sendResponse(device, requestId, 0, 0, byteArrayOf(mClientsAddresses[device?.address]?.toByte() ?: 0))
        }
    }
}