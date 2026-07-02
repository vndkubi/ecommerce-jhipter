package com.ecommerce.shop.service.mapper;

import com.ecommerce.shop.domain.OrderItem;
import com.ecommerce.shop.domain.Product;
import com.ecommerce.shop.domain.ShopOrder;
import com.ecommerce.shop.service.dto.OrderItemDTO;
import com.ecommerce.shop.service.dto.ProductDTO;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link OrderItem} and its DTO {@link OrderItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper extends EntityMapper<OrderItemDTO, OrderItem> {
    @Mapping(target = "order", source = "order", qualifiedByName = "shopOrderId")
    @Mapping(target = "product", source = "product", qualifiedByName = "productSku")
    OrderItemDTO toDto(OrderItem s);

    @Named("shopOrderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ShopOrderDTO toDtoShopOrderId(ShopOrder shopOrder);

    @Named("productSku")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "sku", source = "sku")
    ProductDTO toDtoProductSku(Product product);
}
