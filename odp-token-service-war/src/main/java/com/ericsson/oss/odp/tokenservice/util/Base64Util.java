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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class which deals with the base64 encoding.
 */
public class Base64Util {

    private Base64Util() {}

    /**
     * Encodes a raw string to base64 format.
     *
     * @param raw - The string value to be encoded.
     * @return The base64 string representation.
     */
    public static String encode(final String raw) {
        return encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a byte array to base64 format.
     *
     * @param bytes - The byte array to be encoded.
     * @return The base64 string representation.
     */
    public static String encodeToString(final byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * Decodes a base64 encoded string into a raw string.
     *
     * @param encoded - The encoded string value to be decoded.
     * @return The raw string representation.
     */
    public static String decode(final String encoded) {
        return new String(Base64.getDecoder().decode(encoded));
    }
}