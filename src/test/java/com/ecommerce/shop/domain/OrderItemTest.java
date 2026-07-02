package com.ecommerce.shop.domain;

import static com.ecommerce.shop.domain.OrderItemTestSamples.*;
import static com.ecommerce.shop.domain.ProductTestSamples.*;
import static com.ecommerce.shop.domain.ShopOrderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.shop.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderItem.class);
        OrderItem orderItem1 = getOrderItemSample1();
        OrderItem orderItem2 = new OrderItem();
        assertThat(orderItem1).isNotEqualTo(orderItem2);

        orderItem2.setId(orderItem1.getId());
        assertThat(orderItem1).isEqualTo(orderItem2);

        orderItem2 = getOrderItemSample2();
        assertThat(orderItem1).isNotEqualTo(orderItem2);
    }

    @Test
    void orderTest() {
        OrderItem orderItem = getOrderItemRandomSampleGenerator();
        ShopOrder shopOrderBack = getShopOrderRandomSampleGenerator();

        orderItem.setOrder(shopOrderBack);
        assertThat(orderItem.getOrder()).isEqualTo(shopOrderBack);

        orderItem.order(null);
        assertThat(orderItem.getOrder()).isNull();
    }

    @Test
    void productTest() {
        OrderItem orderItem = getOrderItemRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        orderItem.setProduct(productBack);
        assertThat(orderItem.getProduct()).isEqualTo(productBack);

        orderItem.product(null);
        assertThat(orderItem.getProduct()).isNull();
    }
}
