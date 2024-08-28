/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.odp.tokenservice.util

import spock.lang.Specification

class Base64UtilSpec extends Specification {

    def RAW_STRING_VALUE = "Raw string to be encoded to base64 format."
    def BASE64_ENCODED_VALUE = "UmF3IHN0cmluZyB0byBiZSBlbmNvZGVkIHRvIGJhc2U2NCBmb3JtYXQu"

    def "Encode raw string to base64 format"() {
        when: "Base64 encode is applied on the raw string"
        def base64EncodedOutput = Base64Util.encode(RAW_STRING_VALUE);

        then: "The raw string is base64 encoded"
        base64EncodedOutput == BASE64_ENCODED_VALUE
    }

    def "Decode base64 value to raw string"() {
        when: "Base64 decode is applied on an encoded value"
        def decodedOutput = Base64Util.decode(BASE64_ENCODED_VALUE);

        then: "The base64 value is decoded"
        decodedOutput == RAW_STRING_VALUE
    }
}
