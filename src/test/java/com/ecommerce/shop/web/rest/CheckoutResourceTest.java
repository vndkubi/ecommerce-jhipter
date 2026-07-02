package com.ecommerce.shop.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.shop.service.CheckoutService;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link CheckoutResource}.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutResourceTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutResource checkoutResource;

    @Test
    void checkoutDelegatesToServiceAndReturnsOrder() {
        ShopOrderDTO dto = new ShopOrderDTO();
        dto.setId(7L);
        when(checkoutService.checkout(7L)).thenReturn(dto);

        ResponseEntity<ShopOrderDTO> response = checkoutResource.checkoutOrder(7L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(7L);
    }
}
