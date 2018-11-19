package com.majewski.hivemindbt.client.connection

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.client.data.ClientData

class GattClientCallback(private val mClientData: ClientData): BluetoothGattCallback() {

    private var mConnected = false
    private var mInitialized = false

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        gatt?.let {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer(it)
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true
                it.discoverServices()
                Log.d("HivemindClient", "Device connected")
                return
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer(it)
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if(status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        mInitialized = true
        Log.d("HivemindClient", "Services discovered")
        gatt?.let {
            requestClientId(it)
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)

        if(characteristic.uuid == Uuids.CHARACTERISTIC_CLIENT_ID_UUID) {
            saveClientId(characteristic.value[0])
        }

    }

    private fun requestClientId(gatt: BluetoothGatt) {
        val characteristic = gatt.getService(Uuids.SERVICE_PRIMARY_UUID)?.getCharacteristic(
            Uuids.CHARACTERISTIC_CLIENT_ID_UUID
        )
        gatt.readCharacteristic(characteristic)
    }

    private fun saveClientId(id: Byte) {
        mClientData.clientId = id
        Log.d("HivemindClient", "Connected, client id = $id")
    }

    private fun disconnectGattServer(gatt: BluetoothGatt) {
        mConnected = false
        mInitialized = false
        gatt.disconnect()
        gatt.close()
    }
}



