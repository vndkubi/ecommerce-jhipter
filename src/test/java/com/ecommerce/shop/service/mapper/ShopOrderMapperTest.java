package com.ecommerce.shop.service.mapper;

import static com.ecommerce.shop.domain.ShopOrderAsserts.*;
import static com.ecommerce.shop.domain.ShopOrderTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShopOrderMapperTest {

    private ShopOrderMapper shopOrderMapper;

    @BeforeEach
    void setUp() {
        shopOrderMapper = new ShopOrderMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getShopOrderSample1();
        var actual = shopOrderMapper.toEntity(shopOrderMapper.toDto(expected));
        assertShopOrderAllPropertiesEquals(expected, actual);
    }
}
