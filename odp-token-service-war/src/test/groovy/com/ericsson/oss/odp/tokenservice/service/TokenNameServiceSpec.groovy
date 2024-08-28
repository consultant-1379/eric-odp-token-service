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

package com.ericsson.oss.odp.tokenservice.service

import spock.lang.Specification

class TokenNameServiceSpec extends Specification {

    def "Token names should start with 'odptoken-secret' prefix"() {
        given: "A TokenName service instance"
        def tokenNameService = new TokenNameService()
        tokenNameService.tokenServiceSetup = Mock(TokenServiceSetup)

        expect: "The generated token name starts with 'odptoken-secret' prefix"
        tokenNameService.getNewTokenName().startsWith("odptoken-secret");
    }

    def "Every new token name should contain the same timestamp value"() {
        given: "A TokenName service instance"
        def tokenNameService = new TokenNameService()
        tokenNameService.tokenServiceSetup = Mock(TokenServiceSetup) {
            getTokenServiceTimestamp() >> "20240510111812"
        }

        when: "Multiple token names are generated and the timestamp part is extracted"
        def tokenName1Timestamp = tokenNameService.getNewTokenName().find(/\d{14}/)
        def tokenName2Timestamp = tokenNameService.getNewTokenName().find(/\d{14}/)

        then: "The token names timestamp match"
        tokenName1Timestamp == tokenName2Timestamp
    }

    def "Every new token name should be unique by getting the suffix index incremented"() {
        given: "A TokenName service instance"
        def tokenNameService = new TokenNameService()
        tokenNameService.tokenServiceSetup = Mock(TokenServiceSetup) {
            getTokenServiceTimestamp() >> "20240510111812"
        }

        when: "Multiple token names are generated"
        def tokenName1Index = tokenNameService.getNewTokenName()
        def tokenName2Index = tokenNameService.getNewTokenName()

        then: "The token names should be unique and their suffix index incremented"
        tokenName1Index != tokenName2Index
        tokenName1Index.find(/(\d{1})$/) == "1"
        tokenName2Index.find(/(\d{1})$/) == "2"
    }
}