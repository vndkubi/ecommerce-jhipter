package com.ecommerce.shop.service.mapper;

import com.ecommerce.shop.domain.Customer;
import com.ecommerce.shop.domain.ShopOrder;
import com.ecommerce.shop.service.dto.CustomerDTO;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ShopOrder} and its DTO {@link ShopOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface ShopOrderMapper extends EntityMapper<ShopOrderDTO, ShopOrder> {
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerEmail")
    ShopOrderDTO toDto(ShopOrder s);

    @Named("customerEmail")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    CustomerDTO toDtoCustomerEmail(Customer customer);
}
