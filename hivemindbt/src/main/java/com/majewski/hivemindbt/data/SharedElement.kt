package com.majewski.hivemindbt.data

class SharedElement<T>(val direction: Direction) {

    private val data = HashMap<Byte, T?>()

    fun getDataFromClient(id: Byte): T? {
        return data[id]
    }

    fun setData(value: Any, id:Byte) {

    }

    enum class Direction{
        TO_SERVER,
        FROM_SERVER,
        TO_CLIENTS
    }
}