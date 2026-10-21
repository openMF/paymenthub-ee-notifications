package org.fineract.messagegateway.zeebe;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import java.util.HashMap;
import java.util.Map;
import org.fineract.messagegateway.provider.config.ProviderConfig;
import org.fineract.messagegateway.sms.api.SmsApiResource;
import org.junit.jupiter.api.Test;
import org.mifos.connector.notification.config.properties.OperationsConfigProperties;
import org.mifos.connector.notification.config.properties.ZeebeProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class ZeebeWorkersSendMessageTest {

    private final SmsApiResource smsApiResource = mock(SmsApiResource.class);
    private final JobClient client = mock(JobClient.class, RETURNS_DEEP_STUBS);
    private final ActivatedJob job = mock(ActivatedJob.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final ZeebeWorkers workers = new ZeebeWorkers(
            new ZeebeProperties(new ZeebeProperties.Broker("zeebe:26500"), new ZeebeProperties.Client(10, 30000),
                    new ZeebeProperties.Worker("PT30S", 3)),
            new OperationsConfigProperties("Platform-TenantId", "default", "Platform-TenantAppKey", "app-key"));

    ZeebeWorkersSendMessageTest() {
        ProviderConfig providerConfig = mock(ProviderConfig.class);
        when(providerConfig.getProviderConfig()).thenReturn(2L);
        ReflectionTestUtils.setField(workers, "smsApiResource", smsApiResource);
        ReflectionTestUtils.setField(workers, "providerConfig", providerConfig);
        when(job.getKey()).thenReturn(42L);
        when(job.getProcessInstanceKey()).thenReturn(7L);
        when(job.getRetries()).thenReturn(3);
        when(job.getVariablesAsMap()).thenReturn(variables);
        variables.put("phoneNumber", "+10000000000");
        variables.put("deliveryMessage", "hello");
    }

    @Test
    void aQueuedMessageCompletesTheJob() {
        when(smsApiResource.sendShortMessagesToProvider(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        workers.sendMessage(client, job);

        verify(client).newCompleteCommand(42L);
    }

    @Test
    void aMissingPhoneNumberFailsTheJobWithoutRetries() {
        variables.remove("phoneNumber");

        workers.sendMessage(client, job);

        verify(client.newFailCommand(42L)).retries(0);
        verify(client.newFailCommand(42L).retries(0)).errorMessage("Missing job variable phoneNumber");
        verify(client, never()).newCompleteCommand(anyLong());
        verify(smsApiResource, never()).sendShortMessagesToProvider(anyString(), anyString(), anyString(), any());
    }

    @Test
    void aRejectedSendFailsTheJob() {
        when(smsApiResource.sendShortMessagesToProvider(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));

        workers.sendMessage(client, job);

        verify(client.newFailCommand(42L)).retries(2);
        verify(client, never()).newCompleteCommand(anyLong());
    }
}
