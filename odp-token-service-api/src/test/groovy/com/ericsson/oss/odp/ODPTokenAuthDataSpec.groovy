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

package com.ericsson.oss.odp

import spock.lang.Specification
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenAuthData
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams

class ODPTokenAuthDataSpec extends Specification {

    def ODPTOKEN = "gp1KltivWJTCCvHmedQkHaZsKuw.*AAJTSQACMDEAAlNLABxMWVhkc2IweXkwY3BZaUZHUVVZSzlhaUVMQzQ9AAR0eXBlAANDVFMAAlMxAAA.*"

    def "ODPTokenAuthData object encapsulates authentication data including tokenTypes info"() {
        given: "A tokenname, tokendata and tokenTypes"
        def inputTokenName = "odptoken-secret-20240501123030-1"
        def inputTokenData = new HashMap<String, String>()
        inputTokenData.put("sso", ODPTOKEN)
        def inputTokenTypes = [ODPTokenGenerateParams.TokenType.SSO]

        when: "ODPTokenAuthData is created"
        ODPTokenAuthData authData = new ODPTokenAuthData(inputTokenName, inputTokenData, inputTokenTypes)

        then: "ODPTokenAuthData encapsulates authentication data"
        with(authData) {
            tokenName == inputTokenName
            tokenData.get("sso") == inputTokenData.get("sso")
            tokenTypes.contains(ODPTokenGenerateParams.TokenType.SSO)
        }
    }
}