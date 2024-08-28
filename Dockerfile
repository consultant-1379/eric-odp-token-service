#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG CBOS_IMAGE_TAG
ARG CBOS_IMAGE_REPO
ARG CBOS_IMAGE_NAME

FROM ${CBOS_IMAGE_REPO}/${CBOS_IMAGE_NAME}:${CBOS_IMAGE_TAG}
ARG CBOS_IMAGE_TAG
ARG CBOS_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_IMAGE_TAG}

RUN zypper ar -C -G -f $CBOS_REPO_URL?ssl_verify=no \
    COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y java-11-openjdk-headless \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

ARG JAR_FILE
ADD odp-token-service-war/target/${JAR_FILE} eric-odp-token-service-app.jar

ARG ERIC_ODP_TOKEN_SERVICE_UID=40514
ARG ERIC_ODP_TOKEN_SERVICE_GID=40514
ENV JBOSS_BIND_ADDRESS="0.0.0.0"

RUN echo "${ERIC_ODP_TOKEN_SERVICE_UID}:x:${ERIC_ODP_TOKEN_SERVICE_UID}:${ERIC_ODP_TOKEN_SERVICE_GID}:eric-odp-token-service-user:/:/bin/bash" >> /etc/passwd && \
    cat /etc/passwd && \
    sed -i "s|root:/bin/bash|root:/bin/false|g" /etc/passwd

USER $ERIC_ODP_TOKEN_SERVICE_UID:$ERIC_ODP_TOKEN_SERVICE_GID

CMD ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar eric-odp-token-service-app.jar --install-dir=${APP_DEPLOYMENT_DIR} -b=${JBOSS_BIND_ADDRESS}"]

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER
LABEL \
    org.opencontainers.image.title=eric-odp-token-service \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"