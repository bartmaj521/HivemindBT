package com.majewski.hivemindbt.server

import android.content.Context
import com.majewski.hivemindbt.server.connection.ServerConnection


class HivemindBtServer(mContext: Context) {

    private val mServerConnection = ServerConnection(mContext)

    fun enableBt() = mServerConnection.enableBt()

    fun startServer() {
        mServerConnection.startServer()
    }

    fun stopServer() {
        mServerConnection.stopServer()
    }
}