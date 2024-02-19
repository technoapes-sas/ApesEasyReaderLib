package com.apes.capuchin.rfidcorelib.epctagcoder.parse

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum

class ParseSGTIN(private val steps: Steps) {

    private var extensionDigit: SGTINExtensionDigitEnum? = null
    private var companyPrefix: String? = null
    private var prefixLength: PrefixLengthEnum? = null
    private var tagSize: SGTINTagSizeEnum? = null
    private var filterValue: SGTINFilterValueEnum? = null
    private var itemReference: String? = null
    private var serial: String? = null
    private var rfidTag: String? = null
    private var epcTagURI: String? = null
    private var epcPureIdentityURI: String? = null
    private var tableItem: TableItem? = null
    private var remainder: Int? = null

    init {
        extensionDigit = steps.extensionDigit
        companyPrefix = steps.companyPrefix
        prefixLength = steps.prefixLength
        tagSize = steps.tagSize
        filterValue = steps.filterValue
        itemReference = steps.itemReference
        serial = steps.serial
        rfidTag = steps.rfidTag
        epcTagURI = steps.epcTagURI
        epcPureIdentityURI = steps.epcPureIdentityURI
        tableItem = steps.tableItem
        remainder = steps.remainder
    }

    private fun validateSerial() {
        when (val tagSizeEnum = SGTINTagSizeEnum.findByValue(tagSize?.value)) {
            SGTINTagSizeEnum.BITS_198 -> {
                if (serial.orEmpty().length > tagSizeEnum.getSerialMaxLength()) {
                    throw IllegalArgumentException("Serial value is out of range. " +
                            "Should be up to 20 alphanumeric characters")
                }
            }
            SGTINTagSizeEnum.BITS_96 -> {
                if (serial.orEmpty().toLong() > (tagSizeEnum.getSerialMaxValue() ?: 0L)) {
                    throw IllegalArgumentException("Serial value is out of range. " +
                            "Should be less than or equal 274,877,906,943")
                }
                if (serial.orEmpty().startsWith("0")) {
                    throw IllegalArgumentException("Serial with leading zeros is not allowed")
                }
            }
            else -> Unit
        }
    }

    interface BuildStep {
        fun build(): ParseSGTIN?
    }

    interface FilterValueStep {
        fun withFilterValue(filterValue: SGTINFilterValueEnum?): BuildStep?
    }

    interface TagSizeStep {
        fun withTagSize(tagSize: SGTINTagSizeEnum?): FilterValueStep?
    }

    interface SerialStep {
        fun withSerial(serial: String?): TagSizeStep?
    }

    interface ItemReferenceStep {
        fun withItemReference(itemReference: String?): SerialStep?
    }

    interface ExtensionDigitStep {
        fun withExtensionDigit(extensionDigit: SGTINExtensionDigitEnum?): ItemReferenceStep?
    }

    interface ChoiceStep {
        fun withCompanyPrefix(companyPrefix: String?): ExtensionDigitStep?
        fun withRFIDTag(rfidTag: String?): BuildStep?
        fun withEPCTagURI(epcTagURI: String?): BuildStep?
        fun withEPCPureIdentityURI(epcPureIdentityURI: String?): TagSizeStep?
    }

    inner class Steps : ChoiceStep, ExtensionDigitStep, ItemReferenceStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

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

        override fun withFilterValue(filterValue: SGTINFilterValueEnum?): BuildStep {
            this.filterValue = filterValue
            return this
        }

        override fun withTagSize(tagSize: SGTINTagSizeEnum?): FilterValueStep {
            this.tagSize = tagSize
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

        override fun withExtensionDigit(extensionDigit: SGTINExtensionDigitEnum?): ItemReferenceStep {
            this.extensionDigit = extensionDigit
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