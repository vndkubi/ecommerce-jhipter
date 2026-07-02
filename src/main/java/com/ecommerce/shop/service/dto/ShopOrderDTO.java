package com.ecommerce.shop.service.dto;

import com.ecommerce.shop.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.ecommerce.shop.domain.ShopOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ShopOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant placedAt;

    @NotNull
    private OrderStatus status;

    private BigDecimal totalAmount;

    private CustomerDTO customer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShopOrderDTO)) {
            return false;
        }

        ShopOrderDTO shopOrderDTO = (ShopOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, shopOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ShopOrderDTO{" +
            "id=" + getId() +
            ", placedAt='" + getPlacedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", totalAmount=" + getTotalAmount() +
            ", customer=" + getCustomer() +
            "}";
    }
}
