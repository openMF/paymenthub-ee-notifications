package org.mifos.connector.notification.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The Zeebe gateway and the worker settings, read in five classes before this record existed -
 * zeebe.client.ttl alone was a @Value in four of them.
 *
 * <p>
 * zeebe.client.evenly-allocated-max-jobs is deliberately not here. Its value in application.yml is a
 * SpEL expression over the other two client settings, and only @Value evaluates SpEL, so it stays
 * where it is.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "zeebe")
public record ZeebeProperties(@NotNull @Valid Broker broker, @NotNull @Valid Client client, @NotNull @Valid Worker worker) {

    public record Broker(@NotNull String contactpoint) {}

    public record Client(@NotNull Integer maxExecutionThreads, @NotNull Integer ttl) {}

    /** timer is an ISO-8601 duration handed to Zeebe as a job variable, so it stays a string. */
    public record Worker(@NotNull String timer, @NotNull Integer retries) {}
}
