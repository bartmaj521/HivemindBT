package com.majewski.hivemindbt.client.connection

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.data.ClientData
import java.util.*
import kotlin.collections.HashMap

class ClientConnection(private val mContext: Context,
                       private val clientData: ClientData,
                       private val clientCallbacks: ClientCallbacks?) {

    // bluetooth variables
    private val mBluetoothAdapter = (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val mBluetoothLeScanner: BluetoothLeScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }
    private var mGatt: BluetoothGatt? = null

    private val mScanResults = HashMap<String, BluetoothDevice>()
    private val mScanCallback = BleScanCallback(mScanResults) { clientCallbacks?.onServerFound(it)}
    private var gattClientCallback = GattClientCallback(clientData, clientCallbacks)

    private var mScanning = false

    fun askPermissions(): Boolean {
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled) {
            requestBluetoothEnable()
            return mBluetoothAdapter.isEnabled
        } else if (!hasLocationPermissions()){
            requestLocationPermission()
            return hasLocationPermissions()
        }
        return true
    }

    fun startScan(time: Long = 10000) {
        if(mScanning) {
            return
        }

        val filters = ArrayList<ScanFilter>()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Uuids.SERVICE_PRIMARY))
            .build()
        filters.add(scanFilter)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        mScanResults.clear()
        Log.d("HivemindClient", "attempting to start scan")


        mBluetoothLeScanner.startScan(filters, settings, mScanCallback)
        mScanning = true
        Handler().postDelayed({stopScan()}, time)
    }

    fun stopScan() {
        Log.d("HivemindClient", "Scan ended")
        if(mScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            mScanning = false
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        stopScan()
        Log.d("HivemindClient", "Connecting device")
        mGatt = device.connectGatt(mContext, false, gattClientCallback)
    }

    fun sendData(data: ByteArray) {
        mGatt?.let {
            val characteristic = it
                .getService(Uuids.SERVICE_PRIMARY)
                .getCharacteristic(UUID(0L, Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits + clientData.clientId))

            characteristic.value = byteArrayOf(clientData.clientId, 0).plus(data)
            it.writeCharacteristic(characteristic)
            //gattClientCallback.dataToSave = data
        }
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        (mContext as Activity).startActivityForResult(enableBtIntent, 1)
    }

    private fun hasLocationPermissions(): Boolean {
        return (mContext as Activity).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        (mContext as Activity).requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
    }
}