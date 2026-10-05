package org.mifos.connector.notification.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** The host this service advertises as its own. The deployment sets HOSTCONFIG_HOST. */
@Validated
@ConfigurationProperties(prefix = "hostconfig")
public record HostConfigProperties(@NotNull String host, @NotNull String protocol) {}
