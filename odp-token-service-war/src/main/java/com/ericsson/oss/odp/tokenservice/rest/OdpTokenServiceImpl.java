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

package com.ericsson.oss.odp.tokenservice.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;
import com.ericsson.oss.odp.tokenservice.service.OdpAuthDataService;
import com.ericsson.oss.odp.tokenservice.rest.api.OdpTokenServiceApi;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenAuthData;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams;

/**
 * Implementation of the REST resources for creating, fetching and deleting ODP tokens.
 */
@Stateless
public class OdpTokenServiceImpl implements OdpTokenServiceApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdpTokenServiceImpl.class);

    @Inject
    OdpAuthDataService odpAuthDataService;

    /**
     * Gets the ODP token by the provided token name.
     *
     * @param tokenName - The token name to get the token authentication data for.
     * @return A response encapsulating the fetched token authentication data.
     */
    @Override
    public Response getOdpToken(final String tokenName) {
        try {
            final ODPTokenAuthData odpAuthDataEntity = odpAuthDataService.getOdpAuthData(tokenName);
            return Response.ok(odpAuthDataEntity).build();
        } catch (TokenServiceException e) {
            LOGGER.error("A failure has occurred which getting the ODP token: " + e.getMessage());
            return Response.serverError().entity((e.getMessage())).build();
        }
    }

    /**
     * Creates a new authentication data based on the provided token generate parameters.
     *
     * @param params - The parameters which dictates the type of authentication data to be generated.
     * @return A response encapsulating the created token authentication data.
     */
    @Override
    public Response createOdpToken(final ODPTokenGenerateParams params) {
        try {
            final ODPTokenAuthData odpAuthDataEntity = odpAuthDataService.createOdpAuthData(params.getUsername(), params.getTokenTypes());
            return Response.ok(odpAuthDataEntity).build();
        } catch (TokenServiceException e) {
            LOGGER.error("A failure has occurred while creating the ODP token: " + e.getMessage());
            return Response.serverError().entity((e.getMessage())).build();
        }
    }

    /**
     * Deletes the ODP token by the provided token name param.
     *
     * @param tokenName - The token name to delete the token authentication data for.
     * @return A response encapsulating the delete outcome.
     */
    @Override
    public Response deleteOdpToken(final String tokenName) {
        try {
            odpAuthDataService.deleteOdpAuthData(tokenName);
            return Response.ok().build();
        } catch (TokenServiceException e) {
            LOGGER.error("A failure has occurred while deleting the ODP token: " + e.getMessage());
            return Response.serverError().entity((e.getMessage())).build();
        }
    }
}
