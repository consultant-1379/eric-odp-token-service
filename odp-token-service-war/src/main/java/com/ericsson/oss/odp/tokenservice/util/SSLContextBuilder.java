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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;

/**
 * Class used to build Http SSLContext instances.
 */
@Stateless
public class SSLContextBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLContextBuilder.class);

    @Inject
    @Named("odp_token_keystore_path")
    String odpTokenKeystorePath;
    @Inject
    @Named("odp_token_truststore_path")
    String odpTokenTruststorePath;
    @Inject
    @Named("odp_token_jks_pass")
    char[] odpTokenJksPass;

    /**
     * Builds a Http SSLContext instance.
     *
     * @return The build SSLContext instance.
     * @throws TokenServiceException If an error while building the SSLContext instance.
     */
    public SSLContext buildSSLContext() throws TokenServiceException {
        try {
            final KeyManagerFactory keyManagerFactory = buildKeyManagerFactory();
            final TrustManagerFactory trustManagerFactory = buildTrustManagerFactory();

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (final Exception e) {
            LOGGER.error("A failure has occurred while building a SSL context.", e);
            throw new TokenServiceException("A security error has occurred.");
        }
    }

    private KeyManagerFactory buildKeyManagerFactory() throws Exception {
        final KeyStore keyStore = buildKeyStore(odpTokenKeystorePath);
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, odpTokenJksPass);
        return keyManagerFactory;
    }

    private TrustManagerFactory buildTrustManagerFactory() throws Exception {
        final KeyStore trustStore = buildKeyStore(odpTokenTruststorePath);
        final TrustManagerFactory trustManFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManFactory.init(trustStore);
        return trustManFactory;
    }

    private KeyStore buildKeyStore(final String jksFile) throws Exception {
        try (final InputStream keyStoreStream = Files.newInputStream(Paths.get(jksFile))) {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreStream, odpTokenJksPass);
            return keyStore;
        }
    }
}