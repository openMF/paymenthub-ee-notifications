package org.fineract.messagegateway.provider.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("messageGatewayProviderConfig")
public class ProviderConfig {
    @Value("${provider.id}")
    private Long id;


    // Named explicitly: the connector half declares a bean method with the same
    // name, and in one application context the two would clash.
    @Bean("messageGatewayProviderId")
    @ConditionalOnProperty(
            value="provider.enabled",
            havingValue = "true")
    public Long getProviderConfig(){
        return id;
    }

}
