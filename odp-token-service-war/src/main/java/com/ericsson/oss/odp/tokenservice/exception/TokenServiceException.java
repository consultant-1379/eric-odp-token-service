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

package com.ericsson.oss.odp.tokenservice.exception;

/**
 * An TokenServiceException is thrown when the application fails to
 * create, fetch or delete ODP tokens.
 */
public class TokenServiceException extends Exception {

    public TokenServiceException(String message) {
        super(message);
    }
}