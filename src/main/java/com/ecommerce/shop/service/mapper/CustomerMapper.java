package com.ecommerce.shop.service.mapper;

import com.ecommerce.shop.domain.Customer;
import com.ecommerce.shop.service.dto.CustomerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {}
