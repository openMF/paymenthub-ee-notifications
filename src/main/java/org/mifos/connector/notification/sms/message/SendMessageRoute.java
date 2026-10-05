package org.mifos.connector.notification.sms.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.notification.provider.config.ProviderConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.mifos.connector.notification.config.properties.MessageGatewayConfigProperties;
import org.mifos.connector.notification.config.properties.OperationsConfigProperties;
import org.mifos.connector.notification.config.properties.ZeebeProperties;
import org.springframework.stereotype.Component;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class SendMessageRoute extends RouteBuilder {

    private final OperationsConfigProperties operationsConfig;

    private final MessageGatewayConfigProperties messageGatewayConfig;

    private final ZeebeProperties zeebeProperties;

    public SendMessageRoute(OperationsConfigProperties operationsConfig, MessageGatewayConfigProperties messageGatewayConfig,
            ZeebeProperties zeebeProperties) {
        this.operationsConfig = operationsConfig;
        this.messageGatewayConfig = messageGatewayConfig;
        this.zeebeProperties = zeebeProperties;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
        public void configure() throws Exception {


            from("direct:send-notifications")
                    .id("send-notifications")
                    .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty."+PROVIDER_ID+"}")
                    .setHeader(operationsConfig.tenantid(), constant(operationsConfig.tenantidvalue()))
                    .setHeader(operationsConfig.tenantappkey(), constant(operationsConfig.tenantappvalue()))
                    .process(exchange ->{
                        String mobile = exchange.getProperty(MOBILE_NUMBER).toString();
                        Long internalId = Long.parseLong(exchange.getProperty(INTERNAL_ID).toString());
                        int providerId = providerConfig.getProviderConfig();

                        JSONObject request = new JSONObject();
                        JSONArray jArray = new JSONArray();
                        request.put("internalId", internalId);
                        request.put("mobileNumber", mobile);
                        request.put("message",  exchange.getProperty(DELIVERY_MESSAGE));
                        request.put("providerId", providerId);
                        exchange.getIn().setBody(jArray.put(request).toString());
                    })
                    .log("${body}")
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to(String.format("%s://%s/sms/?bridgeEndpoint=true", messageGatewayConfig.protocol(), messageGatewayConfig.host()))
                    .log(LoggingLevel.INFO, "Sending sms to message gateway completed")
                   ;

        }
    }


