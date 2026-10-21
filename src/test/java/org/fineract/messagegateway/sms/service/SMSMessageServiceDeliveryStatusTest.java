package org.fineract.messagegateway.sms.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.fineract.messagegateway.tenants.domain.Tenant;
import org.fineract.messagegateway.tenants.service.TenantsService;
import org.junit.jupiter.api.Test;
import org.mifos.connector.notification.NotificationsConnectorApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = NotificationsConnectorApplication.class)
@ActiveProfiles("test")
class SMSMessageServiceDeliveryStatusTest {

    @Autowired
    private SMSMessageService smsMessageService;

    @Autowired
    private TenantsService tenantsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void anEmptyIdListReturnsNothingInsteadOfInvalidSql() {
        String appKey = tenantsService.createTenant(new Tenant("deliverytest1", null, "test"));

        assertThat(smsMessageService.getDeliveryStatus("deliverytest1", appKey, List.of())).isEmpty();
    }

    @Test
    void theStatusesOfTheRequestedMessagesAreReturned() {
        String appKey = tenantsService.createTenant(new Tenant("deliverytest2", null, "test"));
        Long tenantPk = jdbcTemplate.queryForObject("select id from m_tenants where tenant_id = 'deliverytest2'", Long.class);
        for (long internalId : new long[] { 101, 102, 103 }) {
            jdbcTemplate.update("insert into m_outbound_messages (tenant_id, internal_id, external_id, delivery_status, mobile_number, message,"
                    + " sms_bridge_id, submitted_on_date, type) values (?, ?, ?, 100, '+10000000000', 'hi', 1, current_timestamp, 'SMS')",
                    tenantPk, internalId, "ext-" + internalId);
        }

        assertThat(smsMessageService.getDeliveryStatus("deliverytest2", appKey, List.of(101L, 103L)))
                .extracting(status -> status.getId()).containsExactlyInAnyOrder("101", "103");
    }
}
