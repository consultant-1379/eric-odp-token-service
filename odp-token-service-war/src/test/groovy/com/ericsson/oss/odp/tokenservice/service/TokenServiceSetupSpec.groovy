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

import io.fabric8.kubernetes.client.KubernetesClientException
import spock.lang.Specification

import com.ericsson.oss.odp.tokenservice.util.SSLContextBuilder

class TokenServiceSetupSpec extends Specification {
    def expectedTimestamp = "^((2[0-9]{1})[0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])([0-5][0-9])([0-5][0-9])([0-5][0-9])\$"

    def "Get token service startup timestamp"() {
        given: "A TokenServiceSetup service instance"
        def tokenServiceSetup = new TokenServiceSetup()

        expect: "The token timestamp matches yyyyMMddHHmmss format"
        tokenServiceSetup.getTokenServiceTimestamp().matches(expectedTimestamp)
    }

    def "Produce a KubernetesClient CDI instance"() {
        given: "A TokenServiceSetup service instance"
        def tokenServiceSetup = new TokenServiceSetup()

        expect: "A KubernetesClient CDI instance"
        tokenServiceSetup.getKubernetesClient()
    }

    def "Produce an Http Client CDI instance"() {
        given: "A TokenServiceSetup service instance"
        def tokenServiceSetup = new TokenServiceSetup()
        tokenServiceSetup.sslContextBuilder = Stub(SSLContextBuilder) {
            buildSSLContext() >> null
        }
        expect: "An Http Client CDI instance"
        tokenServiceSetup.getHttpClient()
    }

    def "Failure producing an Http Client CDI instance"() {
        given: "An exception is thrown while building the Http Client"
        def tokenServiceSetup = new TokenServiceSetup()
        tokenServiceSetup.sslContextBuilder = Stub(SSLContextBuilder) {
            buildSSLContext() >> {throw new KubernetesClientException("A security error has occurred.")}
        }

        when: "Http Client instance is build"
        tokenServiceSetup.getHttpClient()

        then: "Failure producing an Http Client CDI instance"
        def e = thrown(RuntimeException)
        e.getMessage() == "An error has occurred while starting up the odp-token-service. A security error has occurred."
    }

    def "Get ODP token service keystore path"() {
        given: "A TokenServiceSetup service instance"
        def tokenServiceSetup = new TokenServiceSetup()

        expect: "The token keystore path is present"
        tokenServiceSetup.getOdpTokenKeystorePath()
    }

    def "Get ODP token service truststore path"() {
        given: "A TokenServiceSetup service instance"
        def tokenServiceSetup = new TokenServiceSetup()

        expect: "The token truststore path is present"
        tokenServiceSetup.getOdpTokenTruststorePath()
    }
}
