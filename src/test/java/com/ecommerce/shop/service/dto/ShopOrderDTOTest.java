package com.ecommerce.shop.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.shop.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShopOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShopOrderDTO.class);
        ShopOrderDTO shopOrderDTO1 = new ShopOrderDTO();
        shopOrderDTO1.setId(1L);
        ShopOrderDTO shopOrderDTO2 = new ShopOrderDTO();
        assertThat(shopOrderDTO1).isNotEqualTo(shopOrderDTO2);
        shopOrderDTO2.setId(shopOrderDTO1.getId());
        assertThat(shopOrderDTO1).isEqualTo(shopOrderDTO2);
        shopOrderDTO2.setId(2L);
        assertThat(shopOrderDTO1).isNotEqualTo(shopOrderDTO2);
        shopOrderDTO1.setId(null);
        assertThat(shopOrderDTO1).isNotEqualTo(shopOrderDTO2);
    }
}
