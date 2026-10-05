package org.mifos.connector.notification.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Where the connector half reaches the gateway half. Since the two were merged this is a loopback
 * call inside the same process, which is why application.yml sets the host to 127.0.0.1 and the
 * deployment sets MESSAGEGATEWAYCONFIG_HOST to the same.
 */
@Validated
@ConfigurationProperties(prefix = "messagegatewayconfig")
public record MessageGatewayConfigProperties(@NotNull String host, @NotNull String protocol, @NotNull Integer port) {}
