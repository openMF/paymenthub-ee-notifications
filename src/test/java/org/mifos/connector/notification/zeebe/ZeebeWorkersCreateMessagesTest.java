package org.mifos.connector.notification.zeebe;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.mifos.connector.notification.sms.dto.MessageCreationDto;
import org.springframework.test.util.ReflectionTestUtils;

class ZeebeWorkersCreateMessagesTest {

    private final ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
    private final MessageCreationDto messageCreationDto = mock(MessageCreationDto.class);
    private final JobClient client = mock(JobClient.class, RETURNS_DEEP_STUBS);
    private final ActivatedJob job = mock(ActivatedJob.class);
    private final Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    private final ZeebeWorkers workers = new ZeebeWorkers();

    ZeebeWorkersCreateMessagesTest() {
        ReflectionTestUtils.setField(workers, "producerTemplate", producerTemplate);
        ReflectionTestUtils.setField(workers, "messageCreationDto", messageCreationDto);
        Map<String, Object> variables = new HashMap<>();
        when(job.getKey()).thenReturn(42L);
        when(job.getRetries()).thenReturn(3);
        when(job.getVariablesAsMap()).thenReturn(variables);
        when(messageCreationDto.setPropertiesForMessage(variables)).thenReturn(exchange);
    }

    @Test
    void aWorkingRouteCompletesTheJob() {
        workers.createMessages(client, job, "direct:create-success-messages");

        verify(client).newCompleteCommand(42L);
        verify(client, never()).newFailCommand(anyLong());
    }

    @Test
    void aFailedRouteFailsTheJobInsteadOfCompletingIt() {
        when(producerTemplate.send(eq("direct:create-success-messages"), eq(exchange))).thenAnswer(invocation -> {
            exchange.setException(new IllegalStateException("No consumers available on endpoint"));
            return exchange;
        });

        workers.createMessages(client, job, "direct:create-success-messages");

        verify(client, never()).newCompleteCommand(anyLong());
        verify(client.newFailCommand(42L)).retries(2);
        verify(client.newFailCommand(42L).retries(anyInt())).errorMessage(anyString());
    }
}
