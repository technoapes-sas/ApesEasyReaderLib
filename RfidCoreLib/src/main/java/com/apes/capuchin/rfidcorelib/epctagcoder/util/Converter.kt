package com.apes.capuchin.rfidcorelib.epctagcoder.util

import java.math.BigInteger
import kotlin.math.floor
import kotlin.math.log10

object Converter {

    private fun lPadZero(value: Int, fill: Int): String {
        val isNegative = value < 0
        val positiveValue = if (isNegative) -value else value

        val length =
            if (positiveValue == 0) 1 else floor(log10(positiveValue.toDouble())).toInt() + 1

        val stringBuilder = StringBuilder()
        if (isNegative) {
            stringBuilder.append('-')
        }

        for (i in 0 until fill - length) {
            stringBuilder.append('0')
        }

        stringBuilder.append(value)

        return stringBuilder.toString()
    }

    fun String.convertBinToBit(fromBit: Int, toBit: Int): String {
        val regex = "(?<=\\G.{${fromBit}})".toRegex()
        val split = this.split(regex)

        val builder = StringBuilder()
        for (chunk in split) {
            val intChunk = chunk.toInt(2)
            builder.append(lPadZero(intChunk, toBit))
        }

        return builder.toString()
    }

    fun String.hexToBin(): String {
        val hexValue = this.uppercase()
        val binaryBuilder = StringBuilder()
        for (char in hexValue) {
            val binaryEquivalent = Integer.toBinaryString(char.digitToInt(16))
            binaryBuilder.append(
                String.format("%4s", binaryEquivalent).replace(" ", "0")
            )
        }
        return binaryBuilder.toString()
    }

    fun String.binToHex(): String {
        val hexBuilder = StringBuilder()
        for (i in this.indices step 4) {
            val binaryChunk = this.substring(i, i + 4)
            val decimalValue = Integer.parseInt(binaryChunk, 2)
            hexBuilder.append(Character.forDigit(decimalValue, 16))
        }
        return hexBuilder.toString().uppercase()
    }

    fun String.stringToBin(bits: Int): String {
        val builder = StringBuilder()
        for (char in this) {
            val binString = Integer.toBinaryString(char.digitToInt()).padStart(bits, '0')
            builder.append(binString)
        }
        return builder.toString()
    }

    fun String.binToString(): String {
        val sb = StringBuilder()
        for (i in this.indices step 8) {
            val binaryChunk = this.substring(i, i + 8)
            val byteValue = Integer.parseInt(binaryChunk, 2)
            sb.append(byteValue)
        }
        return sb.toString().trim()
    }

    fun Int.decToBin(): String {
        return Integer.toBinaryString(this)
    }

    fun Int.decToBin(bits: Int): String {
        return BigInteger.valueOf(this.toLong()).toString(2).padStart(bits, '0')
    }

    fun String.decToBin(bits: Int): String {
        return BigInteger(this).toString(2).padStart(bits, '0')
    }

    fun String.binToDec(): String {
        return this.toBigInteger(2).toString()
    }

    fun String.splitEqually(size: Int): List<String> {
        val chunks = mutableListOf<String>()
        for (start in this.indices step size) {
            val end = minOf(this.length, start + size)
            chunks.add(this.substring(start, end))
        }
        return chunks.toList()
    }

    fun String.isNumeric(): Boolean {
        return this.all { it.isDigit() }
    }

    fun String.fill(size: Int): String {
        val builder = StringBuilder(this)
        repeat(size - this.length) { builder.append('0') }
        return builder.toString()
    }

    fun String.strZero(length: Int): String {
        return StringBuilder().apply {
            repeat(length - this.length) { append('0') }
            append(this)
        }.toString()
    }
}