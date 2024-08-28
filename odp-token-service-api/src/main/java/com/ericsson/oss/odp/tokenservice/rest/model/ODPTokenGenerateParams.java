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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Contains parameters used to generate ODP tokens.
 */
public class ODPTokenGenerateParams {

    public enum TokenType {
        SSO("sso"),
        SSH_KEY_PAIR("sshkeypair");

        private final String tokenName;

        TokenType(final String tokenName) {
            this.tokenName = tokenName;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(tokenName);
        }

        @JsonCreator
        public static TokenType fromValue(final String value) {
            for (final TokenType type : TokenType.values()) {
                if (type.tokenName.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unexpected token name '" + value + "'");
        }
    }

    @Schema(description = "User to generate the ODP authentication data for.")
    @JsonProperty("username")
    private String username;

    @ArraySchema(arraySchema = @Schema(description = "The ODP authentication data type to be generated.",
            example = "e.g. tokentype: [ sso ]")
    )
    @JsonProperty("tokentypes")
    private List<TokenType> tokenTypes;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TokenType> getTokenTypes() {
        return tokenTypes;
    }

    public void setTokenTypes(List<TokenType> tokenTypes) {
        this.tokenTypes = tokenTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ODPTokenGenerateParams that = (ODPTokenGenerateParams) o;
        return Objects.equals(username, that.username) && Objects.equals(tokenTypes, that.tokenTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, tokenTypes);
    }
}