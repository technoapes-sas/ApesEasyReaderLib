package com.apes.capuchin.rfidcorelib.epctagcoder.util

import java.math.BigInteger

object Converter {

    private fun lPadZero(value: Int, fill: Int): String = buildString {
        val isNegative = value < 0

        if (isNegative) {
            append('-')
        }

        val binaryString = Integer.toBinaryString(value)
        append(binaryString.padStart(fill, '0'))
    }

    fun String.convertBinToBit(fromBit: Int, toBit: Int): String {
        val regex = "(?<=\\G.{${fromBit}})".toRegex()
        val split = this.split(regex)

        return buildString {
            for (chunk in split) {
                if (chunk.isNotEmpty()) {
                    val intChunk = chunk.toInt(2)
                    append(lPadZero(intChunk, toBit))
                }
            }
        }
    }

    fun String.hexToBin(): String {
        val hexValue = this.uppercase()

        return buildString {
            for (char in hexValue) {
                val binaryEquivalent = Integer.toBinaryString(char.digitToInt(16))
                append(binaryEquivalent.padStart(4, '0'))
            }
        }
    }

    fun String.binToHex(): String {
        return this.chunked(4)
            .joinToString("") {
                Character.forDigit(Integer.parseInt(it, 2), 16).toString()
            }
            .uppercase()
    }

    fun String.stringToBin(bits: Int): String {
        val value =  this.toInt()
        return buildString {
            val binaryString = Integer.toBinaryString(value)
            append(binaryString.padStart(bits, '0'))
        }
    }

    fun String.binToString(): String {
        val value = this
        return buildString {
            for (i in value.indices step 8) {
                val binaryChunk = value.substring(i, i + 8)
                val byteValue = Integer.parseInt(binaryChunk, 2)
                append(byteValue.toString())
            }
        }.trim()
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
        return buildString {
            append(this@fill)
            repeat(size - this@fill.length) { append('0') }
        }
    }

    fun String.strZero(length: Int): String {
        return buildString {
            repeat(length - this@strZero.length) { append('0') }
            append(this@strZero)
        }
    }
}