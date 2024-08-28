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

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;

/**
 * Class used to clean up ODP tokens having their TTL expired.
 */
@Singleton
@Startup
public class TokenCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenCleanupService.class);
    private static final int TOKEN_TTL_HOURS = 24;

    @Inject
    OdpAuthDataService odpAuthDataService;

    /**
     * Method that gets invoked every hour to execute clean up of ODP tokens having their TTL expired.
     */
    @Schedule(hour = "*/1", persistent = false)
    public void execute() {
        final Instant cleanupTargetTimestamp = Instant.now().minus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);
        LOGGER.info("Tokens cleanup started, targeting ODP Token Data older than {}.", cleanupTargetTimestamp);
        try {
            final int cleanupCount = odpAuthDataService.cleanupExpiredTokens(cleanupTargetTimestamp);
            LOGGER.info("Token cleanup finished with {} tokens being deleted.", cleanupCount);
        } catch (final TokenServiceException e) {
            LOGGER.error("Tokens cleanup finished with an error due to: {}", e.getMessage());
        }
    }
}