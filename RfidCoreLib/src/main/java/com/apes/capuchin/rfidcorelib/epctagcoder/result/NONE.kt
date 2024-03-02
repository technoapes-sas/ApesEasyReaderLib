package com.apes.capuchin.rfidcorelib.epctagcoder.result

class NONE(tag: String) : BaseReading() {
    override var epcScheme: String? = null
    override var applicationIdentifier: String? = null
    override var tagSize: String? = null
    override var filterValue: String? = null
    override var partitionValue: String? = null
    override var prefixLength: String? = null
    override var companyPrefix: String? = null
    override var epcPureIdentityURI: String? = null
    override var epcTagURI: String? = null
    override var epcRawURI: String? = null
    override var binary: String? = null
    override var rfidTag: String? = null
    override var rssi: Int? = null

    init {
        rfidTag = tag
    }
}