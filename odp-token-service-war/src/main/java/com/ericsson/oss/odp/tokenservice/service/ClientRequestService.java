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

package com.ericsson.oss.odp.tokenservice.service;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;
import com.ericsson.oss.odp.tokenservice.model.OdpToken;

/**
 * Class with user impersonate and tokenId logout operations.
 */
@Singleton
public class ClientRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestService.class);

    private static final String BASE_PATH = System.getenv("SSOSERVICE_URL");
    static final String USER_IMPERSONATE_RESOURCE_PATH = BASE_PATH + "/singlesignon/impersonation/impersonate";
    static final String USER_LOGOUT_RESOURCE_PATH = BASE_PATH + "/singlesignon/logout";

    @Inject
    Client httpClient;

    /**
     * Operation to impersonate a user matching the given username.
     *
     * @param username The username to be impersonated
     * @return The ODP Token for the impersonated user
     * @throws TokenServiceException If the impersonation is unsuccessful
     */
    public OdpToken impersonateUser(final String username) throws TokenServiceException {
        LOGGER.debug("Impersonating the user by {} username.", username);
        final WebTarget target = httpClient.target(USER_IMPERSONATE_RESOURCE_PATH);
        final Invocation.Builder invocationBuilder = target.request().header("X-OpenAM-Username", username);
        try (final Response response = invocationBuilder.post(null)) {
            final int statusCode = response.getStatusInfo().getStatusCode();
            if (statusCode != Response.Status.OK.getStatusCode()) {
                final String errorMsg = response.hasEntity() ? String.valueOf(response.readEntity(String.class)) : "";
                LOGGER.error("User impersonate request finished with an unsuccessful {} status. Response message: {}", statusCode, errorMsg);
                throw new TokenServiceException("Failure impersonating the user.");
            }
            return response.readEntity(OdpToken.class);
        }
    }

    /**
     * Operation to logout the given tokenId.
     *
     * @param tokenId The tokenId to be logged out
     * @return true if the logout was successful, false otherwise
     */
    public boolean logoutUser(final String tokenId) {
        LOGGER.debug("Logging out the user by {} token id.", tokenId);
        WebTarget target = httpClient.target(USER_LOGOUT_RESOURCE_PATH);
        final Invocation.Builder invocationBuilder = target.request().cookie("iPlanetDirectoryPro", tokenId);
        try (final Response response = invocationBuilder.get()) {
            final int statusCode = response.getStatusInfo().getStatusCode();
            if (statusCode != Response.Status.FOUND.getStatusCode()) {
                final String errorMsg = response.hasEntity() ? String.valueOf(response.readEntity(String.class)) : "";
                LOGGER.error("User logout request finished with an unsuccessful {} status. Response message: {}", statusCode, errorMsg);
                return false;
            }
            return true;
        }
    }
}