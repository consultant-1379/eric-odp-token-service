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

package com.ericsson.oss.odp.tokenservice.manager;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;
import com.ericsson.oss.odp.tokenservice.service.TokenNameService;
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams;
import com.ericsson.oss.odp.tokenservice.util.TokenTypeUtil;
import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants;

/**
 * Class to manage k8s Secret objects.
 */
@Singleton
public class K8sSecretManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sSecretManager.class);
    private static final Map<String, String> SECRET_METADATA_LABELS = buildKeyValueMetadata(
            OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL, OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE
    );

    @Inject
    TokenNameService tokenNameService;

    @Inject
    KubernetesClient kubernetesClient;

    /**
     * Creates a Secret to store the provided data.
     *
     * @param tokenTypes - The types of the token data.
     * @param tokenData - The Token data to be stored.
     * @return The name of the Secret created to store the provided data.
     */
    public String createSecret(final List<ODPTokenGenerateParams.TokenType> tokenTypes,
                        final Map<String, String> tokenData) throws TokenServiceException {
        final Secret newSecret = buildSecret(tokenTypes, tokenData);
        LOGGER.debug("New ODP Token Secret to be created with the following content: {}.", newSecret);
        try {
            final Secret createdSecret = kubernetesClient.secrets().resource(newSecret).create();
            final String createSecretName = createdSecret.getMetadata().getName();
            LOGGER.info("ODP Token Secret successfully created with {} name.", createSecretName);
            return createSecretName;
        } catch (Exception e) {
            LOGGER.error("Create Secret request finished with an error: {}", e.getMessage(), e);
            throw new TokenServiceException("Failure creating new Secret.");
        }
    }

    private Secret buildSecret(final List<ODPTokenGenerateParams.TokenType> tokenTypes, final Map<String, String> data) {
        final String secretName = tokenNameService.getNewTokenName();
        final Map<String, String> annotations = buildKeyValueMetadata(
                OdpTokenConstants.ODP_TOKEN_TYPES_ANNOTATION, TokenTypeUtil.tokenTypesToStringList(tokenTypes)
        );

        return new SecretBuilder().
                withNewMetadata()
                .withName(secretName)
                .withLabels(SECRET_METADATA_LABELS)
                .withAnnotations(annotations)
                .endMetadata()
                .addToData(data)
                .build();
    }

    private static Map<String, String> buildKeyValueMetadata(final String key, final String value) {
        final Map<String, String> labels = new HashMap<>(2);
        labels.put(key, value);
        return labels;
    }

    /**
     * Fetches a k8s Secret by its name.
     *
     * @param tokenName - The name of the Secret to be fetched.
     * @return - The Secret object matching the provided token name.
     */
    public Secret getSecretByName(final String tokenName) throws TokenServiceException {
        try {
            final Secret fetchedSecret = kubernetesClient.secrets().withName(tokenName).get();
            LOGGER.debug("Fetched secret by {} token name {}.", tokenName, fetchedSecret);
            return fetchedSecret;
        } catch (Exception e) {
            LOGGER.error("Get Secret by name request finished with an error: {}", e.getMessage(), e);
            throw new TokenServiceException("Failure getting Secret by name.");
        }
    }

    /**
     * Fetches odp-token-service labeled k8s Secrets.
     *
     * @return List of odp-token-service labeled Secrets
     */
    public List<Secret> getOdpTokenServiceLabeledSecrets() throws TokenServiceException {
        try {
            final SecretList fetchedSecretsByLabel = kubernetesClient.secrets().withLabel(
                    OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL,
                    OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE
            ).list();
            LOGGER.debug("Found {} \"{}:{}\" labeled Secrets.", fetchedSecretsByLabel.getItems().size(),
                    OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL, OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE);
            return fetchedSecretsByLabel.getItems();
        } catch (Exception e) {
            LOGGER.error("Get {} labeled Secrets finished with an error: {}",
                    OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE,  e.getMessage(), e);
            throw new TokenServiceException("Failure getting " + OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE + " labeled Secrets.");
        }
    }

    /**
     * Deletes a k8s Secret by its name.
     *
     * @param tokenName - The name of the Secret to be deleted.
     */
    public void deleteSecretByName(final String tokenName) throws TokenServiceException {
        try {
            List<StatusDetails> deletedSecretStatus = kubernetesClient.secrets().withName(tokenName).delete();
            if (deletedSecretStatus.isEmpty()) {
                LOGGER.warn("No ODP Token exists with {} token name to be deleted.", tokenName);
                return;
            }
            LOGGER.debug("Secret successfully deleted by token name: {}.", tokenName);
        } catch (Exception e) {
            LOGGER.error("Delete Secret by name request finished with an error: {}", e.getMessage(), e);
            throw new TokenServiceException("Failure deleting Secret by name.");
        }
    }
}
