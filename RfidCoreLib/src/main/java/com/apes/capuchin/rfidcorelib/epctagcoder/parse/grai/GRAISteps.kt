package com.apes.capuchin.rfidcorelib.epctagcoder.parse.grai

import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAIFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAITagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.AssetTypeStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.BuildStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.ChoiceStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.FilterValueStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.SerialStep
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces.TagSizeStep

class GRAISteps : ChoiceStep, AssetTypeStep, SerialStep, TagSizeStep,
    FilterValueStep, BuildStep {

    var companyPrefix: String? = null
    var tagSize: GRAITagSizeEnum? = null
    var filterValue: GRAIFilterValueEnum? = null
    var itemReference: String? = null
    var serial: String? = null
    var rfidTag: String? = null
    var epcTagURI: String? = null
    var epcPureIdentityURI: String? = null

    override fun build(): ParseGRAI = ParseGRAI(this)

    override fun withFilterValue(filterValue: Any?): BuildStep {
        if (filterValue is GRAIFilterValueEnum) {
            this.filterValue = filterValue
        }
        return this
    }

    override fun withTagSize(tagSize: Any?): FilterValueStep {
        if (tagSize is GRAITagSizeEnum) {
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

    override fun withCompanyPrefix(companyPrefix: String?): BuildStep {
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