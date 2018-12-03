package com.majewski.hivemindbt.server.connection

import android.bluetooth.*
import android.util.Log
import com.majewski.hivemindbt.Uuids
import java.util.*

class GattServerCallback : BluetoothGattServerCallback() {

    var gattServer: BluetoothGattServer? = null

    private val mConnectedDevices = ArrayList<BluetoothDevice>()
    private val mClientsAddresses = HashMap<String, Byte>()
    private var nbOfClients: Byte = 0

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
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

        if(characteristic?.uuid == Uuids.CHARACTERISTIC_CLIENT_ID) {
            device?.let {
                if (mClientsAddresses[device.address] == null) {
                    addNewClient(device)
                }
                Log.d("HivemindServer", "ID ReadRequest, id: ${mClientsAddresses[device.address]}")
                gattServer?.sendResponse(device, requestId, 0, 0, byteArrayOf(mClientsAddresses[device.address] ?: 0))
            }
        }
    }

    private fun addNewClient(device: BluetoothDevice) {
        nbOfClients++
        mClientsAddresses[device.address] = nbOfClients
        val nbOfClientsCharacteristic = gattServer?.getService(Uuids.SERVICE_PRIMARY)?.getCharacteristic(Uuids.CHARACTERISTIC_NB_OF_CLIENTS)
        nbOfClientsCharacteristic?.value = byteArrayOf(nbOfClients)
        Log.d("HivemindServer", "Number of connected devices: ${mConnectedDevices.size}")
        for(d in mConnectedDevices) {
            Log.d("HivemindServer", "Notifying device ${d.name}")
            gattServer?.notifyCharacteristicChanged(d, nbOfClientsCharacteristic, false)
        }
        Log.d("HivemindServer", "Client connected, number of clients = $nbOfClients")
    }
}