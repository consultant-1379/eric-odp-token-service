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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class which generates new token names.
 */
@ApplicationScoped
public class TokenNameService {

    private static final String SEPARATOR = "-";
    private static final String ODPTOKEN_SECRET_NAME_PREFIX = "odptoken-secret";
    private final AtomicInteger odpTokenSecretIndex = new AtomicInteger();

    @Inject
    TokenServiceSetup tokenServiceSetup;

    /**
     * Gets a new token name generated.
     *
     * @return - A new token name.
     */
    public String getNewTokenName() {
        return ODPTOKEN_SECRET_NAME_PREFIX
                + SEPARATOR + tokenServiceSetup.getTokenServiceTimestamp()
                + SEPARATOR + odpTokenSecretIndex.incrementAndGet();
    }
}