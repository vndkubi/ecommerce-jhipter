package com.ecommerce.shop.service.mapper;

import com.ecommerce.shop.domain.Category;
import com.ecommerce.shop.service.dto.CategoryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Category} and its DTO {@link CategoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category> {}
