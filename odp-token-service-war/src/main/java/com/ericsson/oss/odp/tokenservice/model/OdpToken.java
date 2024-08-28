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

package com.ericsson.oss.odp.tokenservice.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains SSO tokenId
 */
public class OdpToken {

    @JsonProperty("tokenId")
    private String tokenId;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdpToken odpToken = (OdpToken) o;
        return Objects.equals(tokenId, odpToken.tokenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId);
    }
}
