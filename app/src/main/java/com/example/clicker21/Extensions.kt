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

@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString()) // Сохраняем как строку
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString()) // Читаем из строки
    }
}

fun BigDecimal.formatNumber(): String {
    if (this < BigDecimal(1000)) return this.setScale(1, RoundingMode.FLOOR).toPlainString() // Обычный вывод для маленьких чисел

    val suffixes = "abcdefghijklmnopqrstuvwxyz"
    var count = 0
    var num = this

    while (num >= BigDecimal(1000) && count < suffixes.length - 1) {
        num /= BigDecimal(1000)
        count++
    }

    return "${num.setScale(1, RoundingMode.FLOOR)}${suffixes[count - 1]}"
}
