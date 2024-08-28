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

package com.ericsson.oss.odp.tokenservice.rest

import javax.ws.rs.core.Response

import spock.lang.Specification

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException
import com.ericsson.oss.odp.tokenservice.rest.api.OdpTokenServiceApi
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenAuthData
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams
import com.ericsson.oss.odp.tokenservice.service.OdpAuthDataService
import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants

class OdpTokenServiceSpec extends Specification {
    def TOKEN_NAME = "odptoken-secret-20240509125212-1"
    def BASE64_ODPTOKEN = "Z3AxS2x0aXZXSlRDQ3ZIbWVkUWtIYVpzS3V3LipBQUpUU1FBQ01ERUFBbE5MQUJ4TVdWaGtjMkl3ZVhrd1kzQlphVVpIVVZWWlN6bGhhVVZNUXpROUFBUjBlWEJsQUFORFZGTUFBbE14QUFBLioK"

    def "Get ODP token by token name"() {
        given: "ODP token params"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService) {
            getOdpAuthData(TOKEN_NAME) >> new ODPTokenAuthData(TOKEN_NAME, [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): BASE64_ODPTOKEN], [])
        }

        when: "ODP token is fetched by name"
        def response = odpTokenService.getOdpToken(TOKEN_NAME)

        then: "The response status is OK and the entity contains the authentication data"
        with(response) {
            getStatus() == Response.Status.OK.statusCode
            ((ODPTokenAuthData)getEntity()).tokenName == TOKEN_NAME
            ((ODPTokenAuthData)getEntity()).tokenData[OdpTokenConstants.ODP_TOKEN_DATA_KEY] == BASE64_ODPTOKEN
        }
    }

    def "Failure to get ODP token by token name"() {
        given: "An exception is thrown while fetching the ODP token"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        def expectedErrorMessage = "No ODP Token exists with [" + TOKEN_NAME + "] token name."
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService) {
            getOdpAuthData(TOKEN_NAME) >> {throw new TokenServiceException(expectedErrorMessage)}
        }

        when: "ODP token is fetched by name"
        def response = odpTokenService.getOdpToken(TOKEN_NAME)

        then: "The response status is INTERNAL_SERVER_ERROR and the entity contains the error message"
        with(response) {
            getStatus() == Response.Status.INTERNAL_SERVER_ERROR.statusCode
            getEntity().toString() == expectedErrorMessage
        }
    }

    def "Create ODP token"() {
        given: "ODP token params"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        def createForUsername = "odpusername"
        def createForTokenTypes = [ODPTokenGenerateParams.TokenType.SSO]
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService) {
            createOdpAuthData(createForUsername, createForTokenTypes) >> new ODPTokenAuthData(TOKEN_NAME, [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): BASE64_ODPTOKEN], [])
        }

        when: "ODP token is created"
        def params = new ODPTokenGenerateParams()
        params.username = createForUsername
        params.tokenTypes = [ODPTokenGenerateParams.TokenType.SSO]
        def response = odpTokenService.createOdpToken(params)

        then: "The response status is OK and the entity contains the authentication data"
        with(response) {
            getStatus() == Response.Status.OK.statusCode
            ((ODPTokenAuthData)getEntity()).tokenName == TOKEN_NAME
            ((ODPTokenAuthData)getEntity()).tokenData[OdpTokenConstants.ODP_TOKEN_DATA_KEY] == BASE64_ODPTOKEN
        }
    }

    def "Failure to create ODP token"() {
        given: "An exception is thrown while creating the ODP token"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        def expectedErrorMessage = "The username param is mandatory."
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService) {
            createOdpAuthData(null, [ODPTokenGenerateParams.TokenType.SSO]) >> {throw new TokenServiceException(expectedErrorMessage)}
        }

        when: "ODP token is created"
        def params = new ODPTokenGenerateParams()
        params.tokenTypes = [ODPTokenGenerateParams.TokenType.SSO]
        def response = odpTokenService.createOdpToken(params)

        then: "The response status is INTERNAL_SERVER_ERROR and the entity contains the error message"
        with(response) {
            getStatus() == Response.Status.INTERNAL_SERVER_ERROR.statusCode
            getEntity().toString() == expectedErrorMessage
        }
    }

    def "Delete ODP token by token name"() {
        given: "ODP token params"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService)

        when: "ODP token is deleted by name"
        def response = odpTokenService.deleteOdpToken(TOKEN_NAME)

        then: "The response status is OK"
        response.getStatus() == Response.Status.OK.statusCode
    }

    def "Failure to delete ODP token by token name"() {
        given: "An exception is thrown while deleting the ODP token"
        OdpTokenServiceApi odpTokenService = new OdpTokenServiceImpl()
        def expectedErrorMessage = "No ODP Token exists with [" + TOKEN_NAME + "] token name."
        odpTokenService.odpAuthDataService = Stub(OdpAuthDataService) {
            deleteOdpAuthData(TOKEN_NAME) >> {throw new TokenServiceException(expectedErrorMessage)}
        }

        when: "ODP token is deleted by name"
        def response = odpTokenService.deleteOdpToken(TOKEN_NAME)

        then: "The response status is INTERNAL_SERVER_ERROR and the entity contains the error message"
        with(response) {
            getStatus() == Response.Status.INTERNAL_SERVER_ERROR.statusCode
            getEntity().toString() == expectedErrorMessage
        }
    }
}
