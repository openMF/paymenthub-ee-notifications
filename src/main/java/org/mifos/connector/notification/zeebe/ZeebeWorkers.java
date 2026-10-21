package org.mifos.connector.notification.zeebe;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.notification.sms.dto.MessageCreationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.TRANSACTION_ID;

@Component
public class ZeebeWorkers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageCreationDto messageCreationDto;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {
        zeebeClient.newWorker()
                .jobType("transaction_failure")
                .handler((client, job) -> createMessages(client, job, "direct:create-failure-messages"))
                .name("transaction_failure")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("transaction_success")
                .handler((client, job) -> createMessages(client, job, "direct:create-success-messages"))
                .name("transaction_success")
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    /**
     * Hands the job to the message route and completes it only if the route worked.
     * producerTemplate.send does not throw: a failure is left on the exchange, and the job
     * used to be completed regardless, telling the workflow a notification went out.
     */
    void createMessages(JobClient client, ActivatedJob job, String endpoint) {
        logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
        Map<String, Object> variables = job.getVariablesAsMap();
        Exchange exchange = messageCreationDto.setPropertiesForMessage(variables);
        exchange.setProperty(INTERNAL_ID, String.valueOf(job.getProcessInstanceKey()));
        exchange.setProperty(TYPE, variables.get(TYPE));
        producerTemplate.send(endpoint, exchange);

        Exception failure = exchange.getException();
        if (failure != null) {
            logger.error("Job '{}' with key {}: {} failed", job.getType(), job.getKey(), endpoint, failure);
            client.newFailCommand(job.getKey())
                    .retries(Math.max(job.getRetries() - 1, 0))
                    .errorMessage(endpoint + " failed: " + failure.getMessage())
                    .send()
                    .join();
            return;
        }
        client.newCompleteCommand(job.getKey())
                .send()
                .join();
    }
}