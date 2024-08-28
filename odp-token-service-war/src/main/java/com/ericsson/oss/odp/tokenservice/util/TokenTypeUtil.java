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

package com.ericsson.oss.odp.tokenservice.util;

import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for string manipulation.
 */
public class TokenTypeUtil {

    private TokenTypeUtil() {
    }

    /**
     * Converts a list of token types into a comma separated string.
     *
     * @param tokenTypes - The token types to be converted.
     * @return The comma separated string of token types.
     */
    public static String tokenTypesToStringList(final List<ODPTokenGenerateParams.TokenType> tokenTypes) {
        return tokenTypes.stream()
                .map(ODPTokenGenerateParams.TokenType::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Converts a comma separated string to a list of token type objects.
     *
     * @param tokenTypeStrings - The comma separated string to be converted.
     * @return The list of token type objects.
     */
    public static List<ODPTokenGenerateParams.TokenType> stringListToTokenTypes(final String tokenTypeStrings) {
        if (tokenTypeStrings == null || tokenTypeStrings.isEmpty()) {
            return Collections.emptyList();
        }
        final String[] splitTokenTypes = tokenTypeStrings.split(",");
        final List<ODPTokenGenerateParams.TokenType> tokenTypes = new ArrayList<>(splitTokenTypes.length);
        for (final String tt : splitTokenTypes) {
            tokenTypes.add(ODPTokenGenerateParams.TokenType.fromValue(tt));
        }
        return tokenTypes;
    }
}