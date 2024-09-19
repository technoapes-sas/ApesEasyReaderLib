package com.apes.capuchin.rfidcorelib

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.partitiontable.SGTINPartitionTableList
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.ParseSGTIN
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.StepsSGTIN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

class ParseSgtinTest {

    @Test
    fun testInit() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        assertEquals("7705751", parseSGTIN.companyPrefix)
        assertEquals("51906", parseSGTIN.itemReference)
        assertEquals("37", parseSGTIN.serial)
    }

    @Test
    fun testHandleParseWithRfidTag() {
        val steps = StepsSGTIN().apply {
            companyPrefix = ""
            itemReference = ""
            serial = ""
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        parseSGTIN.handleParseWithRfidTag(SGTINPartitionTableList())
        assertEquals("7705751", parseSGTIN.companyPrefix)
        assertEquals("51906", parseSGTIN.itemReference)
        assertEquals("37", parseSGTIN.serial)
    }

    @Test
    fun testHandleParseWithoutRfidTag() {
        val steps = StepsSGTIN().apply {
            companyPrefix = ""
            itemReference = ""
            serial = ""
            rfidTag = ""
            epcTagURI = "urn:epc:tag:sgtin-96:1.7705751.051906.37"
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        parseSGTIN.handleParseWithoutRfidTag(SGTINPartitionTableList())
        assertEquals("0", parseSGTIN.extensionDigit.value.toString())
        assertEquals("7705751", parseSGTIN.companyPrefix)
        assertEquals("51906", parseSGTIN.itemReference)
        assertEquals("37", parseSGTIN.serial)
    }

    @Test
    fun testParse() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        parseSGTIN.parse()
        assertEquals("7705751", parseSGTIN.companyPrefix)
        assertEquals("51906", parseSGTIN.itemReference)
        assertEquals("37", parseSGTIN.serial)
    }

    @Test
    fun testGetBinary() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        val binary = parseSGTIN.getBinary()
        assertNotNull(binary)
    }

    @Test
    fun testGetCheckDigit() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        val checkDigit = parseSGTIN.getCheckDigit()
        assertEquals(3, checkDigit)
    }

    @Test
    fun testValidateSerial() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        assertDoesNotThrow { parseSGTIN.validateSerial() }
    }

    @Test
    fun testValidateExtensionDigitAndItemReference() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        assertDoesNotThrow { parseSGTIN.validateExtensionDigitAndItemReference() }
    }

    @Test
    fun testGetSGTIN() {
        val steps = StepsSGTIN().apply {
            companyPrefix = "7705751"
            itemReference = "51906"
            serial = "37"
            rfidTag = "3035D6525C32B08000000025"
            epcTagURI = ""
            epcPureIdentityURI = ""
            remainder = 0
            extensionDigit = SGTINExtensionDigitEnum.EXTENSION_0
            prefixLength = PrefixLengthEnum.DIGIT_7
            tagSize = SGTINTagSizeEnum.BITS_96
            filterValue = SGTINFilterValueEnum.POS_ITEM_1
        }
        val parseSGTIN = ParseSGTIN(steps)
        val sgtin = parseSGTIN.getSGTIN()
        assertNotNull(sgtin)
    }
}