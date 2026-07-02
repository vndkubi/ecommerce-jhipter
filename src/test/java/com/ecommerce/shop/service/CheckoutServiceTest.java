package com.ecommerce.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.shop.domain.Customer;
import com.ecommerce.shop.domain.OrderItem;
import com.ecommerce.shop.domain.Product;
import com.ecommerce.shop.domain.ShopOrder;
import com.ecommerce.shop.domain.enumeration.OrderStatus;
import com.ecommerce.shop.repository.OrderItemRepository;
import com.ecommerce.shop.repository.ProductRepository;
import com.ecommerce.shop.repository.ShopOrderRepository;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import com.ecommerce.shop.service.mapper.ShopOrderMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CheckoutService}.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private ShopOrderRepository shopOrderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopOrderMapper shopOrderMapper;

    private CheckoutService checkoutService;

    private ShopOrder order;

    private Product product;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
            shopOrderRepository,
            orderItemRepository,
            productRepository,
            new PricingService(),
            shopOrderMapper
        );
        Customer customer = new Customer().loyaltyPoints(500);
        order = new ShopOrder().status(OrderStatus.PENDING).customer(customer);
        order.setId(7L);
        product = new Product().sku("SKU-1").stockQuantity(20).price(new BigDecimal("10.00"));
    }

    @Test
    void checkoutPaysPendingOrderAndDecrementsStock() {
        OrderItem item = new OrderItem().quantity(10).unitPrice(new BigDecimal("10.00")).product(product);
        when(shopOrderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(7L)).thenReturn(List.of(item));
        when(shopOrderRepository.save(any(ShopOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shopOrderMapper.toDto(any(ShopOrder.class))).thenReturn(new ShopOrderDTO());

        checkoutService.checkout(7L);

        ArgumentCaptor<ShopOrder> saved = ArgumentCaptor.forClass(ShopOrder.class);
        verify(shopOrderRepository).save(saved.capture());
        // 10 x 10.00 with 3% bulk discount = 97.00, minus 10% loyalty = 87.30
        assertThat(saved.getValue().getTotalAmount()).isEqualByComparingTo("87.30");
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(product.getStockQuantity()).isEqualTo(10);
        verify(productRepository).save(product);
    }

    @Test
    void checkoutRejectsMissingOrder() {
        when(shopOrderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> checkoutService.checkout(1L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void checkoutRejectsNonPendingOrder() {
        order.setStatus(OrderStatus.PAID);
        when(shopOrderRepository.findById(7L)).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> checkoutService.checkout(7L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkoutRejectsEmptyOrder() {
        when(shopOrderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(7L)).thenReturn(List.of());
        assertThatThrownBy(() -> checkoutService.checkout(7L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkoutRejectsInsufficientStockWithoutSavingAnything() {
        OrderItem tooMany = new OrderItem().quantity(25).unitPrice(new BigDecimal("10.00")).product(product);
        when(shopOrderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(7L)).thenReturn(List.of(tooMany));

        assertThatThrownBy(() -> checkoutService.checkout(7L)).isInstanceOf(InsufficientStockException.class);

        assertThat(product.getStockQuantity()).isEqualTo(20);
        verify(productRepository, never()).save(any());
        verify(shopOrderRepository, never()).save(any());
    }
}
