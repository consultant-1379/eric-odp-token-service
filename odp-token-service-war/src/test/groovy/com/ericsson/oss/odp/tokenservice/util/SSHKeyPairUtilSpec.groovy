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

class SSHKeyPairUtilSpec extends Specification {

    def "Generate RSA SSH key pair"() {
        given: "An RSA Key Pair is generated"
        def generateSshKeyPair = SSHKeyPairUtil.generateRSAKeyPair()

        expect: "Valid SSH private and public keys"
        with(generateSshKeyPair) {
            Base64Util.decode(getPrivateKey()).startsWith("-----BEGIN PRIVATE KEY-----\n")
            Base64Util.decode(getPrivateKey()).endsWith("\n-----END PRIVATE KEY-----")
            Base64Util.decode(getPublicKey()).startsWith("ssh-rsa")
        }
    }
}