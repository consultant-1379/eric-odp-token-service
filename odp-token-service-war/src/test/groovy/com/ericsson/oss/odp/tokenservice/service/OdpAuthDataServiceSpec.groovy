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

import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Secret
import spock.lang.Specification

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException
import com.ericsson.oss.odp.tokenservice.manager.K8sSecretManager
import com.ericsson.oss.odp.tokenservice.model.OdpToken
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams
import com.ericsson.oss.odp.tokenservice.util.Base64Util
import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants

import java.time.Instant
import java.time.temporal.ChronoUnit

class OdpAuthDataServiceSpec extends Specification {

    def TOKEN_NAME = "odptoken-secret-20240509125212-1"
    def ODPTOKEN = "gp1KltivWJTCCvHmedQkHaZsKuw.*AAJTSQACMDEAAlNLABxMWVhkc2IweXkwY3BZaUZHUVVZSzlhaUVMQzQ9AAR0eXBlAANDVFMAAlMxAAA.*"
    def BASE64_ODPTOKEN = Base64Util.encode(ODPTOKEN)

    def "Get ODP authentication data by token name"() {
        given: "ODP authentication data for the given name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        authDataService.k8SSecretManager = Stub(K8sSecretManager) {
            getSecretByName(TOKEN_NAME) >> Stub(Secret) {
                getMetadata() >> Stub(ObjectMeta) {
                    getAnnotations() >> [(OdpTokenConstants.ODP_TOKEN_TYPES_ANNOTATION): "sso"]
                    getName() >> TOKEN_NAME
                }
                getData() >> [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): BASE64_ODPTOKEN]
            }
        }

        when: "ODP authentication data is fetched by name"
        def tokenAuthData = authDataService.getOdpAuthData(TOKEN_NAME)

        then: "The authentication data is returned"
        with(tokenAuthData) {
            tokenName == TOKEN_NAME
            tokenData[(OdpTokenConstants.ODP_TOKEN_DATA_KEY)] == ODPTOKEN
            tokenData[(OdpTokenConstants.SSH_PRIV_KEY)] == null
            tokenTypes == [ODPTokenGenerateParams.TokenType.SSO]
        }
    }

    def "No ODP Token exists for the provided token name"() {
        given: "No ODP authentication data represented by a particular name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        authDataService.k8SSecretManager = Mock(K8sSecretManager)

        when: "ODP authentication data is fetched by name"
        authDataService.getOdpAuthData(TOKEN_NAME)

        then: "Failure getting the ODP authentication data"
        def e = thrown(TokenServiceException)
        e.getMessage() == "No ODP Token exists with [" + TOKEN_NAME + "] token name."
    }

    def "Create ODP authentication data"() {
        given: "ODP authentication data params"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        def generateForUsername = "odpusername"
        def generateForTokenTypes = [
                ODPTokenGenerateParams.TokenType.SSO,
                ODPTokenGenerateParams.TokenType.SSH_KEY_PAIR
        ]
        authDataService.k8SSecretManager = Stub(K8sSecretManager) {
            createSecret(generateForTokenTypes, _ as Map) >> TOKEN_NAME
        }
        authDataService.clientRequestService = Stub(ClientRequestService) {
            def odpToken = new OdpToken()
            odpToken.setTokenId(ODPTOKEN)
            impersonateUser(generateForUsername) >> odpToken
        }

        when: "ODP authentication data is created"
        def tokenAuthData = authDataService.createOdpAuthData(generateForUsername, generateForTokenTypes)

        then: "The Token name which stores the authentication data is returned along with ODP Token and SSH public key"
        with(tokenAuthData) {
            tokenName == TOKEN_NAME
            tokenData[(OdpTokenConstants.ODP_TOKEN_DATA_KEY)] == ODPTOKEN
            tokenData[(OdpTokenConstants.SSH_PRIV_KEY)] == null
            tokenData[(OdpTokenConstants.SSH_PUB_KEY)].startsWith("ssh-rsa")
            tokenTypes == []
        }
    }

    def "Null username fails to create ODP authentication data"() {
        given: "ODP authentication data params with null username"
        OdpAuthDataService authDataService = new OdpAuthDataService()

        when: "ODP authentication data is created with null username"
        authDataService.createOdpAuthData(null, [ODPTokenGenerateParams.TokenType.SSO])

        then: "Failure creating the ODP authentication data"
        def e = thrown(TokenServiceException)
        e.getMessage() == "The username param is mandatory."
    }

    def "Empty username fails to create ODP authentication data"() {
        given: "ODP authentication data params with empty username"
        OdpAuthDataService authDataService = new OdpAuthDataService()

        when: "ODP authentication data is created with empty username"
        authDataService.createOdpAuthData("", [ODPTokenGenerateParams.TokenType.SSO])

        then: "Failure creating the ODP authentication data"
        def e = thrown(TokenServiceException)
        e.getMessage() == "The username param is mandatory."
    }

    def "Missing tokenTypes fail to create ODP authentication data"() {
        given: "ODP authentication data params"
        OdpAuthDataService authDataService = new OdpAuthDataService()

        when: "ODP authentication data is created with empty token types list"
        def tokenAuthData = authDataService.createOdpAuthData("odpusername", [])

        then: "Failure creating the ODP authentication data"
        def e = thrown(TokenServiceException)
        e.getMessage() == "No tokentypes have been specified."
    }

    def "Delete ODP authentication data by token name with ODP token logout"() {
        given: "ODP authentication data for a given name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        def k8SSecretManager = Mock(K8sSecretManager) {
            getSecretByName(TOKEN_NAME) >> Stub(Secret) {
                getData() >> [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): BASE64_ODPTOKEN]
            }
        }
        authDataService.k8SSecretManager = k8SSecretManager
        authDataService.clientRequestService = Stub(ClientRequestService) {
            logoutUser(ODPTOKEN) >> true
        }

        when: "ODP authentication data is deleted by name"
        authDataService.deleteOdpAuthData(TOKEN_NAME)

        then: "The Secret is deleted by name"
        1 * k8SSecretManager.deleteSecretByName(TOKEN_NAME)
    }

    def "Delete ODP authentication data by token name without ODP token logout"() {
        given: "ODP authentication data for a given name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        def k8SSecretManager = Mock(K8sSecretManager) {
            getSecretByName(TOKEN_NAME) >> Stub(Secret) {
                getData() >> [(OdpTokenConstants.SSH_PUB_KEY): "ssh-rsa"]
            }
        }
        authDataService.k8SSecretManager = k8SSecretManager

        when: "ODP authentication data is deleted by name"
        authDataService.deleteOdpAuthData(TOKEN_NAME)

        then: "The Secret is deleted by name"
        1 * k8SSecretManager.deleteSecretByName(TOKEN_NAME)
    }

    def "No authentication data deletion due to unsuccessful logout"() {
        given: "ODP authentication data for the given name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        def k8SSecretManager = Mock(K8sSecretManager) {
            getSecretByName(TOKEN_NAME) >> Stub(Secret) {
                getData() >> [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): BASE64_ODPTOKEN]
            }
        }
        authDataService.k8SSecretManager = k8SSecretManager
        authDataService.clientRequestService = Stub(ClientRequestService) {
            logoutUser(ODPTOKEN) >> false
        }

        when: "ODP authentication data is deleted by name"
        authDataService.deleteOdpAuthData(TOKEN_NAME)

        then: "The Secret is not deleted due to unsuccessful logout"
        0 * k8SSecretManager.deleteSecretByName(TOKEN_NAME)
    }

    def "No ODP authentication data found for deletion"() {
        given: "No ODP authentication data for the given name exists"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        authDataService.k8SSecretManager = Mock(K8sSecretManager)

        when: "ODP authentication data is deleted by name"
        authDataService.deleteOdpAuthData(TOKEN_NAME)

        then: "Failure deleting the ODP authentication data"
        def e = thrown(TokenServiceException)
        e.getMessage() == "No ODP Token exists with [" + TOKEN_NAME + "] token name."
    }

    def "Cleanup ODP authentication data having TTL expired"() {
        given: "3 odp-token-service labeled Secrets exist; 2 have the TTL expired, but 1 fails to be deleted"
        OdpAuthDataService authDataService = new OdpAuthDataService()
        def k8SSecretManager = Mock(K8sSecretManager) {
            getSecretByName(_ as String) >> Stub(Secret) >> null
            getOdpTokenServiceLabeledSecrets() >> createLabeledSecrets()
        }
        authDataService.k8SSecretManager = k8SSecretManager
        authDataService.clientRequestService = Stub(ClientRequestService) {
            logoutUser(_ as String) >> true
        }
        final Instant cleanupTargetTimestamp = Instant.now().minus(24, ChronoUnit.HOURS);

        when: "TTL expired ODP authentication data gets cleaned up "
        def cleanupCount = authDataService.cleanupExpiredTokens(cleanupTargetTimestamp)

        then: "1 Secret out of 2 with the expired TTL is deleted"
        1 * k8SSecretManager.deleteSecretByName(_ as String)
        cleanupCount == 1
    }

    List<Secret> createLabeledSecrets() {
        def labels = [(OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL): OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE]
        def secretList = []
        1.upto(3, {
            Secret secret = new Secret()
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName("odptoken-secret-20240509125212-" + it)
            objectMeta.setLabels(labels)
            Instant tokenCreationTimestamp = Instant.now().minus((int) it * 12, ChronoUnit.HOURS)
            objectMeta.creationTimestamp = tokenCreationTimestamp.toString()
            secret.setMetadata(objectMeta)
            secretList << secret
        })
        secretList
    }
}
