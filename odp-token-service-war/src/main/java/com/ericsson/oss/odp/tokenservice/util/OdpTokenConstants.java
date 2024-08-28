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

/**
 * ODP Token related constant values.
 */
public interface OdpTokenConstants {

    String ODP_TOKEN_DATA_KEY = "odptoken";
    String ODP_TOKEN_TYPES_ANNOTATION = "com.ericsson.odp.tokentypes";
    String ODP_TOKEN_CREATED_BY_LABEL = "com.ericsson.odp.created.by";
    String ODP_TOKEN_CREATED_BY_SERVICE = "odp-token-service";
    String SSH_PUB_KEY = "sshpubkey";
    String SSH_PRIV_KEY = "sshprivkey";
    String ODP_TOKEN_KEYSTORE_PATH = "/keystore/odp-token-service.jks";
    String ODP_TOKEN_TRUSTSTORE_PATH = "/truststore/ENM_Management_CA.jks";
}