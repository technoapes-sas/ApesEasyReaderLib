package com.apes.capuchin.rfidcorelib.epctagcoder.result

class SGTIN(
    override var epcScheme: String?,
    override var applicationIdentifier: String?,
    override var tagSize: String?,
    override var filterValue: String?,
    override var partitionValue: String?,
    override var prefixLength: String?,
    override var companyPrefix: String?,
    override var epcPureIdentityURI: String?,
    override var epcTagURI: String?,
    override var epcRawURI: String?,
    override var binary: String?,
    override var rfidTag: String?,
    override var rssi: Int?
) : BaseReading() {

    var extensionDigit: String? = null
    var itemReference: String? = null
    var serial: String? = null
    var checkDigit: String? = null

}