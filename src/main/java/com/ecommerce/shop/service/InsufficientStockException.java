package com.ecommerce.shop.service;

/**
 * Thrown when a checkout requests more units of a product than are in stock.
 */
public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for product " + sku + ": requested " + requested + ", available " + available);
    }
}
