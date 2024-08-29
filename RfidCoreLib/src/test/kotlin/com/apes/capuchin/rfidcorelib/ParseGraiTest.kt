package com.apes.capuchin.rfidcorelib

import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAIFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAITagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.partitiontablelist.GRAIPartitionTableList
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.grai.ParseGRAI
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.grai.StepsGRAI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

class ParseGraiTest {

    @Test
    fun testInit() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        assertEquals("170034", parseGRAI.companyPrefix)
        assertEquals(GRAITagSizeEnum.BITS_96, parseGRAI.tagSize)
        assertEquals(GRAIFilterValueEnum.ALL_OTHERS_0, parseGRAI.filterValue)
        assertEquals("00004", parseGRAI.assetType)
        assertEquals("70", parseGRAI.serial)
    }

    @Test
    fun testHandleParseWithRfidTag() {
        val steps = StepsGRAI().apply {
            companyPrefix = ""
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = ""
            serial = ""
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        parseGRAI.handleParseWithRfidTag(GRAIPartitionTableList(GRAITagSizeEnum.BITS_96))
        assertEquals("170034", parseGRAI.companyPrefix)
        assertEquals("00004", parseGRAI.assetType)
        assertEquals("70", parseGRAI.serial)
    }

    @Test
    fun testHandleParseWithoutRfidTag() {
        val steps = StepsGRAI().apply {
            companyPrefix = ""
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = ""
            serial = ""
            rfidTag = ""
            epcTagURI = "urn:epc:tag:grai-96:0.170034.000004.70"
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        parseGRAI.handleParseWithoutRfidTag(GRAIPartitionTableList(GRAITagSizeEnum.BITS_96))
        assertEquals("170034", parseGRAI.companyPrefix)
        assertEquals("00004", parseGRAI.assetType)
        assertEquals("70", parseGRAI.serial)
    }

    @Test
    fun testParse() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        parseGRAI.parse()
        assertEquals("170034", parseGRAI.companyPrefix)
        assertEquals("00004", parseGRAI.assetType)
        assertEquals("70", parseGRAI.serial)
    }

    @Test
    fun testGetBinary() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        val binary = parseGRAI.getBinary()
        assertNotNull(binary)
    }

    @Test
    fun testValidateCompanyPrefix() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        assertDoesNotThrow { parseGRAI.validateCompanyPrefix() }
    }

    @Test
    fun testGetGRAI() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        val grai = parseGRAI.getGRAI()
        assertNotNull(grai)
    }

    @Test
    fun testGetRfidTag() {
        val steps = StepsGRAI().apply {
            companyPrefix = "170034"
            tagSize = GRAITagSizeEnum.BITS_96
            filterValue = GRAIFilterValueEnum.ALL_OTHERS_0
            itemReference = "00004"
            serial = "70"
            rfidTag = "3318A60C8000010000000046"
            epcTagURI = ""
            epcPureIdentityURI = ""
        }
        val parseGRAI = ParseGRAI(steps)
        val rfidTag = parseGRAI.getRfidHexTag()
        assertNotNull(rfidTag)
    }
}