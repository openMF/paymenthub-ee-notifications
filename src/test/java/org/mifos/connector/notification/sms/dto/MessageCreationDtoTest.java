package org.mifos.connector.notification.sms.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mifos.connector.notification.camel.config.CamelProperties.DATE;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MessageCreationDtoTest {

    private static final long ORIGIN_MILLIS = 1_700_000_000_000L;

    private final MessageCreationDto dto = new MessageCreationDto();

    MessageCreationDtoTest() {
        ReflectionTestUtils.setField(dto, "camelContext", new DefaultCamelContext());
    }

    private static Map<String, Object> variables(Object originDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("transactionId", "tx-1");
        variables.put("accountId", "000000001");
        variables.put("amount", "10");
        if (originDate != null) {
            variables.put("originDate", originDate);
        }
        return variables;
    }

    @Test
    void aLongOriginDateBecomesTheMessageDate() {
        Exchange exchange = dto.setPropertiesForMessage(variables(ORIGIN_MILLIS));
        assertThat(exchange.getProperty(DATE)).isEqualTo(Instant.ofEpochMilli(ORIGIN_MILLIS).atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Test
    void anIntegerOriginDateIsAccepted() {
        Exchange exchange = dto.setPropertiesForMessage(variables(86_400_000));
        assertThat(exchange.getProperty(DATE)).isEqualTo(Instant.ofEpochMilli(86_400_000).atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Test
    void aMissingOriginDateFallsBackToToday() {
        Exchange exchange = dto.setPropertiesForMessage(variables(null));
        assertThat(exchange.getProperty(DATE)).isEqualTo(LocalDate.now(ZoneId.systemDefault()));
        assertThat(exchange.getProperty("correlationId")).isEqualTo("tx-1");
    }
}
