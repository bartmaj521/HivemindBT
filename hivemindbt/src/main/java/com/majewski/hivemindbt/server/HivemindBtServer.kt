package com.majewski.hivemindbt.server

import android.content.Context
import com.majewski.hivemindbt.server.connection.ServerConnection


class HivemindBtServer(mContext: Context,
                       private val serverCallbacks: ServerCallbacks? = null) {

    private val mServerConnection = ServerConnection(mContext, serverCallbacks)

    fun startServer() {
        mServerConnection.startServer()
    }

    fun stopServer() {
        mServerConnection.stopServer()
    }

    fun sendData(data: ByteArray) {
        mServerConnection.sendData(data)
    }
}