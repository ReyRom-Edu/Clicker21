package com.example.clicker21

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.RoundingMode

object BigDecimalSerializer : KSerializer<BigDecimal>{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

fun BigDecimal.formatNumber(scale:Int = 0): String{
    val suffixes = " abcdefghijklmnopqrstuvwxyz"
    var count = 0
    var num = this

    while (num >= BigDecimal(1000) && count < suffixes.length){
        num/= BigDecimal(1000)
        count++
    }

    return "${num.setScale(scale, RoundingMode.FLOOR)}${suffixes[count]}"
}