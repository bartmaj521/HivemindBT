package com.majewski.hivemindbt.server

import android.content.Context
import com.majewski.hivemindbt.server.connection.ServerConnection


class HivemindBtServer(mContext: Context,
                       private val serverCallbacks: ServerCallbacks? = null) {

    var onDataReceived: ((Byte) -> Unit)? = null
        set(value) {
            mServerConnection.onDataReceived = value
        }

    private val mServerConnection = ServerConnection(mContext, serverCallbacks)

    fun enableBt() = mServerConnection.enableBt()

    fun startServer() {
        mServerConnection.startServer()
    }

    fun stopServer() {
        mServerConnection.stopServer()
    }

    fun sendData(data: Byte) {
        mServerConnection.sendData(data)
    }
}