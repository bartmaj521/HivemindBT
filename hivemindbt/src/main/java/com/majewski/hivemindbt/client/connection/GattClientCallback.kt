package com.majewski.hivemindbt.client.connection

import android.bluetooth.*
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.data.ClientData
import com.majewski.hivemindbt.data.ReceivedElement
import java.util.*

class GattClientCallback(private val mClientData: ClientData,
                         private val clientCallbacks: ClientCallbacks?): BluetoothGattCallback() {

    var dataToSave: Byte? = null

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
            val dataReadCharacteristic = primaryService.getCharacteristic(Uuids.CHARACTERISTIC_READ_DATA)
            gatt.setCharacteristicNotification(nbOfClientsCharacteristic, true)
            gatt.setCharacteristicNotification(dataReadCharacteristic, true)
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
            setCharacteristicWriteType(gatt)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        dataToSave?.let {
            descriptor?.characteristic?.value = byteArrayOf(it)
            gatt?.writeCharacteristic(descriptor?.characteristic)
            dataToSave = null
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        when(characteristic?.uuid) {
            Uuids.CHARACTERISTIC_NB_OF_CLIENTS -> saveNbOfClients(characteristic.value[0])
            Uuids.CHARACTERISTIC_READ_DATA -> dataChanged(characteristic)
        }
    }

    private fun setCharacteristicWriteType(gatt: BluetoothGatt) {
        val dataWriteCharacteristic = gatt.getService(Uuids.SERVICE_PRIMARY).getCharacteristic(UUID(0L, Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits + mClientData.clientId))
        dataWriteCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    }

    private fun dataChanged(characteristic: BluetoothGattCharacteristic) {
        Log.d("HivemindClient", "Data received: ${characteristic.value[0]}")
        val recv = ReceivedElement(characteristic.value[0], characteristic.value[1], byteArrayOf(characteristic.value[2]))
        clientCallbacks?.onDataChanged(recv)
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
        clientCallbacks?.onConnectedToServer(id)
        Log.d("HivemindClient", "Connected, client id = $id")
    }

    private fun saveNbOfClients(nbOfClients: Byte) {
        mClientData.nbOfClients = nbOfClients
        Log.d("HivemindClient", "Number of clients changed = ${mClientData.nbOfClients}")
        clientCallbacks?.onNumberOfClientsChanged(nbOfClients)
    }

    private fun disconnectGattServer(gatt: BluetoothGatt) {
        mConnected = false
        mInitialized = false
        gatt.disconnect()
        gatt.close()
    }
}



