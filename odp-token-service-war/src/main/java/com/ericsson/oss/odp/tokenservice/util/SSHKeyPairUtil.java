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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException;
import com.ericsson.oss.odp.tokenservice.model.SSHKeyPair;

/**
 * Class used to generate public and private RSA key pair.
 */
public class SSHKeyPairUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHKeyPairUtil.class);

    static String SSH_RSA_ALG_NAME = "ssh-rsa";
    private static final byte[] SSH_RSA_ALG_BYTES = SSH_RSA_ALG_NAME.getBytes(StandardCharsets.US_ASCII);
    private static final String PRIVATE_KEY_BEGIN_LINE = "-----BEGIN PRIVATE KEY-----\n";
    private static final String PRIVATE_KEY_END_LINE = "\n-----END PRIVATE KEY-----";

    /**
     * Generates public and private RSA key pair.
     *
     * @return An SSHKeyPair encapsulating the generated public and private keys.
     * @throws TokenServiceException If an error occurs while generating the public and private RSA keys.
     */
    public static SSHKeyPair generateRSAKeyPair() throws TokenServiceException {
        final KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            final KeyPair keyPair = generator.generateKeyPair();

            final String privateKey = getBase64EncodedPrivateKey(keyPair);
            final String publicKey = getBase64EncodedPublicKey(keyPair);

            return new SSHKeyPair(privateKey, publicKey);
        } catch (final Exception e) {
            LOGGER.error("A failure has occurred while generating public and private RSA key pair.", e);
            throw new TokenServiceException("Unable to generate public and private RSA key pair.");
        }
    }

    private static String getBase64EncodedPrivateKey(final KeyPair keyPair) {
        final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        final String base64PrivKey = Base64.getMimeEncoder().encodeToString(rsaPrivateKey.getEncoded());
        return Base64Util.encode(PRIVATE_KEY_BEGIN_LINE + base64PrivKey + PRIVATE_KEY_END_LINE);
    }

    private static String getBase64EncodedPublicKey(final KeyPair keyPair) throws IOException {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        final String base64PubKey = Base64.getEncoder().encodeToString(sshPublicKeyEncode(rsaPublicKey));
        return Base64Util.encode(SSH_RSA_ALG_NAME + " " + base64PubKey);
    }

    private static byte[] sshPublicKeyEncode(final RSAPublicKey key) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        write(SSH_RSA_ALG_BYTES, buf);
        write(key.getPublicExponent().toByteArray(), buf);
        write(key.getModulus().toByteArray(), buf);
        return buf.toByteArray();
    }

    private static void write(final byte[] str, final OutputStream outputStream) throws IOException {
        for (int shift = 24; shift >= 0; shift -= 8)
            outputStream.write((str.length >>> shift) & 0xFF);
        outputStream.write(str);
    }
}