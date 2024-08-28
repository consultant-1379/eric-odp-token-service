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

package com.ericsson.oss.odp.tokenservice.rest.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;

import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenAuthData;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams;

/**
 * REST resources for creating, fetching and deleting ODP tokens.
 */
@OpenAPIDefinition(
        info = @Info(
                version = "0.0.5",
                title = "ODP Token Service API",
                description = "An API for managing ODP authentication data for users."
        ),
        servers = {
                @Server(url = "http://{ODP_TOKEN_SERVICE_HOSTNAME}:{ODP_TOKEN_SERVICE_HTTP_PORT}/",
                        variables = {
                                @ServerVariable(name = "ODP_TOKEN_SERVICE_HOSTNAME", defaultValue = "eric-odp-token-service"),
                                @ServerVariable(name = "ODP_TOKEN_SERVICE_HTTP_PORT", defaultValue = "8080")
                        })
        }
)
@Path("/odp-token")
public interface OdpTokenServiceApi {

    @GET
    @Path("/{tokenname}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Returns the ODP authentication data from the Token storage fetched by the provided token name.",
            tags = "OdpTokenService",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful fetch of the ODP authentication data from the Token storage by the provided token name.",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(implementation = ODPTokenAuthData.class))
                            }
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failure fetching the ODP authentication data for the provided token name."
                    )
            }
    )
    Response getOdpToken(@PathParam("tokenname") @Parameter(description = "Token storage name to fetch the ODP authentication data from.") final String tokenName);

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Creates new ODP Token storage which stores the ODP authentication data for the provided ODP token generate parameters.",
            tags = "OdpTokenService",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful storage of the ODP authentication data into a new Token storage for the provided ODP token generate parameters.",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(implementation = ODPTokenAuthData.class))
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad parameters name or bad tokentype enum value(s) were provided."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failure generating the ODP authentication data or creating new Token storage for the provided ODP token generate parameters."
                    )
            }
    )
    Response createOdpToken(final ODPTokenGenerateParams odpTokenGenerateParams);

    @DELETE
    @Path("/{tokenname}")
    @Operation(
            summary = "Deletes the Token storage and invalidates the ODP authentication data for the provided token name.",
            tags = "OdpTokenService",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful Token storage deletion and ODP authentication data invalidation queuing for the provided token name."
                    )
            }
    )
    Response deleteOdpToken(@PathParam("tokenname") @Parameter(description = "Token storage name the ODP authentication data to be invalidated from.") final String tokenName);

}