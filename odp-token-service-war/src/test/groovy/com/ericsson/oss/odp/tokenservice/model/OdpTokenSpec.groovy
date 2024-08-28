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

package com.ericsson.oss.odp.tokenservice.model

import spock.lang.Specification

class OdpTokenSpec extends Specification {

    def ODPTOKEN_ID = "gp1KltivWJTCCvHmedQkHaZsKuw.*AAJTSQACMDEAAlNLABxMWVhkc2IweXkwY3BZaUZHUVVZSzlhaUVMQzQ9AAR0eXBlAANDVFMAAlMxAAA.*"

    def "OdpToken object encapsulates tokenId"() {
        given: "A tokenId"
        OdpToken odpToken = new OdpToken();
        odpToken.setTokenId(ODPTOKEN_ID)

        expect: "OdpToken object encapsulates the tokenId"
        odpToken.getTokenId() == ODPTOKEN_ID
    }

    def "OdpToken instances are equal when encapsulating same data"() {
        given: "Two OdpToken instances"
        OdpToken odpToken1 = new OdpToken();
        odpToken1.setTokenId(ODPTOKEN_ID)
        OdpToken odpToken2 = new OdpToken();
        odpToken2.setTokenId(ODPTOKEN_ID)

        expect: "Equality is satisfied"
        odpToken1.equals(odpToken2)
        odpToken1.hashCode() == odpToken2.hashCode()
    }

}
