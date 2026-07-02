package com.ecommerce.shop.web.rest;

import com.ecommerce.shop.service.CheckoutService;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for checking out a {@link com.ecommerce.shop.domain.ShopOrder}.
 */
@RestController
@RequestMapping("/api/shop-orders")
public class CheckoutResource {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutResource.class);

    private final CheckoutService checkoutService;

    public CheckoutResource(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * {@code POST /shop-orders/:id/checkout} : check out a pending order,
     * validating and decrementing stock and computing the final total.
     *
     * @param id the id of the order to check out.
     * @return the updated order.
     */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<ShopOrderDTO> checkoutOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to checkout ShopOrder : {}", id);
        return ResponseEntity.ok(checkoutService.checkout(id));
    }
}
