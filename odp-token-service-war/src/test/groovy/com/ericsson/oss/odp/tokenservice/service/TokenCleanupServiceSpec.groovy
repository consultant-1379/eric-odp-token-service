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

import java.time.Instant

class TokenCleanupServiceSpec extends Specification {

    def "Token cleanup execution"() {
        given: "A Token Cleanup service instance"
        def tokenCleanupService = new TokenCleanupService()
         def odpAuthDataService = Mock(OdpAuthDataService) {
            cleanupExpiredTokens(_ as Instant) >> 1
        }
        tokenCleanupService.odpAuthDataService = odpAuthDataService

        when: "Token cleanup is executed"
        tokenCleanupService.execute()

        then: "Expired token is cleaned up"
        1 * odpAuthDataService.cleanupExpiredTokens(_ as Instant)
    }
}