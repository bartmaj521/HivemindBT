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
                Log.d("HivemindClient", "Server offline")
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
            val primaryService = gatt.getService(Uuids.SERVICE_PRIMARY)
            val nbOfClientsCharacteristic = primaryService.getCharacteristic(Uuids.CHARACTERISTIC_NB_OF_CLIENTS)
            gatt.setCharacteristicNotification(nbOfClientsCharacteristic, true)
            requestClientId(it)
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)

        if(characteristic.uuid == Uuids.CHARACTERISTIC_CLIENT_ID) {
            saveClientId(characteristic.value[0])
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        if(characteristic?.uuid == Uuids.CHARACTERISTIC_NB_OF_CLIENTS) {
            saveNbOfClients(characteristic.value[0])
        }
    }

    private fun requestClientId(gatt: BluetoothGatt) {
        val clientIdCharacteristic = gatt.getService(Uuids.SERVICE_PRIMARY)?.getCharacteristic(
            Uuids.CHARACTERISTIC_CLIENT_ID
        )
        Log.d("HivemindClient", "Requesting client id")
        gatt.readCharacteristic(clientIdCharacteristic)
    }

    private fun saveClientId(id: Byte) {
        mClientData.clientId = id
        Log.d("HivemindClient", "Connected, client id = $id")
    }

    private fun saveNbOfClients(nbOfClients: Byte) {
        mClientData.nbOfClients = nbOfClients
        Log.d("HivemindClient", "Number of clients changed = ${mClientData.nbOfClients}")
    }

    private fun disconnectGattServer(gatt: BluetoothGatt) {
        mConnected = false
        mInitialized = false
        gatt.disconnect()
        gatt.close()
    }
}



