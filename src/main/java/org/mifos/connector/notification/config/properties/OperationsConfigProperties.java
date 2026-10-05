package org.mifos.connector.notification.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The tenant header names and values the gateway authenticates its own calls with. Before this record
 * existed, these four keys were read through ten {@code @Value} fields in three different classes.
 */
@Validated
@ConfigurationProperties(prefix = "operationsconfig")
public record OperationsConfigProperties(@NotNull String tenantid, @NotNull String tenantidvalue, @NotNull String tenantappkey,
        @NotNull String tenantappvalue) {}
