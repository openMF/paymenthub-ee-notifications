package org.mifos.connector.notification.zeebe;


import io.camunda.zeebe.client.ZeebeClient;
import org.mifos.connector.notification.config.properties.ZeebeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeClientConfiguration {

    private final ZeebeProperties zeebeProperties;

    public ZeebeClientConfiguration(ZeebeProperties zeebeProperties) {
        this.zeebeProperties = zeebeProperties;
    }


    @Bean
    public ZeebeClient setup() {
        return ZeebeClient.newClientBuilder()
                .gatewayAddress(zeebeProperties.broker().contactpoint())
                .usePlaintext()
                .numJobWorkerExecutionThreads(zeebeProperties.client().maxExecutionThreads())
                .build();
    }
}
