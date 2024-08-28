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


import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants;
import com.ericsson.oss.odp.tokenservice.util.SSLContextBuilder;

/**
 * Class instantiated at the service startup which initializes the service components.
 */
@Startup
@ApplicationScoped
public class TokenServiceSetup {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String serviceStartupTimestamp = FORMATTER.format(LocalDateTime.now());
    private static final String TLS_MOUNT_PATH = System.getenv("TLS_MOUNT_PATH");

    @Inject
    SSLContextBuilder sslContextBuilder;

    /**
     * Gets service startup timestamp.
     *
     * @return - The service startup timestamp.
     */
    public String getTokenServiceTimestamp() {
        return serviceStartupTimestamp;
    }

    @Produces
    public KubernetesClient getKubernetesClient() {
        return new KubernetesClientBuilder().build();
    }

    @Produces
    public Client getHttpClient() {
        try {
            final SSLContext sslContext = sslContextBuilder.buildSSLContext();
            return ClientBuilder.newBuilder().sslContext(sslContext).build();
        } catch (Exception e) {
            throw new RuntimeException("An error has occurred while starting up the odp-token-service. " + e.getMessage());
        }
    }

    @Produces
    @Named("odp_token_keystore_path")
    public String getOdpTokenKeystorePath() {
        return TLS_MOUNT_PATH + OdpTokenConstants.ODP_TOKEN_KEYSTORE_PATH;
    }

    @Produces
    @Named("odp_token_truststore_path")
    public String getOdpTokenTruststorePath() {
        return TLS_MOUNT_PATH + OdpTokenConstants.ODP_TOKEN_TRUSTSTORE_PATH;
    }

    @Produces
    @Named("odp_token_jks_pass")
    public char[] getOdpTokenJksPass() {
        return "OnDemandPod2024".toCharArray();
    }
}