package com.ecommerce.shop.repository;

import com.ecommerce.shop.domain.ShopOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ShopOrder entity.
 */
@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
    default Optional<ShopOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ShopOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ShopOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select shopOrder from ShopOrder shopOrder left join fetch shopOrder.customer",
        countQuery = "select count(shopOrder) from ShopOrder shopOrder"
    )
    Page<ShopOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query("select shopOrder from ShopOrder shopOrder left join fetch shopOrder.customer")
    List<ShopOrder> findAllWithToOneRelationships();

    @Query("select shopOrder from ShopOrder shopOrder left join fetch shopOrder.customer where shopOrder.id =:id")
    Optional<ShopOrder> findOneWithToOneRelationships(@Param("id") Long id);
}
