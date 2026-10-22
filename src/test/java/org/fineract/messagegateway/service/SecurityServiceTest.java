package org.fineract.messagegateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.fineract.messagegateway.exception.SecurityException;
import org.fineract.messagegateway.tenants.domain.Tenant;
import org.fineract.messagegateway.tenants.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

class SecurityServiceTest {

    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final SecurityService securityService = new SecurityService();

    SecurityServiceTest() {
        ReflectionTestUtils.setField(securityService, "tenantRepository", tenantRepository);
    }

    @Test
    void aNewTenantGetsAnApiKey() {
        assertThat(securityService.generateApiKey("newtenant")).isNotBlank();
    }

    @Test
    void anExistingTenantIsRefused() {
        when(tenantRepository.findByTenantId("greenbank")).thenReturn(mock(Tenant.class));

        assertThatThrownBy(() -> securityService.generateApiKey("greenbank")).isInstanceOf(SecurityException.class)
                .hasMessageContaining("greenbank");
        assertThat(AnnotationUtils.findAnnotation(SecurityException.class, ResponseStatus.class).value()).isEqualTo(HttpStatus.CONFLICT);
    }
}
