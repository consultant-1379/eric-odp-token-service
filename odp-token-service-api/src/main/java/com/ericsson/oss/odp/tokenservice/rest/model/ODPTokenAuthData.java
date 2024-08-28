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

package com.ericsson.oss.odp.tokenservice.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains generated ODP token authentication data.
 */
public class ODPTokenAuthData {

    @Schema(description = "The Token storage name which stores the ODP authentication data." )
    @JsonProperty("tokenname")
    private final String tokenName;

    @Schema(description = "The ODP Token data represented in the form of string name/value pairs.",
            example = "tokendata : { odptoken: <generated-sso-token> }")
    @JsonProperty("tokendata")
    private final Map<String, String> tokenData;

    @ArraySchema(arraySchema = @Schema(description = "Type of ODP authentication data to be generated.",
            example = "e.g. tokentype: [ sso ]")
    )
    @JsonProperty("tokentypes")
    private final List<ODPTokenGenerateParams.TokenType> tokenTypes;

    public ODPTokenAuthData(final String tokenName, final Map<String, String> tokenData, final List<ODPTokenGenerateParams.TokenType> tokenTypes) {
        this.tokenName = tokenName;
        this.tokenData = tokenData;
        this.tokenTypes = tokenTypes;
    }

    public String getTokenName() {
        return tokenName;
    }

    public Map<String, String> getTokenData() {
        return tokenData;
    }

    public List<ODPTokenGenerateParams.TokenType> getTokenTypes() {
        return tokenTypes;
    }
}