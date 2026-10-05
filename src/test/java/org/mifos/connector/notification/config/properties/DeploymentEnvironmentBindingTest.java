package org.mifos.connector.notification.config.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * These bind the variables the running gazelle message-gateway deployment sets, with the same names
 * and values. Two of them are not what the CRs write: the deployment sets CALLBACKCONFIG_HOST and
 * MESSAGEGATEWAYCONFIG_HOST to 127.0.0.1. If a rename ever creeps in, the build says so instead of
 * a deployment going quiet.
 */
class DeploymentEnvironmentBindingTest {

    private static Binder deploymentEnvironment() {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("ZEEBE_BROKER_CONTACTPOINT", "paymenthub-infra-zeebe-gateway:26500");
        variables.put("CALLBACKCONFIG_HOST", "127.0.0.1");
        variables.put("HOSTCONFIG_HOST", "message-gateway");
        variables.put("MESSAGEGATEWAYCONFIG_HOST", "127.0.0.1");

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, variables));
        ConfigurationPropertySources.attach(environment);
        return Binder.get(environment);
    }

    private static Binder propertiesOf(String... pairs) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            properties.put(pairs[i], pairs[i + 1]);
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        ConfigurationPropertySources.attach(environment);
        return Binder.get(environment);
    }

    @Test
    void bindsTheHostsTheDeploymentSets() {
        Binder deployment = deploymentEnvironment();

        assertEquals("paymenthub-infra-zeebe-gateway:26500",
                deployment.bind("zeebe", ZeebeProperties.class).get().broker().contactpoint());
        assertEquals("127.0.0.1", deployment.bind("callbackconfig", CallbackConfigProperties.class).get().host());
        assertEquals("message-gateway", deployment.bind("hostconfig", HostConfigProperties.class).get().host());
        assertEquals("127.0.0.1", deployment.bind("messagegatewayconfig", MessageGatewayConfigProperties.class).get().host());
    }

    @Test
    void keepsTheUnderscoreInTheVelocityFailureTypeKeys() {
        // velocity.failure_type and velocity.defaults.failure_type are the names in
        // application.yml and the names the templates use
        VelocityProperties velocity = propertiesOf("velocity.failure_type", "failType", "velocity.defaults.failure_type",
                "Reason not available").bind("velocity", VelocityProperties.class).get();

        assertEquals("failType", velocity.failureType());
        assertEquals("Reason not available", velocity.defaults().failureType());
    }

    @Test
    void bindsTheOperationsTenantHeaders() {
        OperationsConfigProperties operations = propertiesOf("operationsconfig.tenantid", "Fineract-Platform-TenantId",
                "operationsconfig.tenantidvalue", "default", "operationsconfig.tenantappkey", "Fineract-Tenant-App-Key",
                "operationsconfig.tenantappvalue", "123456543234abdkdkdkd").bind("operationsconfig", OperationsConfigProperties.class)
                .get();

        assertEquals("Fineract-Platform-TenantId", operations.tenantid());
        assertEquals("Fineract-Tenant-App-Key", operations.tenantappkey());
        assertEquals("123456543234abdkdkdkd", operations.tenantappvalue());
    }
}
