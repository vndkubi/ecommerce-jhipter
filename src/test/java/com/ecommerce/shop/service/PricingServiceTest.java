package com.ecommerce.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.shop.domain.Customer;
import com.ecommerce.shop.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PricingService}.
 */
class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    void loyaltyDiscountRateUsesTiers() {
        assertThat(pricingService.loyaltyDiscountRate(null)).isEqualByComparingTo("0");
        assertThat(pricingService.loyaltyDiscountRate(0)).isEqualByComparingTo("0");
        assertThat(pricingService.loyaltyDiscountRate(99)).isEqualByComparingTo("0");
        assertThat(pricingService.loyaltyDiscountRate(100)).isEqualByComparingTo("0.05");
        assertThat(pricingService.loyaltyDiscountRate(500)).isEqualByComparingTo("0.10");
        assertThat(pricingService.loyaltyDiscountRate(1000)).isEqualByComparingTo("0.15");
    }

    @Test
    void lineTotalMultipliesQuantityAndPrice() {
        assertThat(pricingService.lineTotal(2, new BigDecimal("19.99"))).isEqualByComparingTo("39.98");
    }

    @Test
    void lineTotalAppliesBulkDiscountAtThreshold() {
        // 10 x 10.00 = 100.00, minus 3% bulk discount = 97.00
        assertThat(pricingService.lineTotal(10, new BigDecimal("10.00"))).isEqualByComparingTo("97.00");
        // 9 items stay below the threshold.
        assertThat(pricingService.lineTotal(9, new BigDecimal("10.00"))).isEqualByComparingTo("90.00");
    }

    @Test
    void lineTotalRejectsInvalidInput() {
        assertThatThrownBy(() -> pricingService.lineTotal(0, BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pricingService.lineTotal(1, new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pricingService.lineTotal(1, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderTotalAppliesLoyaltyDiscountOnTopOfLineTotals() {
        Customer customer = new Customer().loyaltyPoints(500);
        OrderItem cheap = new OrderItem().quantity(2).unitPrice(new BigDecimal("10.00"));
        OrderItem bulk = new OrderItem().quantity(10).unitPrice(new BigDecimal("10.00"));
        // subtotal = 20.00 + 97.00 = 117.00; minus 10% loyalty = 105.30
        assertThat(pricingService.orderTotal(List.of(cheap, bulk), customer)).isEqualByComparingTo("105.30");
    }

    @Test
    void shippingFeeIsFlatBelowThreshold() {
        assertThat(pricingService.shippingFee(new BigDecimal("99.99"))).isEqualByComparingTo("5.99");
        assertThat(pricingService.shippingFee(BigDecimal.ZERO)).isEqualByComparingTo("5.99");
    }

    @Test
    void shippingFeeIsFreeAtThreshold() {
        assertThat(pricingService.shippingFee(new BigDecimal("100.00"))).isEqualByComparingTo("0");
        assertThat(pricingService.shippingFee(new BigDecimal("250.00"))).isEqualByComparingTo("0");
    }

    @Test
    void shippingFeeRejectsInvalidInput() {
        assertThatThrownBy(() -> pricingService.shippingFee(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pricingService.shippingFee(new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderTotalHandlesMissingCustomer() {
        OrderItem item = new OrderItem().quantity(1).unitPrice(new BigDecimal("5.00"));
        assertThat(pricingService.orderTotal(List.of(item), null)).isEqualByComparingTo("5.00");
    }
}
