package org.mifos.connector.notification.sms.dto;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.TRANSACTION_ID;

@Component
public class MessageCreationDto {

    private static final Logger logger = LoggerFactory.getLogger(MessageCreationDto.class);

    @Autowired
    private CamelContext camelContext;

    public Exchange setPropertiesForMessage(Map<String, Object> variables){
        String transactionId = (String) variables.get(TRANSACTION_ID);
        String account = (String) variables.get(ACCOUNT);
        LocalDate localDate = originDate(variables.get(ORIGIN_DATE));
        String amount = (String) variables.get(AMOUNT);
        Exchange exchange = new DefaultExchange(camelContext);

        if(variables.containsKey(SERVER_TRANSACTION_RECEIPT_NUMBER)){
            exchange.setProperty(CORRELATION_ID, variables.get(SERVER_TRANSACTION_RECEIPT_NUMBER).toString());
        }
        else {
            exchange.setProperty(CORRELATION_ID, transactionId);
        }
        exchange.setProperty(DATE, localDate);
        exchange.setProperty(ACCOUNT_ID,account);
        exchange.setProperty(TRANSACTION_AMOUNT,amount);
        return exchange;
    }

    // Zeebe hands a whole number back as Integer or Long depending on its size, and the
    // variable may be absent; either used to throw inside the job handler, before the
    // job was completed or failed, so the notification stopped with nothing logged.
    private static LocalDate originDate(Object value) {
        if (value instanceof Number epochMillis) {
            return Instant.ofEpochMilli(epochMillis.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        logger.warn("Job variable {} is {}; using today's date in the notification", ORIGIN_DATE,
                value == null ? "missing" : "not a number (" + value + ")");
        return LocalDate.now(ZoneId.systemDefault());
    }
}
