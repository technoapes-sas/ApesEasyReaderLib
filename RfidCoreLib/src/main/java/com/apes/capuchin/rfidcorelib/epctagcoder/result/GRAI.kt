package com.apes.capuchin.rfidcorelib.epctagcoder.result

class GRAI : BaseReading() {
    
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

    var assetType: String? = null
    var serial: String? = null

    override fun toString(): String {
        val json = StringBuilder().apply {
            append(String.format("{ \"epcScheme\": \"%s\"", epcScheme))
            append(String.format(", \"applicationIdentifier\": \"%s\"", applicationIdentifier))
            append(String.format(", \"tagSize\": \"%s\"", tagSize))
            append(String.format(", \"filterValue\": \"%s\"", filterValue))
            append(String.format(", \"partitionValue\": \"%s\"", partitionValue))
            append(String.format(", \"prefixLength\": \"%s\"", prefixLength))
            append(String.format(", \"companyPrefix\": \"%s\"", companyPrefix))
            append(String.format(", \"assetType\": \"%s\"", assetType))
            append(String.format(", \"serial\": \"%s\"", serial))
            append(String.format(", \"epcPureIdentityURI\": \"%s\"", epcPureIdentityURI))
            append(String.format(", \"epcTagURI\": \"%s\"", epcTagURI))
            append(String.format(", \"epcRawURI\": \"%s\"", epcRawURI))
            append(String.format(", \"binary\": \"%s\"", binary))
            append(String.format(", \"rfidTag\": \"%s\"", rfidTag)).append(" }")
        }

        return json.toString()
    }
}