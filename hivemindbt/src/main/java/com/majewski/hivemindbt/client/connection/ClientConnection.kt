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
import com.majewski.hivemindbt.client.data.ClientData
import java.util.*
import kotlin.collections.HashMap

class ClientConnection(private val mContext: Context, private val mClientData: ClientData) {

    companion object {
        val SERVICE_UUID = UUID(0L, 300L)
    }

    // bluetooth variables
    private val mBluetoothAdapter = (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val mBluetoothLeScanner: BluetoothLeScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }
    private var mGatt: BluetoothGatt? = null

    private val mScanResults = HashMap<String, BluetoothDevice>()
    private val mScanCallback = BleScanCallback(mScanResults) { connectDevice(it)}

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
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
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
        val gattClientCallback = GattClientCallback(mClientData)
        mGatt = device.connectGatt(mContext, false, gattClientCallback)
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