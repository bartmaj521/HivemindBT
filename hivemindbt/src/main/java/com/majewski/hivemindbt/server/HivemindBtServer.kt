package com.majewski.hivemindbt.server

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import java.util.*
import kotlin.collections.HashMap

class HivemindBtServer(private val mContext: Context) {

    companion object {
        val SERVICE_UUID = UUID(0L, 300L)
        val CHARACTERISTIC_CLIENT_ID_UUID = UUID(0L, 301L)
    }

    // Clients variables
    private val mConnectedDevices = ArrayList<BluetoothDevice>()
    private val mClientsAddresses = HashMap<String, Int>()
    private var currentClientId = 0

    // Bluetooth variables
    private val mBluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val mBluetoothAdapter = mBluetoothManager.adapter
    private lateinit var mBluetoothAdvertiser: BluetoothLeAdvertiser
    private lateinit var mGattServer: BluetoothGattServer

    private val mAdvertiseCallback = object: AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("bleTest", "Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("bleTest", "Peripheral advertising failed: $errorCode")
        }
    }

    fun enableBt(): Boolean {
        return if (mBluetoothAdapter.isEnabled) {
            true
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            mContext.startActivity(enableBtIntent)
            mBluetoothAdapter.isEnabled
        }
    }

    fun startServer() {
        mBluetoothAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
        mGattServer = mBluetoothManager.openGattServer(mContext, GattServerCallback(mConnectedDevices))
        setupServer()
        startAdvertising()
    }

    fun pauseServer() {
        stopAdvertising()
        stopServer()
    }

    private fun stopServer() {
        mGattServer.close()
    }

    private fun stopAdvertising() {
        mBluetoothAdvertiser.stopAdvertising(mAdvertiseCallback)
    }

    private fun setupServer() {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val clientIdCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_CLIENT_ID_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(clientIdCharacteristic)

        mGattServer.addService(service)
    }

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()
        val parcelUUID = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(parcelUUID)
            .build()
        mBluetoothAdvertiser.startAdvertising(settings, data, mAdvertiseCallback)
        Log.d("HivemindServer", "Started server")
    }

    private inner class GattServerCallback(private val mConnectedDevices: ArrayList<BluetoothDevice>) : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if(mClientsAddresses[device.address] == null) {
                    currentClientId++
                    mClientsAddresses[device.address] = currentClientId
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
                mGattServer.sendResponse(device, requestId, 0, 0, byteArrayOf(mClientsAddresses[device?.address]?.toByte() ?: 0))
            }
        }
    }
}