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

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException
import com.ericsson.oss.odp.tokenservice.model.OdpToken
import spock.lang.Specification

import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

class ClientRequestServiceSpec extends Specification {

    def ODPTOKEN_ID = "gp1KltivWJTCCvHmedQkHaZsKuw.*AAJTSQACMDEAAlNLABxMWVhkc2IweXkwY3BZaUZHUVVZSzlhaUVMQzQ9AAR0eXBlAANDVFMAAlMxAAA.*"

    def "Impersonates a give user"() {
        given: "An username to impersonate"
        def username = "odpuser"
        ClientRequestService clientRequestService = new ClientRequestService()
        clientRequestService.httpClient = Stub(Client) {
            target(ClientRequestService.USER_IMPERSONATE_RESOURCE_PATH) >> Stub(WebTarget) {
                request() >> Stub(Invocation.Builder) {
                    header("X-OpenAM-Username", username) >> Stub(Invocation.Builder) {
                        post(null) >> Stub(Response) {
                            getStatusInfo() >> Stub(Response.StatusType) {
                                getStatusCode() >> Response.Status.OK.getStatusCode()
                            }
                            def odpToken = new OdpToken()
                            odpToken.setTokenId(ODPTOKEN_ID)
                            readEntity(OdpToken.class) >> odpToken
                        }
                    }
                }
            }
        }

        when: "User is impersonated"
        def odpTokenOutput = clientRequestService.impersonateUser(username)

        then: "OdpToken is returned as output"
        odpTokenOutput.tokenId == ODPTOKEN_ID
    }

    def "Failure impersonating a give user"() {
        given: "An username to impersonate"
        def username = "odpuser"
        ClientRequestService clientRequestService = new ClientRequestService()
        clientRequestService.httpClient = Stub(Client) {
            target(ClientRequestService.USER_IMPERSONATE_RESOURCE_PATH) >> Stub(WebTarget) {
                request() >> Stub(Invocation.Builder) {
                    header("X-OpenAM-Username", username) >> Stub(Invocation.Builder) {
                        post(null) >> Stub(Response) {
                            getStatusInfo() >> Stub(Response.StatusType) {
                                getStatusCode() >> Response.Status.UNAUTHORIZED.getStatusCode()
                            }
                        }
                    }
                }
            }
        }

        when: "User is impersonated"
        clientRequestService.impersonateUser(username)

        then: "Failure impersonating the given user"
        def e = thrown(TokenServiceException)
        e.getMessage() == "Failure impersonating the user."
    }

    def "Successful tokenId logout"() {
        given: "A tokenId to revoke"
        ClientRequestService clientRequestService = new ClientRequestService()
        clientRequestService.httpClient = Stub(Client) {
            target(ClientRequestService.USER_LOGOUT_RESOURCE_PATH) >> Stub(WebTarget) {
                request() >> Stub(Invocation.Builder) {
                    cookie("iPlanetDirectoryPro", ODPTOKEN_ID) >> Stub(Invocation.Builder) {
                        get() >> Stub(Response) {
                            getStatusInfo() >> Stub(Response.StatusType) {
                                getStatusCode() >> Response.Status.FOUND.getStatusCode()
                            }
                        }
                    }
                }
            }
        }

        when: "TokenId is logged out"
        def logoutSuccess = clientRequestService.logoutUser(ODPTOKEN_ID)

        then: "TokenId logout is successful"
        logoutSuccess
    }

    def "Unsuccessful tokenId logout"() {
        given: "A tokenId to revoke"
        ClientRequestService clientRequestService = new ClientRequestService()
        clientRequestService.httpClient = Stub(Client) {
            target(ClientRequestService.USER_LOGOUT_RESOURCE_PATH) >> Stub(WebTarget) {
                request() >> Stub(Invocation.Builder) {
                    cookie("iPlanetDirectoryPro", ODPTOKEN_ID) >> Stub(Invocation.Builder) {
                        get() >> Stub(Response) {
                            getStatusInfo() >> Stub(Response.StatusType) {
                                getStatusCode() >> Response.Status.BAD_REQUEST.getStatusCode()
                            }
                        }
                    }
                }
            }
        }

        when: "TokenId is logged out"
        def logoutSuccess = clientRequestService.logoutUser(ODPTOKEN_ID)

        then: "TokenId logout is unsuccessful"
        !logoutSuccess
    }
}