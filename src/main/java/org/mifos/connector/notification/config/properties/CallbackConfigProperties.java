package org.mifos.connector.notification.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Where the delivery-status callback is posted. The deployment sets CALLBACKCONFIG_HOST. */
@Validated
@ConfigurationProperties(prefix = "callbackconfig")
public record CallbackConfigProperties(@NotNull String host, @NotNull String protocol, @NotNull Integer port) {}
