package com.ecommerce.shop.web.rest;

import com.ecommerce.shop.repository.ShopOrderRepository;
import com.ecommerce.shop.service.ShopOrderService;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import com.ecommerce.shop.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.ecommerce.shop.domain.ShopOrder}.
 */
@RestController
@RequestMapping("/api/shop-orders")
public class ShopOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShopOrderResource.class);

    private static final String ENTITY_NAME = "shopOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShopOrderService shopOrderService;

    private final ShopOrderRepository shopOrderRepository;

    public ShopOrderResource(ShopOrderService shopOrderService, ShopOrderRepository shopOrderRepository) {
        this.shopOrderService = shopOrderService;
        this.shopOrderRepository = shopOrderRepository;
    }

    /**
     * {@code POST  /shop-orders} : Create a new shopOrder.
     *
     * @param shopOrderDTO the shopOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shopOrderDTO, or with status {@code 400 (Bad Request)} if the shopOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ShopOrderDTO> createShopOrder(@Valid @RequestBody ShopOrderDTO shopOrderDTO) throws URISyntaxException {
        LOG.debug("REST request to save ShopOrder : {}", shopOrderDTO);
        if (shopOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new shopOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        shopOrderDTO = shopOrderService.save(shopOrderDTO);
        return ResponseEntity.created(new URI("/api/shop-orders/" + shopOrderDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, shopOrderDTO.getId().toString()))
            .body(shopOrderDTO);
    }

    /**
     * {@code PUT  /shop-orders/:id} : Updates an existing shopOrder.
     *
     * @param id the id of the shopOrderDTO to save.
     * @param shopOrderDTO the shopOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopOrderDTO,
     * or with status {@code 400 (Bad Request)} if the shopOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shopOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShopOrderDTO> updateShopOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ShopOrderDTO shopOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ShopOrder : {}, {}", id, shopOrderDTO);
        if (shopOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shopOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        shopOrderDTO = shopOrderService.update(shopOrderDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shopOrderDTO.getId().toString()))
            .body(shopOrderDTO);
    }

    /**
     * {@code PATCH  /shop-orders/:id} : Partial updates given fields of an existing shopOrder, field will ignore if it is null
     *
     * @param id the id of the shopOrderDTO to save.
     * @param shopOrderDTO the shopOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopOrderDTO,
     * or with status {@code 400 (Bad Request)} if the shopOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the shopOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the shopOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShopOrderDTO> partialUpdateShopOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ShopOrderDTO shopOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ShopOrder partially : {}, {}", id, shopOrderDTO);
        if (shopOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shopOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShopOrderDTO> result = shopOrderService.partialUpdate(shopOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shopOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /shop-orders} : get all the shopOrders.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shopOrders in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ShopOrderDTO>> getAllShopOrders(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ShopOrders");
        Page<ShopOrderDTO> page;
        if (eagerload) {
            page = shopOrderService.findAllWithEagerRelationships(pageable);
        } else {
            page = shopOrderService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /shop-orders/:id} : get the "id" shopOrder.
     *
     * @param id the id of the shopOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shopOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopOrderDTO> getShopOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ShopOrder : {}", id);
        Optional<ShopOrderDTO> shopOrderDTO = shopOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(shopOrderDTO);
    }

    /**
     * {@code DELETE  /shop-orders/:id} : delete the "id" shopOrder.
     *
     * @param id the id of the shopOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShopOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ShopOrder : {}", id);
        shopOrderService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
