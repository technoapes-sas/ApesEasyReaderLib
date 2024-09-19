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
import com.apes.capuchin.rfidcorelib.utils.EMPTY_STRING
import java.util.regex.Pattern
import kotlin.math.ceil

class ParseGRAI(steps: StepsGRAI) {

    private var prefixLength: PrefixLengthEnum? = null
    private var remainder: Int? = null

    private lateinit var tableItem: TableItem

    var grai: GRAI
    var companyPrefix: String
    var tagSize: GRAITagSizeEnum
    var filterValue: GRAIFilterValueEnum
    var assetType: String
    var serial: String
    var rfidTag: String
    var epcTagURI: String
    var epcPureIdentityURI: String

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

    fun handleParseWithRfidTag(partitionTableList: GRAIPartitionTableList) {

        val inputBin = rfidTag.hexToBin()
        val headerBin = inputBin.substring(0, 8)
        val filterBin = inputBin.substring(8, 11)
        val partitionBin = inputBin.substring(11, 14)

        tagSize = GRAITagSizeEnum.findByValue(GRAIHeaderEnum.findByValue(headerBin).getTagSize())
        require(tagSize != GRAITagSizeEnum.BITS_96) { "Tag size is invalid" }

        tableItem = partitionTableList.getPartitionByValue(partitionBin.toInt(2))

        val filterDec = filterBin.toInt(2)

        val companyPrefixBin = inputBin.substring(14, 14 + tableItem.m)
        require(companyPrefixBin.length == tableItem.m) { "Company Prefix is invalid" }

        val assetTypeBin = inputBin.substring(14 + tableItem.m, 14 + tableItem.m + tableItem.n)
        require(assetTypeBin.length == tableItem.n) { "Item Reference is invalid" }

        val serialBin = inputBin.substring(14 + tableItem.m + tableItem.n)
        require(serialBin.length == tagSize.getSerialBitCount()) { "Serial is invalid" }

        val companyPrefixDec = companyPrefixBin.binToDec()
        val assetTypeDec = assetTypeBin.binToDec().strZero(tableItem.digits)

        serial = serialBin.binToDec()
        assetType = assetTypeDec.substring(1)

        companyPrefix = companyPrefixDec.strZero(tableItem.l)
        filterValue = GRAIFilterValueEnum.findByValue(filterDec)
        prefixLength = PrefixLengthEnum.findByCode(tableItem.l)
    }

    fun handleParseWithoutRfidTag(partitionTableList: GRAIPartitionTableList) {
        when {
            companyPrefix.isEmpty() -> {
                when {
                    epcTagURI.isNotEmpty() -> {
                        val pattern =
                            Pattern.compile("(urn:epc:tag:grai-)(96):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(\\w+)")
                        val matcher = pattern.matcher(epcTagURI)
                        when {
                            matcher.matches() -> {
                                tagSize = GRAITagSizeEnum.findByValue(matcher.group(2)?.toInt())
                                filterValue = GRAIFilterValueEnum.findByValue(matcher.group(3)?.toInt())
                                companyPrefix = matcher.group(4).orEmpty()
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(4)?.length)
                                assetType = matcher.group(6).orEmpty()
                                serial = matcher.group(7).orEmpty()
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
                                companyPrefix = matcher.group(2).orEmpty()
                                prefixLength = PrefixLengthEnum.findByCode(matcher.group(2)?.length)
                                assetType = matcher.group(3).orEmpty()
                                serial = matcher.group(4).orEmpty()
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
        tableItem = partitionTableList.getPartitionByL(prefixLength?.value ?: 6)
    }

    fun parse() {

        val partitionTableList = GRAIPartitionTableList(tagSize)

        try {
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
            grai.partitionValue = tableItem.partitionValue.toString()
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
                tagSize.value + (remainder ?: 0),
                outputHex
            )
            grai.binary = outputBin
            grai.rfidTag = outputHex
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw IllegalArgumentException("GRAI is invalid")
        }
    }

    fun getBinary(): String {
        remainder = (ceil(tagSize.value / 16.0) * 16).toInt() - tagSize.value
        return try {
            StringBuilder().apply {
                append(tagSize.getHeader().decToBin(8))
                append(filterValue.value.decToBin(3))
                append(tableItem.partitionValue.decToBin(3))
                append(companyPrefix.toInt().decToBin(tableItem.m))
                append(assetType.toInt().decToBin(tableItem.n))
                append(serial.decToBin(tagSize.getSerialBitCount() + (remainder ?: 0)))
            }.toString()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            EMPTY_STRING
        }
    }

    fun validateCompanyPrefix() {
        prefixLength
            ?: throw IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table")
    }

    fun getGRAI() = grai

    companion object {
        fun builder(): ChoiceStep = StepsGRAI()
    }
}