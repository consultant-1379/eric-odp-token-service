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

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;
import com.ericsson.oss.odp.tokenservice.manager.K8sSecretManager;
import com.ericsson.oss.odp.tokenservice.model.SSHKeyPair;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenAuthData;
import com.ericsson.oss.odp.tokenservice.model.OdpToken;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams;
import com.ericsson.oss.odp.tokenservice.util.Base64Util;
import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants;
import com.ericsson.oss.odp.tokenservice.util.TokenTypeUtil;
import com.ericsson.oss.odp.tokenservice.util.SSHKeyPairUtil;

@Singleton
public class OdpAuthDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdpAuthDataService.class);

    @Inject
    K8sSecretManager k8SSecretManager;
    @Inject
    ClientRequestService clientRequestService;

    /**
     * Fetches the ODP token authentication data from the storage.
     *
     * @param tokenName - The token name to fetch the token authentication data for.
     * @return The ODPTokenAuthData object encapsulating the fetched data.
     * @throws TokenServiceException - If no ODP Token Secret exists for the given name.
     */
    public ODPTokenAuthData getOdpAuthData(final String tokenName) throws TokenServiceException {
        final Secret fetchedSecret = k8SSecretManager.getSecretByName(tokenName);
        if (fetchedSecret == null) {
            throw new TokenServiceException("No ODP Token exists with [" + tokenName + "] token name.");
        }
        LOGGER.debug("ODP Token Secret details fetched by {} token name: {}.", tokenName, fetchedSecret);
        final ObjectMeta secretMetadata = fetchedSecret.getMetadata();
        final String tokenTypeStrings = secretMetadata.getAnnotations().get(OdpTokenConstants.ODP_TOKEN_TYPES_ANNOTATION);
        return buildODPTokenAuthData(secretMetadata.getName(), fetchedSecret.getData(),
                TokenTypeUtil.stringListToTokenTypes(tokenTypeStrings));
    }

    /**
     * Creates ODP token authentication data for the provided parameters.
     *
     * @param username   - The username to generate and store the authentication data for.
     * @param tokenTypes - The types of the token data to be generated.
     * @return The ODPTokenAuthData object encapsulating the created data.
     */
    public ODPTokenAuthData createOdpAuthData(final String username, final List<ODPTokenGenerateParams.TokenType> tokenTypes) throws TokenServiceException {
        if (username == null || username.isEmpty()) {
            throw new TokenServiceException("The username param is mandatory.");
        }
        if (tokenTypes.isEmpty()) {
            throw new TokenServiceException("No tokentypes have been specified.");
        }

        LOGGER.debug("Creating ODP authentication data for username: {} and token types: {}. ", username, tokenTypes);

        final Map<String, String> tokenData = getTokenData(username, tokenTypes);

        final String tokenName = k8SSecretManager.createSecret(tokenTypes, tokenData);
        LOGGER.debug("The ODP Token Secret with {} token name has been created for {} username.", tokenName, username);
        return buildODPTokenAuthData(tokenName, tokenData, Collections.emptyList());
    }

    private ODPTokenAuthData buildODPTokenAuthData(final String tokenName, final Map<String, String> tokenStorageData,
                                                   final List<ODPTokenGenerateParams.TokenType> tokenTypes) {
        final Map<String, String> outputTokenData = new HashMap<>(tokenStorageData.size());

        for (final Map.Entry<String, String> entry : tokenStorageData.entrySet()) {
            if (!entry.getKey().equals(OdpTokenConstants.SSH_PRIV_KEY)) {
                outputTokenData.put(entry.getKey(), Base64Util.decode(entry.getValue()));
            }
        }
        return new ODPTokenAuthData(tokenName, outputTokenData, tokenTypes);
    }

    private Map<String, String> getTokenData(final String username, final List<ODPTokenGenerateParams.TokenType> tokenTypes) throws TokenServiceException {
        final Map<String, String> tokenData = new HashMap<>(3);

        if (tokenTypes.contains(ODPTokenGenerateParams.TokenType.SSO)) {
            final OdpToken ssoODPToken = clientRequestService.impersonateUser(username);
            tokenData.put(OdpTokenConstants.ODP_TOKEN_DATA_KEY, Base64Util.encode(ssoODPToken.getTokenId()));
        }

        if (tokenTypes.contains(ODPTokenGenerateParams.TokenType.SSH_KEY_PAIR)) {
            final SSHKeyPair keyPair = SSHKeyPairUtil.generateRSAKeyPair();
            tokenData.put(OdpTokenConstants.SSH_PRIV_KEY, keyPair.getPrivateKey());
            tokenData.put(OdpTokenConstants.SSH_PUB_KEY, keyPair.getPublicKey());
        }
        return tokenData;
    }

    /**
     * Operation to logout the tokenId and delete the ODP token authentication data from the storage.
     *
     * @param tokenName - The token name to delete the token authentication data for.
     * @throws TokenServiceException - If no ODP Token Secret exists for the given name.
     */
    public void deleteOdpAuthData(final String tokenName) throws TokenServiceException {
        LOGGER.info("Deleting ODP Token: {}.", tokenName);

        final Secret secretToDelete = k8SSecretManager.getSecretByName(tokenName);
        if (secretToDelete == null) {
            throw new TokenServiceException("No ODP Token exists with [" + tokenName + "] token name.");
        }
        final String tokenId = secretToDelete.getData().get(OdpTokenConstants.ODP_TOKEN_DATA_KEY);
        boolean logoutSuccess = false;
        if (tokenId != null) {
            logoutSuccess = clientRequestService.logoutUser(Base64Util.decode(tokenId));
            LOGGER.debug("Token ID logout status: {}.", logoutSuccess);
        }

        if (tokenId == null || logoutSuccess) {
            k8SSecretManager.deleteSecretByName(tokenName);
        }
    }

    /**
     * Operation to clean up the ODP tokens having their TTL expired.
     *
     * @param cleanupTargetTimestamp - Cleanup target timestamp the tokens TTL should be checked against.
     * @return The count of cleaned up token storages
     * @throws TokenServiceException - If an exception occurs during the cleanup.
     */
    public int cleanupExpiredTokens(final Instant cleanupTargetTimestamp) throws TokenServiceException {
        final List<Secret> allODPTokenSecrets = k8SSecretManager.getOdpTokenServiceLabeledSecrets();
        int cleanupCount = 0;
        for (final Secret secret : allODPTokenSecrets) {
            if (hasODPTokenTTLExpired(secret, cleanupTargetTimestamp)) {
                final String secretName = secret.getMetadata().getName();
                try {
                    deleteOdpAuthData(secretName);
                    cleanupCount++;
                } catch (final TokenServiceException e) {
                    LOGGER.error("Failure deleting the {} token due to: {}", secretName, e.getMessage());
                }
            }
        }
        return cleanupCount;
    }

    private boolean hasODPTokenTTLExpired(final Secret secret, final Instant cleanupTargetTimestamp) {
        final Instant secretCreationTimestamp = Instant.parse(secret.getMetadata().getCreationTimestamp());
        return secretCreationTimestamp.compareTo(cleanupTargetTimestamp) < 1;
    }
}