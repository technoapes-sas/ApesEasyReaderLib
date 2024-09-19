package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINHeaderEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.partitiontable.SGTINPartitionTableList
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ChoiceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.result.SGTIN
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToDec
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToHex
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToString
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.convertBinToBit
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.decToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.fill
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.hexToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.strZero
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.stringToBin
import com.apes.capuchin.rfidcorelib.utils.EMPTY_STRING
import java.util.regex.Pattern
import kotlin.math.ceil

class ParseSGTIN(steps: StepsSGTIN) {

    var companyPrefix: String = steps.companyPrefix.orEmpty()
    var itemReference: String = steps.itemReference.orEmpty()
    var serial: String = steps.serial.orEmpty()
    private var rfidTag: String = steps.rfidTag.orEmpty()
    private var epcTagURI: String = steps.epcTagURI.orEmpty()
    private var epcPureIdentityURI: String = steps.epcPureIdentityURI.orEmpty()
    private var remainder: Int = steps.remainder ?: 0

    var extensionDigit = steps.extensionDigit ?: SGTINExtensionDigitEnum.EXTENSION_0
    private var prefixLength = steps.prefixLength ?: PrefixLengthEnum.DIGIT_6
    private var tagSize = steps.tagSize ?: SGTINTagSizeEnum.BITS_96
    private var filterValue = steps.filterValue ?: SGTINFilterValueEnum.ALL_OTHERS_0
    private lateinit var tableItem: TableItem

    private val sgtin: SGTIN = SGTIN()

    companion object {
        const val EPC_TAG_URI_PATTERN =
            "(urn:epc:tag:sgtin-)(96|198):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(\\w+)"
        const val EPC_PURE_IDENTITY_URI_PATTERN =
            "(urn:epc:id:sgtin):(\\d+)\\.([0-8])(\\d+)\\.(\\w+)"
        const val EPC_SCHEME = "sgtin"
        const val APPLICATION_IDENTIFIER = "AI 414 + AI 254"
        const val EPC_PURE_IDENTITY_URI_FORMAT = "urn:epc:id:sgtin:%s.%s%s.%s"
        const val EPC_TAG_URI_FORMAT = "urn:epc:tag:sgtin-%s:%s.%s.%s%s.%s"
        const val EPC_RAW_URI_FORMAT = "urn:epc:raw:%s.x%s"

        fun builder(): ChoiceStep = StepsSGTIN()
    }

    init {
        parse()
    }

    fun handleParseWithRfidTag(sgtinPartitionTableList: SGTINPartitionTableList) {

        val inputBin = rfidTag.hexToBin()
        val headerBin = inputBin.substring(0, 8)
        val filterBin = inputBin.substring(8, 11)
        val partitionBin = inputBin.substring(11, 14)

        tagSize = SGTINTagSizeEnum.findByValue(SGTINHeaderEnum.findByValue(headerBin).getTagSize())
        require(tagSize != SGTINTagSizeEnum.BITS_96) { "Tag size is invalid" }

        tableItem = sgtinPartitionTableList.getPartitionByValue(partitionBin.binToDec().toInt())

        val filterDec = filterBin.toInt(2)

        val companyPrefixBin = inputBin.substring(14, 14 + tableItem.m)
        require(companyPrefixBin.length == tableItem.m) { "Company Prefix is invalid" }

        val itemReferenceWithExtensionBin = inputBin.substring(
            14 + tableItem.m,
            14 + tableItem.m + tableItem.n
        )
        require(itemReferenceWithExtensionBin.length == tableItem.n) { "Item Reference is invalid" }

        var serialBin = inputBin.substring(14 + tableItem.m + tableItem.n)
        require(serialBin.length == tagSize.getSerialBitCount()) { "Serial is invalid" }

        val companyPrefixDec = companyPrefixBin.binToDec()
        val itemReferenceWithExtensionDec = itemReferenceWithExtensionBin
            .binToDec()
            .strZero(tableItem.digits)
        val extensionDec = itemReferenceWithExtensionDec.substring(0, 1)

        itemReference = itemReferenceWithExtensionDec.substring(1)
        serial = when (tagSize.getSerialBitCount()) {
            140 -> {
                serialBin = serialBin.convertBinToBit(7, 8)
                serialBin.binToString()
            }

            else -> serialBin.binToDec()
        }
        companyPrefix = companyPrefixDec.strZero(tableItem.l)
        extensionDigit = SGTINExtensionDigitEnum.findByValue(extensionDec.toInt())
        filterValue = SGTINFilterValueEnum.findByValue(filterDec)
        prefixLength = PrefixLengthEnum.findByCode(tableItem.l)
    }

    fun handleParseWithoutRfidTag(sgtinPartitionTableList: SGTINPartitionTableList) {
        tableItem = sgtinPartitionTableList.getPartitionByL(prefixLength.value)
        when {
            companyPrefix.isEmpty() -> {
                when {
                    epcTagURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile(EPC_TAG_URI_PATTERN)
                        val matcher = pattern.matcher(epcTagURI)
                        when {
                            matcher.matches() -> {
                                tagSize = SGTINTagSizeEnum
                                    .findByValue(matcher.group(2).orEmpty().toInt())
                                filterValue = SGTINFilterValueEnum
                                    .findByValue(matcher.group(3).orEmpty().toInt())
                                companyPrefix = matcher.group(4).orEmpty()
                                prefixLength = PrefixLengthEnum
                                    .findByCode(matcher.group(4).orEmpty().length)
                                extensionDigit = SGTINExtensionDigitEnum
                                    .findByValue(matcher.group(5).orEmpty().toInt())
                                itemReference = matcher.group(6).orEmpty()
                                serial = matcher.group(7).orEmpty()
                            }

                            else -> throw IllegalArgumentException("EPC Tag URI is invalid")
                        }
                    }

                    epcPureIdentityURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile(EPC_PURE_IDENTITY_URI_PATTERN)
                        val matcher = pattern.matcher(epcPureIdentityURI)
                        when {
                            matcher.matches() -> {
                                companyPrefix = matcher.group(2).orEmpty()
                                prefixLength = PrefixLengthEnum
                                    .findByCode(matcher.group(2).orEmpty().length)
                                extensionDigit =
                                    SGTINExtensionDigitEnum
                                        .findByValue(matcher.group(3).orEmpty().toInt())
                                itemReference = matcher.group(4).orEmpty()
                                serial = matcher.group(5).orEmpty()
                            }

                            else -> throw IllegalArgumentException("EPC Pure Identity is invalid")
                        }
                    }

                    else -> Unit
                }
            }

            else -> {
                when {
                    companyPrefix.isEmpty() -> {
                        throw IllegalArgumentException(
                            "Company Prefix is invalid. Length not " +
                                    "found in the partition table"
                        )
                    }

                    else -> {
                        prefixLength = PrefixLengthEnum.findByCode(companyPrefix.length)
                        validateExtensionDigitAndItemReference()
                        validateSerial()
                    }
                }
            }
        }
    }

    fun parse() {
        val sgtinPartitionTableList = SGTINPartitionTableList()

        try {
            when {
                rfidTag.isEmpty() -> handleParseWithoutRfidTag(sgtinPartitionTableList)
                else -> handleParseWithRfidTag(sgtinPartitionTableList)
            }

            val outputBin = getBinary()
            val outputHex = outputBin.binToHex()
            sgtin.apply {
                epcScheme = EPC_SCHEME
                applicationIdentifier = APPLICATION_IDENTIFIER
                tagSize = this@ParseSGTIN.tagSize.value.toString()
                filterValue = this@ParseSGTIN.filterValue.value.toString()
                partitionValue = this@ParseSGTIN.tableItem.partitionValue.toString()
                prefixLength = this@ParseSGTIN.prefixLength.value.toString()
                companyPrefix = this@ParseSGTIN.companyPrefix
                itemReference = this@ParseSGTIN.itemReference
                extensionDigit = this@ParseSGTIN.extensionDigit.value.toString()
                serial = this@ParseSGTIN.serial
                checkDigit = getCheckDigit().toString()
                epcPureIdentityURI = String.format(
                    EPC_PURE_IDENTITY_URI_FORMAT,
                    this@ParseSGTIN.companyPrefix,
                    this@ParseSGTIN.extensionDigit.value,
                    this@ParseSGTIN.itemReference,
                    this@ParseSGTIN.serial
                )
                epcTagURI = String.format(
                    EPC_TAG_URI_FORMAT,
                    this@ParseSGTIN.tagSize.value,
                    this@ParseSGTIN.filterValue.value,
                    this@ParseSGTIN.companyPrefix,
                    this@ParseSGTIN.extensionDigit.value,
                    this@ParseSGTIN.itemReference,
                    this@ParseSGTIN.serial
                )
                epcRawURI = String.format(
                    EPC_RAW_URI_FORMAT,
                    this@ParseSGTIN.tagSize.value + this@ParseSGTIN.remainder,
                    outputHex
                )
                binary = outputBin
                rfidTag = outputHex
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw IllegalArgumentException("SGTIN is invalid")
        }
    }

    fun getBinary(): String {
        remainder = (ceil(tagSize.value / 16.0) * 16).toInt() - tagSize.value
        return try {
            StringBuilder().apply {
                append(tagSize.getHeader().decToBin(8))
                append((filterValue.value ?: 0).decToBin(3))
                append(tableItem.partitionValue.decToBin(3))
                append(companyPrefix.toInt().decToBin(tableItem.m))
                append("${extensionDigit.value}$itemReference".toInt().decToBin(tableItem.n))
                when (tagSize) {
                    SGTINTagSizeEnum.BITS_198 ->
                        append(serial.stringToBin(7).fill(tagSize.getSerialBitCount() + remainder))

                    SGTINTagSizeEnum.BITS_96 ->
                        append(serial.decToBin(tagSize.getSerialBitCount() + remainder))

                    else -> Unit
                }
            }.toString()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            EMPTY_STRING
        }
    }

    fun getCheckDigit(): Int {
        val value = buildString {
            append(extensionDigit.value)
            append(companyPrefix)
            append(itemReference)
        }

        val sumOdd = value.filterIndexed { index, _ -> index % 2 == 0 }
            .sumOf { it.digitToInt() }

        val sumEven = value.filterIndexed { index, _ -> index % 2 != 0 }
            .sumOf { it.digitToInt() }

        return (10 - ((3 * sumOdd + sumEven) % 10)) % 10
    }

    fun validateSerial() {
        when (val tagSizeEnum = SGTINTagSizeEnum.findByValue(tagSize.value)) {
            SGTINTagSizeEnum.BITS_198 -> when {
                serial.length > tagSizeEnum.getSerialMaxLength() -> {
                    throw IllegalArgumentException(
                        "Serial value is out of range. " +
                                "Should be up to 20 alphanumeric characters"
                    )
                }
            }

            SGTINTagSizeEnum.BITS_96 -> {
                when {
                    serial.toLong() > (tagSizeEnum.getSerialMaxValue() ?: 0L) -> {
                        throw IllegalArgumentException(
                            "Serial value is out of range. " +
                                    "Should be less than or equal 274,877,906,943"
                        )
                    }

                    serial.startsWith("0") -> {
                        throw IllegalArgumentException("Serial with leading zeros is not allowed")
                    }
                }
            }

            else -> Unit
        }
    }

    fun validateExtensionDigitAndItemReference() {
        val value = StringBuilder().append(extensionDigit.value).append(itemReference).toString()
        when {
            value.length != tableItem.digits -> {
                throw IllegalArgumentException(
                    "Concatenation between Extension Digit " +
                            "\"${extensionDigit.value}\" and Item Reference \"$itemReference\" has " +
                            "$value.length length and should have ${tableItem.digits} length"
                )
            }
        }
    }

    fun getSGTIN(): SGTIN = sgtin
}