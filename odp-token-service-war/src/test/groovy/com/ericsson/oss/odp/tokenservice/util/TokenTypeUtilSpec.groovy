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

import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams
import spock.lang.Specification

class TokenTypeUtilSpec extends Specification {

    def "Convert TokenType enums list into a comma separated string list"() {
        given: "A list of token type enums"
        def inputTokenTypes = [ODPTokenGenerateParams.TokenType.SSO, ODPTokenGenerateParams.TokenType.SSH_KEY_PAIR]
        def expectedStringList = "sso,sshkeypair"

        when: "TokenType enums are converted into a comma separated string list"
        def actualStringList = TokenTypeUtil.tokenTypesToStringList(inputTokenTypes);

        then: "The token types are represented as a comma separated string list"
        actualStringList == expectedStringList
    }

    def "Convert comma separated token types string list into a TokenType enums list"() {
        given: "A comma separated token types string list"
        def inputStringList = "sso,sshkeypair"
        def expectedTokenTypes = [ODPTokenGenerateParams.TokenType.SSO, ODPTokenGenerateParams.TokenType.SSH_KEY_PAIR]

        when: "Comma separated token types string list is converted into a TokenType enums list"
        def actualTokenTypes = TokenTypeUtil.stringListToTokenTypes(inputStringList);

        then: "The token types are represented as a TokenType enums list"
        actualTokenTypes == expectedTokenTypes
    }

    def "A null string value results into an empty token type list"() {
        expect: "The token types list is empty"
        TokenTypeUtil.stringListToTokenTypes(null).isEmpty()
    }
}
