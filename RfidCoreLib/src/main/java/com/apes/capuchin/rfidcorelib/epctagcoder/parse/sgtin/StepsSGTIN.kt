package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin

import com.apes.capuchin.rfidcorelib.epctagcoder.option.PrefixLengthEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.BuildStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ChoiceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ExtensionDigitStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.FilterValueStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ItemReferenceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.SerialStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.TagSizeStep

class StepsSGTIN : ChoiceStep, ExtensionDigitStep, ItemReferenceStep, SerialStep, TagSizeStep,
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
        when(filterValue) {
            is SGTINFilterValueEnum -> this.filterValue = filterValue
        }
        return this
    }

    override fun withTagSize(tagSize: Any?): FilterValueStep {
        when(tagSize) {
            is SGTINTagSizeEnum -> this.tagSize = tagSize
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
        when(extensionDigit) {
            is SGTINExtensionDigitEnum -> this.extensionDigit = extensionDigit
        }
        return this
    }

    override fun withCompanyPrefix(companyPrefix: String?): ExtensionDigitStep {
        this.companyPrefix = companyPrefix
        return this
    }

    override fun withEPCTag(rfidTag: String?): BuildStep {
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