package com.apes.capuchin.rfidcorelib.epctagcoder.parse.grai

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAIFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAIHeaderEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAITagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.partitiontablelist.GRAIPartitionTableList
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ChoiceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.result.GRAI
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToDec
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToHex
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.decToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.hexToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.strZero
import java.util.regex.Pattern
import kotlin.math.ceil

class ParseGRAI(steps: StepsGRAI) {

    private var grai: GRAI
    private var companyPrefix: String
    private var tagSize: GRAITagSizeEnum
    private var filterValue: GRAIFilterValueEnum
    private var assetType: String
    private var serial: String
    private var rfidTag: String
    private var epcTagURI: String
    private var epcPureIdentityURI: String
    private var prefixLength: PrefixLengthEnum? = null
    private var tableItem: TableItem? = null
    private var remainder: Int? = null

    init {
        companyPrefix = steps.companyPrefix.orEmpty()
        tagSize = steps.tagSize ?: GRAITagSizeEnum.BITS_96
        filterValue = steps.filterValue ?: GRAIFilterValueEnum.ALL_OTHERS_0
        assetType = steps.itemReference.orEmpty()
        serial = steps.serial.orEmpty()
        rfidTag = steps.rfidTag.orEmpty()
        epcTagURI = steps.epcTagURI.orEmpty()
        epcPureIdentityURI = steps.epcPureIdentityURI.orEmpty()
        grai = GRAI()
        parse()
    }

    private fun handleParseWithRfidTag(partitionTableList: GRAIPartitionTableList) {

        val inputBin = rfidTag.hexToBin()
        val headerBin = inputBin.substring(0, 8)
        val filterBin = inputBin.substring(8, 11)
        val partitionBin = inputBin.substring(11, 14)

        tagSize = GRAITagSizeEnum.findByValue(GRAIHeaderEnum.findByValue(headerBin).getTagSize())
        tableItem = partitionTableList.getPartitionByValue(partitionBin.toInt(2))

        val filterDec = filterBin.toLong(2).toString()
        val companyPrefixBin = inputBin.substring(14, 14 + (tableItem?.m ?: 0))
        val assetTypeBin = inputBin.substring(
            14 + (tableItem?.m ?: 0),
            14 + (tableItem?.m ?: 0) + (tableItem?.n ?: 0)
        )
        val serialBin = inputBin
            .substring(14 + (tableItem?.m ?: 0) + (tableItem?.n ?: 0))
        val companyPrefixDec = companyPrefixBin.binToDec()
        val assetTypeDec = assetTypeBin
            .binToDec()
            .strZero(tableItem?.digits ?: 0)

        serial = serialBin.binToDec()
        assetType = assetTypeDec.substring(1)

        companyPrefix = companyPrefixDec.strZero(tableItem?.l ?: 0)
        filterValue = GRAIFilterValueEnum.findByValue(filterDec.toInt())
        prefixLength = PrefixLengthEnum.findByCode(tableItem?.l ?: 0)
    }

    private fun handleParseWithoutRfidTag(partitionTableList: GRAIPartitionTableList) {
        when {
            companyPrefix.isEmpty() -> {
                when {
                    epcTagURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile("(urn:epc:tag:grai-)(96):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(\\w+)")
                        val matcher = pattern.matcher(epcTagURI)
                        when {
                            matcher.matches() -> {
                                tagSize = GRAITagSizeEnum.findByValue(matcher.group(2).toInt())
                                filterValue = GRAIFilterValueEnum
                                    .findByValue(matcher.group(3).toInt())
                                companyPrefix = matcher.group(4)
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(4).length)
                                assetType = matcher.group(5)
                                serial = matcher.group(6)
                            }

                            else -> throw IllegalArgumentException("EPC Tag URI is invalid")
                        }
                    }

                    epcPureIdentityURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile("(urn:epc:id:grai):(\\d+)\\.([0-8])(\\d+)\\.(\\w+)")
                        val matcher = pattern.matcher(epcPureIdentityURI)
                        when {
                            matcher.matches() -> {
                                companyPrefix = matcher.group(2)
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(2).length)
                                assetType = matcher.group(3)
                                serial = matcher.group(4)
                            }

                            else -> throw IllegalArgumentException("EPC Pure Identity is invalid")
                        }
                    }

                    else -> Unit
                }
            }

            else -> {
                prefixLength = PrefixLengthEnum.findByCode(companyPrefix.length)
                validateCompanyPrefix()
            }
        }
        tableItem = partitionTableList.getPartitionByL(prefixLength?.value ?: 6) ?: TableItem()
    }

    private fun parse() {

        val partitionTableList = GRAIPartitionTableList(tagSize)

        when {
            rfidTag.isEmpty() -> handleParseWithoutRfidTag(partitionTableList)
            else -> handleParseWithRfidTag(partitionTableList)
        }

        val outputBin = getBinary()
        val outputHex = outputBin.binToHex()

        grai.epcScheme = "grai"
        grai.applicationIdentifier = "8003"
        grai.tagSize = tagSize.value.toString()
        grai.filterValue = filterValue.value.toString()
        grai.partitionValue = tableItem?.partitionValue?.toString().orEmpty()
        grai.prefixLength = prefixLength?.value.toString()
        grai.companyPrefix = companyPrefix
        grai.assetType = assetType
        grai.serial = serial
        grai.epcPureIdentityURI = String.format(
            "urn:epc:id:grai:%s.%s.%s",
            companyPrefix,
            assetType,
            serial
        )
        grai.epcTagURI = String.format(
            "urn:epc:tag:grai-%s:%s.%s.%s.%s",
            tagSize.value,
            filterValue.value,
            companyPrefix,
            assetType,
            serial
        )
        grai.epcRawURI = String.format(
            "urn:epc:raw:%s.x%s",
            (tagSize.value ?: 0) + (remainder ?: 0),
            outputHex
        )
        grai.binary = outputBin
        grai.rfidTag = outputHex
    }

    private fun getBinary(): String {
        remainder = (ceil((tagSize.value ?: 0) / 16.0) * 16).toInt() - (tagSize.value ?: 0)
        return StringBuilder().apply {
            append(tagSize.getHeader().decToBin(8))
            append((filterValue.value ?: 0).decToBin(3))
            append(tableItem?.partitionValue?.decToBin(3))
            append(companyPrefix.toInt().decToBin(tableItem?.m ?: 0))
            append(assetType.toInt().decToBin(tableItem?.n ?: 0))
            append(serial.decToBin(tagSize.getSerialBitCount() + (remainder ?: 0)))
        }.toString()
    }

    private fun validateCompanyPrefix() {
        prefixLength
            ?: throw IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table")
    }

    fun getGRAI() = grai

    fun getRfidTag() = getBinary().binToHex()

    companion object {
        fun Builder(): ChoiceStep = StepsGRAI()
    }
}