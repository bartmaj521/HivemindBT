package com.majewski.hivemindbt.client.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.data.SharedData
import java.util.*
import kotlin.collections.HashMap

internal class ClientConnection(
    private val mContext: Context,
    private val mClientData: SharedData,
    private val mClientCallbacks: ClientCallbacks?
) {

    // bluetooth variables
    private val mBluetoothAdapter = (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val mBluetoothLeScanner: BluetoothLeScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }
    private var mGatt: BluetoothGatt? = null

    private val mScanResults = HashMap<String, BluetoothDevice>()
    private val mScanCallback = BleScanCallback(mScanResults) { mClientCallbacks?.onServerFound(it) }
    private var mGattClientCallback = GattClientCallback(mClientData, mClientCallbacks)

    private var mScanning = false

    fun startScan(time: Long = 10000) {
        if (mScanning) {
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
        Handler().postDelayed({ stopScan() }, time)
    }

    fun stopScan() {
        Log.d("HivemindClient", "Scan ended")
        if (mScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            mScanning = false
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        stopScan()
        Log.d("HivemindClient", "Connecting device")
        mGatt = device.connectGatt(mContext, false, mGattClientCallback)
    }

    fun disconnect() {
        stopScan()
        mGatt?.disconnect()
        mGatt?.close()
        mGattClientCallback.mConnected = false
        mGattClientCallback.mInitialized = false
    }

    fun sendData(data: ByteArray, elementId: Byte) {
        mGatt?.let {
            val characteristic = it
                .getService(Uuids.SERVICE_PRIMARY)
                .getCharacteristic(UUID(0L, Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits + mClientData.clientId))

            characteristic.value = byteArrayOf(mClientData.clientId, elementId).plus(data)
            it.writeCharacteristic(characteristic)
        }
    }
}