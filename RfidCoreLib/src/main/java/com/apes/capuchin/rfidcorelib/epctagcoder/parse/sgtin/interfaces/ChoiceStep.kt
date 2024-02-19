package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

interface ChoiceStep {
    fun withCompanyPrefix(companyPrefix: String?): ExtensionDigitStep?
    fun withRFIDTag(rfidTag: String?): BuildStep?
    fun withEPCTagURI(epcTagURI: String?): BuildStep?
    fun withEPCPureIdentityURI(epcPureIdentityURI: String?): TagSizeStep?
}