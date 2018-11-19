package com.majewski.hivemindbt.server.connection

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

class ServerConnection(private val mContext: Context) {

    companion object {
        private val SERVICE_UUID = UUID(0L, 300L)
        private val CHARACTERISTIC_CLIENT_ID_UUID = UUID(0L, 301L)
    }

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
        val gattServerCallback = GattServerCallback()
        mGattServer = mBluetoothManager.openGattServer(mContext, gattServerCallback)
        gattServerCallback.gattServer = mGattServer
        setupServer()
        startAdvertising()
    }

    fun stopServer() {
        mBluetoothAdvertiser.stopAdvertising(mAdvertiseCallback)
        mGattServer.close()
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
}