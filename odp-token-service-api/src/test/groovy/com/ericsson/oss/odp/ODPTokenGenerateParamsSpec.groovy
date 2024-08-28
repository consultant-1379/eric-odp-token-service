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
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams

class ODPTokenGenerateParamsSpec extends Specification {

    def "Get TokenType constant from valid string value"() {
        given: "A valid string token type value"
        def tokenTypeString = "sso"

        expect: "TokenType constant matching the given tokentype string"
        ODPTokenGenerateParams.TokenType.fromValue(tokenTypeString) == ODPTokenGenerateParams.TokenType.SSO
    }

    def "Get exception when the string value is not matching any TokenType constants"() {
        given: "An invalid string token type value"
        def tokenTypeString = "odptoken"

        when: "TokenType constant match with the string value is done"
        ODPTokenGenerateParams.TokenType.fromValue(tokenTypeString)

        then: "Unexpected token name"
        thrown(IllegalArgumentException)
    }

    def "TokenType string representation matches the tokenName string value"() {
        given: "A token type constant"
        def ssoTokenType = ODPTokenGenerateParams.TokenType.SSO

        expect: "TokenType constant string representation matches the given string value"
        ssoTokenType.toString() == "sso"
    }

    def "ODPTokenGenerateParams object encapsulates token generation parameters"() {
        given: "A username and tokenTypes"
        def username = "odpuser"
        def tokenTypes = [ODPTokenGenerateParams.TokenType.SSO]

        when: "ODPTokenGenerateParams is created"
        ODPTokenGenerateParams tokenGenerateParams = new ODPTokenGenerateParams()
        tokenGenerateParams.setUsername(username)
        tokenGenerateParams.setTokenTypes(tokenTypes)

        then: "ODPTokenGenerateParams encapsulates token generation parameters"
        tokenGenerateParams.getUsername() == username
        tokenGenerateParams.getTokenTypes() == tokenTypes
    }

    def "ODPTokenGenerateParams instances are equal when encapsulating same data"() {
        given: "Two ODPTokenGenerateParams instances encapsulating same data"
        def username = "odpuser"
        def tokenTypes = [ODPTokenGenerateParams.TokenType.SSO]
        ODPTokenGenerateParams tokenGenerateParams1 = new ODPTokenGenerateParams()
        tokenGenerateParams1.setUsername(username)
        tokenGenerateParams1.setTokenTypes(tokenTypes)
        ODPTokenGenerateParams tokenGenerateParams2 = new ODPTokenGenerateParams()
        tokenGenerateParams2.setUsername(username)
        tokenGenerateParams2.setTokenTypes(tokenTypes)

        expect: "Equality is satisfied"
        tokenGenerateParams1.equals(tokenGenerateParams2)
        tokenGenerateParams1.hashCode() == tokenGenerateParams2.hashCode()
    }
}