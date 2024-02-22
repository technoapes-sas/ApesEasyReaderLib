package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINHeaderEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.partitiontable.SGTINPartitionTableList
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.BuildStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ChoiceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ExtensionDigitStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.FilterValueStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ItemReferenceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.SerialStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.TagSizeStep
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
import java.util.regex.Pattern
import kotlin.math.ceil

class ParseSGTIN private constructor(steps: Steps) {

    private var sgtin: SGTIN
    private var extensionDigit: SGTINExtensionDigitEnum
    private var companyPrefix: String
    private var prefixLength: PrefixLengthEnum
    private var tagSize: SGTINTagSizeEnum
    private var filterValue: SGTINFilterValueEnum
    private var itemReference: String
    private var serial: String
    private var rfidTag: String
    private var epcTagURI: String
    private var epcPureIdentityURI: String
    private var tableItem: TableItem
    private var remainder: Int

    init {
        extensionDigit = steps.extensionDigit ?: SGTINExtensionDigitEnum.EXTENSION_0
        companyPrefix = steps.companyPrefix.orEmpty()
        prefixLength = steps.prefixLength ?: PrefixLengthEnum.DIGIT_6
        tagSize = steps.tagSize ?: SGTINTagSizeEnum.BITS_96
        filterValue = steps.filterValue ?: SGTINFilterValueEnum.ALL_OTHERS_0
        itemReference = steps.itemReference.orEmpty()
        serial = steps.serial.orEmpty()
        rfidTag = steps.rfidTag.orEmpty()
        epcTagURI = steps.epcTagURI.orEmpty()
        epcPureIdentityURI = steps.epcPureIdentityURI.orEmpty()
        tableItem = steps.tableItem ?: TableItem()
        remainder = steps.remainder ?: 0
        sgtin = SGTIN()
        parse()
    }

    private fun handleParseWithRfidTag(sgtinPartitionTableList: SGTINPartitionTableList) {
        val inputBin = rfidTag.hexToBin()
        val headerBin = inputBin.substring(0, 8)
        val filterBin = inputBin.substring(8, 11)
        val partitionBin = inputBin.substring(11, 14)

        tagSize = SGTINTagSizeEnum.findByValue(
            SGTINHeaderEnum.findByValue(headerBin).getTagSize()
        )
        tableItem = sgtinPartitionTableList.getPartitionByValue(
            partitionBin.binToDec().toInt()
        ) ?: TableItem()

        val filterDec = filterBin.toLong(2).toString()
        val companyPrefixBin = inputBin.substring(14, 14 + (tableItem.m ?: 0))
        val itemReferenceWithExtensionBin = inputBin.substring(
            14 + (tableItem.m ?: 0),
            14 + (tableItem.m ?: 0) + (tableItem.n ?: 0)
        )
        var serialBin = inputBin
            .substring(14 + (tableItem.m ?: 0) + (tableItem.n ?: 0))
        val companyPrefixDec = companyPrefixBin.binToDec()
        val itemReferenceWithExtensionDec = itemReferenceWithExtensionBin
            .binToDec()
            .strZero(tableItem.digits ?: 0)
        val extensionDec = itemReferenceWithExtensionDec.substring(0, 1)
        itemReference = itemReferenceWithExtensionDec.substring(1)

        serial = when (tagSize.getSerialBitCount()) {
            140 -> {
                serialBin = serialBin.convertBinToBit(7, 8)
                serialBin.binToString()
            }

            else -> serialBin.binToDec()
        }

        companyPrefix = companyPrefixDec.strZero(tableItem.l ?: 0)
        extensionDigit = SGTINExtensionDigitEnum.findByValue(extensionDec.toInt())
        filterValue = SGTINFilterValueEnum.findByValue(filterDec.toInt())
        prefixLength = PrefixLengthEnum.findByCode(tableItem.l ?: 0)
    }

    private fun handleParseWithoutRfidTag(sgtinPartitionTableList: SGTINPartitionTableList) {
        when {
            companyPrefix.isEmpty() -> {
                when {
                    epcTagURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile("(urn:epc:tag:sgtin-)(96|198):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(\\w+)")
                        val matcher = pattern.matcher(epcTagURI)
                        when {
                            matcher.matches() -> {
                                tagSize = SGTINTagSizeEnum.findByValue(matcher.group(2).toInt())
                                filterValue = SGTINFilterValueEnum
                                    .findByValue(matcher.group(3).toInt())
                                companyPrefix = matcher.group(4)
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(4).length)
                                extensionDigit = SGTINExtensionDigitEnum
                                    .findByValue(matcher.group(5).toInt())
                                itemReference = matcher.group(6)
                                serial = matcher.group(7)
                            }

                            else -> throw IllegalArgumentException("EPC Tag URI is invalid")
                        }
                    }

                    epcPureIdentityURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile("(urn:epc:id:sgtin):(\\d+)\\.([0-8])(\\d+)\\.(\\w+)")
                        val matcher = pattern.matcher(epcPureIdentityURI)
                        when {
                            matcher.matches() -> {
                                companyPrefix = matcher.group(2)
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(2).length)
                                extensionDigit =
                                    SGTINExtensionDigitEnum.findByValue(matcher.group(3).toInt())
                                itemReference = matcher.group(4)
                                serial = matcher.group(5)
                            }

                            else -> throw IllegalArgumentException("EPC Pure Identity is invalid")
                        }
                    }

                    else -> Unit
                }
            }

            else -> {
                if (companyPrefix.isEmpty()) {
                    throw IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table")
                }
                prefixLength = PrefixLengthEnum.findByCode(companyPrefix.length)
                validateExtensionDigitAndItemReference()
                validateSerial()
            }
        }
        tableItem = sgtinPartitionTableList.getPartitionByL(prefixLength.value) ?: TableItem()
    }

    private fun parse() {

        val sgtinPartitionTableList = SGTINPartitionTableList()

        when {
            rfidTag.isEmpty() -> handleParseWithoutRfidTag(sgtinPartitionTableList)
            else -> handleParseWithRfidTag(sgtinPartitionTableList)
        }

        val outputBin = getBinary()
        val outputHex = outputBin.binToHex()

        sgtin.epcScheme = "sgtin"
        sgtin.applicationIdentifier = "AI 414 + AI 254"
        sgtin.tagSize = tagSize.value.toString()
        sgtin.filterValue = filterValue.value.toString()
        sgtin.partitionValue = tableItem.partitionValue?.toString().orEmpty()
        sgtin.prefixLength = prefixLength.value.toString()
        sgtin.companyPrefix = companyPrefix
        sgtin.itemReference = itemReference
        sgtin.extensionDigit = extensionDigit.value.toString()
        sgtin.serial = serial
        sgtin.checkDigit = getCheckDigit().toString()
        sgtin.epcPureIdentityURI = String.format(
            "urn:epc:id:sgtin:%s.%s%s.%s",
            companyPrefix,
            extensionDigit.value,
            itemReference,
            serial
        )
        sgtin.epcTagURI = String.format(
            "urn:epc:tag:sgtin-%s:%s.%s.%s%s.%s",
            tagSize.value,
            filterValue.value,
            companyPrefix,
            extensionDigit.value,
            itemReference,
            serial
        )
        sgtin.epcRawURI = String.format("urn:epc:raw:%s.x%s", tagSize.value + remainder, outputHex)
        sgtin.binary = outputBin
        sgtin.rfidTag = outputHex
    }

    private fun getBinary(): String {
        remainder = (ceil(tagSize.value / 16.0) * 16).toInt() - tagSize.value
        return StringBuilder().apply {
            append(tagSize.getHeader().decToBin(8))
            append(filterValue.value.decToBin(3))
            append(tableItem.partitionValue?.decToBin(3))
            append(companyPrefix.toInt().decToBin(tableItem.m ?: 0))
            append("${extensionDigit.value}$itemReference".toInt().decToBin(tableItem.n ?: 0))
            when (tagSize) {
                SGTINTagSizeEnum.BITS_198 ->
                    append(serial.stringToBin(7).fill(tagSize.getSerialBitCount() + remainder))

                SGTINTagSizeEnum.BITS_96 ->
                    append(serial.decToBin(tagSize.getSerialBitCount() + remainder))
            }
        }.toString()
    }

    private fun getCheckDigit(): Int {
        val value = StringBuilder()
            .append(extensionDigit.value)
            .append(companyPrefix)
            .append(itemReference)
            .toString()

        return (10 - ((3 * (value.indices.step(2)
            .sumOf { value[it].digitToInt() }) + (value.indices.step(2).drop(1)
            .sumOf { value[it].digitToInt() })) % 10)) % 10
    }

    private fun validateSerial() {
        when (val tagSizeEnum = SGTINTagSizeEnum.findByValue(tagSize.value)) {
            SGTINTagSizeEnum.BITS_198 -> {
                if (serial.length > tagSizeEnum.getSerialMaxLength()) {
                    throw IllegalArgumentException(
                        "Serial value is out of range. " +
                                "Should be up to 20 alphanumeric characters"
                    )
                }
            }

            SGTINTagSizeEnum.BITS_96 -> {
                if (serial.toLong() > (tagSizeEnum.getSerialMaxValue() ?: 0L)) {
                    throw IllegalArgumentException(
                        "Serial value is out of range. " +
                                "Should be less than or equal 274,877,906,943"
                    )
                }
                if (serial.startsWith("0")) {
                    throw IllegalArgumentException("Serial with leading zeros is not allowed")
                }
            }
        }
    }

    private fun validateExtensionDigitAndItemReference() {
        val value = StringBuilder().append(extensionDigit.value).append(itemReference).toString()
        if (value.length != tableItem.digits) {
            throw IllegalArgumentException("Concatenation between Extension Digit \"${extensionDigit.value}\" and Item Reference \"$itemReference\" has $value.length length and should have ${tableItem.digits} length")
        }
    }

    fun getSGTIN() = sgtin

    fun getRfidTag() = getBinary().binToHex()

    private class Steps : ChoiceStep, ExtensionDigitStep, ItemReferenceStep, SerialStep, TagSizeStep,
        FilterValueStep, BuildStep {

        var extensionDigit: SGTINExtensionDigitEnum? = null
        var companyPrefix: String? = null
        var prefixLength: PrefixLengthEnum? = null
        var tagSize: SGTINTagSizeEnum? = null
        var filterValue: SGTINFilterValueEnum? = null
        var itemReference: String? = null
        var serial: String? = null
        var rfidTag: String? = null
        var epcTagURI: String? = null
        var epcPureIdentityURI: String? = null
        var tableItem: TableItem? = null
        var remainder: Int? = null

        override fun build(): ParseSGTIN = ParseSGTIN(this)

        override fun withFilterValue(filterValue: Any?): BuildStep {
            if (filterValue is SGTINFilterValueEnum) {
                this.filterValue = filterValue
            }
            return this
        }

        override fun withTagSize(tagSize: Any?): FilterValueStep {
            if (tagSize is SGTINTagSizeEnum) {
                this.tagSize = tagSize
            }
            return this
        }

        override fun withSerial(serial: String?): TagSizeStep {
            this.serial = serial
            return this
        }

        override fun withItemReference(itemReference: String?): SerialStep {
            this.itemReference = itemReference
            return this
        }

        override fun withExtensionDigit(extensionDigit: Any?): ItemReferenceStep {
            if (extensionDigit is SGTINExtensionDigitEnum) {
                this.extensionDigit = extensionDigit
            }
            return this
        }

        override fun withCompanyPrefix(companyPrefix: String?): ExtensionDigitStep {
            this.companyPrefix = companyPrefix
            return this
        }

        override fun withRFIDTag(rfidTag: String?): BuildStep {
            this.rfidTag = rfidTag
            return this
        }

        override fun withEPCTagURI(epcTagURI: String?): BuildStep {
            this.epcTagURI = epcTagURI
            return this
        }

        override fun withEPCPureIdentityURI(epcPureIdentityURI: String?): TagSizeStep {
            this.epcPureIdentityURI = epcPureIdentityURI
            return this
        }
    }
}