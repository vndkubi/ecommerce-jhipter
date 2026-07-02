package com.ecommerce.shop.service;

import com.ecommerce.shop.domain.Customer;
import com.ecommerce.shop.domain.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Computes order pricing: per-line totals with a bulk discount and an
 * order-level loyalty discount based on the customer's accumulated points.
 */
@Service
public class PricingService {

    /** Quantity at or above which a line gets the bulk discount. */
    public static final int BULK_QUANTITY_THRESHOLD = 10;

    /** Merchandise total at or above which the order ships free. */
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");

    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.03");

    private static final BigDecimal FLAT_SHIPPING_FEE = new BigDecimal("5.99");

    /**
     * Loyalty discount rate for a customer.
     *
     * @param loyaltyPoints the customer's points (null-safe).
     * @return 0, 0.05, 0.10 or 0.15 depending on the tier.
     */
    public BigDecimal loyaltyDiscountRate(Integer loyaltyPoints) {
        int points = loyaltyPoints == null ? 0 : loyaltyPoints;
        if (points >= 1000) {
            return new BigDecimal("0.15");
        }
        if (points >= 500) {
            return new BigDecimal("0.10");
        }
        if (points >= 100) {
            return new BigDecimal("0.05");
        }
        return BigDecimal.ZERO;
    }

    /**
     * Line total: quantity x unit price, minus the bulk discount when the
     * quantity reaches {@link #BULK_QUANTITY_THRESHOLD}.
     */
    public BigDecimal lineTotal(int quantity, BigDecimal unitPrice) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (quantity >= BULK_QUANTITY_THRESHOLD) {
            gross = gross.subtract(gross.multiply(BULK_DISCOUNT_RATE));
        }
        return gross.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Shipping fee for an order: free at or above
     * {@link #FREE_SHIPPING_THRESHOLD}, otherwise a flat fee.
     */
    public BigDecimal shippingFee(BigDecimal merchandiseTotal) {
        if (merchandiseTotal == null || merchandiseTotal.signum() < 0) {
            throw new IllegalArgumentException("merchandiseTotal must be >= 0");
        }
        if (merchandiseTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return FLAT_SHIPPING_FEE;
    }

    /**
     * Order total: sum of line totals with the customer's loyalty discount
     * applied on top, rounded to cents.
     */
    public BigDecimal orderTotal(List<OrderItem> items, Customer customer) {
        BigDecimal subtotal = items
            .stream()
            .map(item -> lineTotal(item.getQuantity(), item.getUnitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = loyaltyDiscountRate(customer == null ? null : customer.getLoyaltyPoints());
        BigDecimal discounted = subtotal.subtract(subtotal.multiply(rate));
        return discounted.setScale(2, RoundingMode.HALF_UP);
    }
}
