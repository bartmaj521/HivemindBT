package com.majewski.hivemindbt.data

class SharedData {

    var nbOfClients: Byte = 0
    internal set

    var clientId: Byte = 0
    internal set

    private val clients = HashMap<Byte, HashMap<Byte, ByteArray>>()

    private val nameIdDictionary = HashMap<String, Byte>()

    fun addElement(name: String, id: Byte) {
        clients[id] = HashMap()
        nameIdDictionary[name] = id
    }

    fun setElementValue(elementId: Byte, value: ByteArray) {
        val elementData = clients[elementId] ?: throw NoSuchElementException("Element with given id not found.")
        elementData[clientId] = value
}

    fun getElementValue(name: String, clientId: Byte): ByteArray {
        val elementId = nameIdDictionary[name] ?: throw NoSuchElementException("Element with given name not found.")
        return getElementValue(elementId, clientId)
    }

    fun getElementValue(elementId: Byte, clientId: Byte): ByteArray {
        val elementData = clients[elementId] ?: throw NoSuchElementException("Element with given id not found.")
        return elementData[clientId] ?: throw NoSuchElementException("Element with given clientId not found.")
    }

    fun getElementId(name: String) = nameIdDictionary[name]

    fun setElementValueFromClient(elementId: Byte, clientId: Byte, value: ByteArray) {
        clients[elementId]?.put(clientId, value)
    }
}

