package com.ecommerce.shop.domain;

import static com.ecommerce.shop.domain.CustomerTestSamples.*;
import static com.ecommerce.shop.domain.ShopOrderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.shop.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShopOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShopOrder.class);
        ShopOrder shopOrder1 = getShopOrderSample1();
        ShopOrder shopOrder2 = new ShopOrder();
        assertThat(shopOrder1).isNotEqualTo(shopOrder2);

        shopOrder2.setId(shopOrder1.getId());
        assertThat(shopOrder1).isEqualTo(shopOrder2);

        shopOrder2 = getShopOrderSample2();
        assertThat(shopOrder1).isNotEqualTo(shopOrder2);
    }

    @Test
    void customerTest() {
        ShopOrder shopOrder = getShopOrderRandomSampleGenerator();
        Customer customerBack = getCustomerRandomSampleGenerator();

        shopOrder.setCustomer(customerBack);
        assertThat(shopOrder.getCustomer()).isEqualTo(customerBack);

        shopOrder.customer(null);
        assertThat(shopOrder.getCustomer()).isNull();
    }
}
