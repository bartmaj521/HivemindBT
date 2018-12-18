package com.majewski.hivemindbt

import java.io.*

fun Any.toByteArray(): ByteArray {
    val out: ObjectOutputStream
    val bos = ByteArrayOutputStream()
    try {
        out = ObjectOutputStream(bos)
        out.writeObject(this)
        out.flush()
        return bos.toByteArray()
    } finally {
        try {
            bos.close()
        } catch (t:Throwable) {}
    }
}

inline fun <reified T>ByteArray.fromByteArray(): T?{
    val bis = ByteArrayInputStream(this)
    var ina: ObjectInput? = null
    try {
        ina = ObjectInputStream(bis)
        val o = ina.readObject()
        return if(o is T){
            o
        } else {
            null
        }
    } finally {
        try {
            ina?.close()
        } catch (ex: IOException) {}
    }
}