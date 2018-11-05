package com.majewski.hivemindbt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager

class HivemindBt {

    companion object {
        fun isSupported(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                    && (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isMultipleAdvertisementSupported
        }

    }

}
