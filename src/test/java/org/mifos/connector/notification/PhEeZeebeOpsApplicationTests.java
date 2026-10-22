package org.mifos.connector.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhEeZeebeOpsApplicationTests {

    @Autowired
    private CamelContext camelContext;

    @Test
    void contextLoads() {
    }

    // The workers send to these endpoints. With an asynchronous event multicaster Camel
    // started with no routes, so every notification was dropped without a trace.
    @Test
    void theNotificationRoutesAreStarted() {
        assertThat(camelContext.getRoutes()).extracting(route -> route.getEndpoint().getEndpointUri())
                .contains("direct://create-success-messages", "direct://create-failure-messages", "direct://send-notifications");
        assertThat(camelContext.getRouteController().getRouteStatus(
                camelContext.getRoutes().stream().filter(r -> r.getEndpoint().getEndpointUri().equals("direct://send-notifications"))
                        .map(Route::getRouteId).findFirst().orElseThrow()).isStarted()).isTrue();
    }

}
