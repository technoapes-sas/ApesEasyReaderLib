package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface ChoiceStep {
    fun withCompanyPrefix(companyPrefix: String?): Any
    fun withRFIDTag(rfidTag: String?): BuildStep
    fun withEPCTagURI(epcTagURI: String?): BuildStep
    fun withEPCPureIdentityURI(epcPureIdentityURI: String?): TagSizeStep
}