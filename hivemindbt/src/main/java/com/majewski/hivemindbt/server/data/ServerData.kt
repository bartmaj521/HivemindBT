package com.majewski.hivemindbt.server.data

import com.majewski.hivemindbt.data.SharedElement

class ServerData(val maxNbOfClients: Byte) {

    var nbOfClients: Byte = 0
    var clientId: Byte = 0
}