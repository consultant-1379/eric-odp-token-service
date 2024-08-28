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

package com.ericsson.oss.odp.tokenservice.manager

import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretList
import io.fabric8.kubernetes.api.model.StatusDetails
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import spock.lang.Specification

import com.ericsson.oss.odp.tokenservice.exception.TokenServiceException
import com.ericsson.oss.odp.tokenservice.rest.model.ODPTokenGenerateParams
import com.ericsson.oss.odp.tokenservice.service.TokenNameService
import com.ericsson.oss.odp.tokenservice.util.OdpTokenConstants

class K8sSecretManagerSpec extends Specification {

    def SECRET_NAME = "odptoken-secret-20240509125212-1"
    K8sSecretManager k8sSecretManager = new K8sSecretManager()

    def "Create new Secret"() {
        given: "A list of token types and token data"
        def base64OdpToken = "Z3AxS2x0aXZXSlRDQ3ZIbWVkUWtIYVpzS3V3LipBQUpUU1FBQ01ERUFBbE5MQUJ4TVdWaGtjMkl3ZVhrd1kzQlphVVpIVVZWWlN6bGhhVVZNUXpROUFBUjBlWEJsQUFORFZGTUFBbE14QUFBLioK"
        def tokenTypes = [ODPTokenGenerateParams.TokenType.SSO]
        def tokenData = [(OdpTokenConstants.ODP_TOKEN_DATA_KEY): base64OdpToken]

        k8sSecretManager.tokenNameService = Stub(TokenNameService) {
            getNewTokenName() >> SECRET_NAME
        }
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> Stub(MixedOperation) {
                resource(_) >> Stub(Resource) {
                    create() >> Stub(Secret) {
                        getMetadata() >> Stub(ObjectMeta) {
                            getName() >> SECRET_NAME
                        }
                    }
                }
            }
        }

        when: "A secret is created to store the passed data as params"
        def createdSecretName = k8sSecretManager.createSecret(tokenTypes, tokenData)

        then: "The Secret is created with the expected name"
        createdSecretName == SECRET_NAME
    }

    def "Failure to create new Secret"() {
        given: "An exception is thrown while creating a Secret"
        k8sSecretManager.tokenNameService = Stub(TokenNameService)
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> {throw new KubernetesClientException("namespace not specified for an operation requiring one and no default was found in the Config.")}
        }

        when: "A secret is created to store the passed data as params"
        k8sSecretManager.createSecret(Collections.emptyList(), Collections.emptyMap())

        then: "Failure creating new Secret"
        def e = thrown(TokenServiceException)
        e.getMessage() == "Failure creating new Secret."
    }

    def "Get Secret by name"() {
        given: "A Secret with a particular name exists"
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> Stub(MixedOperation) {
                withName(_ as String) >> Stub(Resource) {
                    get() >> Stub(Secret) {
                        getMetadata() >> Stub(ObjectMeta) {
                            getName() >> SECRET_NAME
                        }
                    }
                }
            }
        }

        when: "Secret is fetched by name"
        def fetchedSecret = k8sSecretManager.getSecretByName(SECRET_NAME)

        then: "The matching name Secret is returned"
        fetchedSecret.getMetadata().getName() == SECRET_NAME
    }

    def "Failure to get Secret by name"() {
        given: "An exception is thrown while fetching a Secret"
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> {throw new KubernetesClientException("namespace not specified for an operation requiring one and no default was found in the Config.")}
        }

        when: "A secret is fetched by name"
        k8sSecretManager.getSecretByName(SECRET_NAME)

        then: "Failure fetching the Secret"
        def e = thrown(TokenServiceException)
        e.getMessage() == "Failure getting Secret by name."
    }

    def "Get Secrets by odp-token-service label"() {
        given: "Two odp-token-service labeled Secrets exist"
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> Stub(MixedOperation) {
                withLabel(OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL, OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE) >> Stub(FilterWatchListDeletable) {
                    list() >> Stub(SecretList) {
                        getItems() >> createTwoLabeledSecrets()
                    }
                }
            }
        }

        when: "Secret is fetched by odp-token-service label"
        def fetchedSecrets = k8sSecretManager.getOdpTokenServiceLabeledSecrets()

        then: "A list of two odp-token-service labeled Secrets is returned"
        fetchedSecrets.size() == 2
        fetchedSecrets.get(0).getMetadata().getLabels()
                .get(OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL) == OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE
        fetchedSecrets.get(1).getMetadata().getLabels()
                .get(OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL) == OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE
    }

    def "Failure to get Secrets by odp-token-service label"() {
        given: "An exception is thrown while fetching the Secrets by label"
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> {throw new KubernetesClientException("namespace not specified for an operation requiring one and no default was found in the Config.")}
        }

        when: "Secret is fetched by odp-token-service label"
        k8sSecretManager.getOdpTokenServiceLabeledSecrets()

        then: "Failure fetching the odp-token-service labeled Secrets"
        def e = thrown(TokenServiceException)
        e.getMessage() == "Failure getting " + OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE + " labeled Secrets."
    }

    def "Delete Secret by name"() {
        given: "A Secret with a particular name exists"
        def statusDetails = Spy(ArrayList)
        statusDetails.add(new StatusDetails())
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> Stub(MixedOperation) {
                withName(_ as String) >> Stub(Resource) {
                    delete() >> statusDetails
                }
            }
        }

        when: "Secret is deleted by name"
        k8sSecretManager.deleteSecretByName(SECRET_NAME)

        then: "Deleted Secret status details are returned"
        1 * statusDetails.isEmpty()
        !statusDetails.isEmpty()
    }

    def "No Secret to be deleted by name"() {
        given: "No Secret exists for a given name"
        def statusDetails = Spy(ArrayList)
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> Stub(MixedOperation) {
                withName(_ as String) >> Stub(Resource) {
                    delete() >> statusDetails
                }
            }
        }

        when: "Secret is deleted by name"
        k8sSecretManager.deleteSecretByName(SECRET_NAME)

        then: "No deleted Secret status details are returned"
        1 * statusDetails.isEmpty()
        statusDetails.isEmpty()
    }

    def "Failure to delete Secret by name"() {
        given: "An exception is thrown while deleting a Secret"
        k8sSecretManager.kubernetesClient = Stub(KubernetesClient) {
            secrets() >> {throw new KubernetesClientException("namespace not specified for an operation requiring one and no default was found in the Config.")}
        }

        when: "A secret is deleted by name"
        k8sSecretManager.deleteSecretByName(SECRET_NAME)

        then: "Failure deleting the Secret"
        def e = thrown(TokenServiceException)
        e.getMessage() == "Failure deleting Secret by name."
    }

    List<Secret> createTwoLabeledSecrets() {
        def labels = [(OdpTokenConstants.ODP_TOKEN_CREATED_BY_LABEL): OdpTokenConstants.ODP_TOKEN_CREATED_BY_SERVICE]
        def secretList = []
        1.upto(2, {
            Secret secret = new Secret()
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName("odptoken-secret-20240509125212-" + it)
            objectMeta.setLabels(labels)
            secret.setMetadata(objectMeta)
            secretList << secret
        })
        secretList
    }
}