package com.ecommerce.shop.service;

import com.ecommerce.shop.domain.OrderItem;
import com.ecommerce.shop.domain.Product;
import com.ecommerce.shop.domain.ShopOrder;
import com.ecommerce.shop.domain.enumeration.OrderStatus;
import com.ecommerce.shop.repository.OrderItemRepository;
import com.ecommerce.shop.repository.ProductRepository;
import com.ecommerce.shop.repository.ShopOrderRepository;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import com.ecommerce.shop.service.mapper.ShopOrderMapper;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns a PENDING order into a PAID one: validates stock for every line,
 * decrements inventory, prices the order through {@link PricingService} and
 * persists the final total.
 */
@Service
@Transactional
public class CheckoutService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutService.class);

    private final ShopOrderRepository shopOrderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    private final PricingService pricingService;

    private final ShopOrderMapper shopOrderMapper;

    public CheckoutService(
        ShopOrderRepository shopOrderRepository,
        OrderItemRepository orderItemRepository,
        ProductRepository productRepository,
        PricingService pricingService,
        ShopOrderMapper shopOrderMapper
    ) {
        this.shopOrderRepository = shopOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
        this.shopOrderMapper = shopOrderMapper;
    }

    /**
     * Check out a pending order.
     *
     * @param orderId the order to check out.
     * @return the updated order.
     * @throws NoSuchElementException when the order does not exist.
     * @throws IllegalStateException when the order is not PENDING or has no items.
     * @throws InsufficientStockException when any line exceeds available stock.
     */
    public ShopOrderDTO checkout(Long orderId) {
        LOG.debug("Request to checkout ShopOrder : {}", orderId);
        ShopOrder order = shopOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("ShopOrder " + orderId + " not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("ShopOrder " + orderId + " is " + order.getStatus() + ", only PENDING orders can be checked out");
        }
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalStateException("ShopOrder " + orderId + " has no items");
        }

        for (OrderItem item : items) {
            Product product = item.getProduct();
            int available = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (available < item.getQuantity()) {
                throw new InsufficientStockException(product.getSku(), item.getQuantity(), available);
            }
        }
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(pricingService.orderTotal(items, order.getCustomer()));
        order.setStatus(OrderStatus.PAID);
        order = shopOrderRepository.save(order);
        return shopOrderMapper.toDto(order);
    }
}
