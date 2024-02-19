package com.apes.capuchin.rfidcorelib.epctagcoder.result

abstract class BaseReading : Comparable<BaseReading> {

    abstract var epcScheme: String
    abstract var applicationIdentifier: String
    abstract var tagSize: String
    abstract var filterValue: String
    abstract var partitionValue: String
    abstract var prefixLength: String
    abstract var companyPrefix: String
    abstract var epcPureIdentityURI: String
    abstract var epcTagURI: String
    abstract var epcRawURI: String
    abstract var binary: String
    abstract var rfidTag: String
    abstract var rssi: Int

    override fun hashCode(): Int = rfidTag.hashCode()

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BaseReading -> {
                val isSameObj = rfidTag.equals(other.rfidTag, ignoreCase = true)
                when {
                    this === other || isSameObj -> {
                        this.rssi = other.rssi
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    override fun compareTo(other: BaseReading): Int {
        return other.rssi.compareTo(this.rssi)
    }
}