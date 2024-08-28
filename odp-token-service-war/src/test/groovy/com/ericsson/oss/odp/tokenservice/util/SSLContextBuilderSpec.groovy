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
import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException

class SSLContextBuilderSpec extends Specification {

    def "Build an SSL Context instance"() {
        given: "An SSL Context builder instance with keystore and truststore jks files"
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder()
        sslContextBuilder.odpTokenKeystorePath = getClass().getResource('/jks/keystore.jks').path
        sslContextBuilder.odpTokenTruststorePath = getClass().getResource('/jks/truststore.jks').path

        expect: "An SSL Context instance as output"
        sslContextBuilder.buildSSLContext()
    }

    def "Fail building an SSL Context instance"() {
        given: "An SSL Context builder instance"
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder()

        when: "An SSL Context instance is build"
        sslContextBuilder.buildSSLContext()

        then: "Failure building an SSL Context instance"
        def e = thrown(TokenServiceException)
        e.getMessage() == "A security error has occurred."
    }
}