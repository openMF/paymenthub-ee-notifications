package org.mifos.connector.notification.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The names of the placeholders in the Velocity templates, and the text to use when a value is
 * missing. Thirteen @Value fields in one class before this record existed.
 *
 * <p>
 * failure_type keeps its underscore, because that is the key in application.yml and the name the
 * templates use; relaxed binding matches it to failureType.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "velocity")
public record VelocityProperties(@NotNull String transactionid, @NotNull String amount, @NotNull String date,
        @NotNull String account, @NotNull String failureType, @NotNull String txnType, @NotNull String currency,
        @NotNull @Valid Defaults defaults) {

    public record Defaults(@NotNull String transactionid, @NotNull String amount, @NotNull String account,
            @NotNull String failureType, @NotNull String txnType, @NotNull String currency) {}
}
