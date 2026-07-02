package com.ecommerce.shop.web.rest;

import static com.ecommerce.shop.domain.ShopOrderAsserts.*;
import static com.ecommerce.shop.web.rest.TestUtil.createUpdateProxyForBean;
import static com.ecommerce.shop.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ecommerce.shop.IntegrationTest;
import com.ecommerce.shop.domain.ShopOrder;
import com.ecommerce.shop.domain.enumeration.OrderStatus;
import com.ecommerce.shop.repository.ShopOrderRepository;
import com.ecommerce.shop.service.ShopOrderService;
import com.ecommerce.shop.service.dto.ShopOrderDTO;
import com.ecommerce.shop.service.mapper.ShopOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ShopOrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ShopOrderResourceIT {

    private static final Instant DEFAULT_PLACED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PLACED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.PENDING;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.PAID;

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/shop-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ShopOrderRepository shopOrderRepository;

    @Mock
    private ShopOrderRepository shopOrderRepositoryMock;

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Mock
    private ShopOrderService shopOrderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShopOrderMockMvc;

    private ShopOrder shopOrder;

    private ShopOrder insertedShopOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopOrder createEntity() {
        return new ShopOrder().placedAt(DEFAULT_PLACED_AT).status(DEFAULT_STATUS).totalAmount(DEFAULT_TOTAL_AMOUNT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopOrder createUpdatedEntity() {
        return new ShopOrder().placedAt(UPDATED_PLACED_AT).status(UPDATED_STATUS).totalAmount(UPDATED_TOTAL_AMOUNT);
    }

    @BeforeEach
    void initTest() {
        shopOrder = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedShopOrder != null) {
            shopOrderRepository.delete(insertedShopOrder);
            insertedShopOrder = null;
        }
    }

    @Test
    @Transactional
    void createShopOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);
        var returnedShopOrderDTO = om.readValue(
            restShopOrderMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShopOrderDTO.class
        );

        // Validate the ShopOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedShopOrder = shopOrderMapper.toEntity(returnedShopOrderDTO);
        assertShopOrderUpdatableFieldsEquals(returnedShopOrder, getPersistedShopOrder(returnedShopOrder));

        insertedShopOrder = returnedShopOrder;
    }

    @Test
    @Transactional
    void createShopOrderWithExistingId() throws Exception {
        // Create the ShopOrder with an existing ID
        shopOrder.setId(1L);
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restShopOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPlacedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        shopOrder.setPlacedAt(null);

        // Create the ShopOrder, which fails.
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        restShopOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        shopOrder.setStatus(null);

        // Create the ShopOrder, which fails.
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        restShopOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllShopOrders() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        // Get all the shopOrderList
        restShopOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shopOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].placedAt").value(hasItem(DEFAULT_PLACED_AT.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllShopOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(shopOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShopOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(shopOrderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllShopOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(shopOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShopOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(shopOrderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getShopOrder() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        // Get the shopOrder
        restShopOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, shopOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(shopOrder.getId().intValue()))
            .andExpect(jsonPath("$.placedAt").value(DEFAULT_PLACED_AT.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.totalAmount").value(sameNumber(DEFAULT_TOTAL_AMOUNT)));
    }

    @Test
    @Transactional
    void getNonExistingShopOrder() throws Exception {
        // Get the shopOrder
        restShopOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingShopOrder() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopOrder
        ShopOrder updatedShopOrder = shopOrderRepository.findById(shopOrder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedShopOrder are not directly saved in db
        em.detach(updatedShopOrder);
        updatedShopOrder.placedAt(UPDATED_PLACED_AT).status(UPDATED_STATUS).totalAmount(UPDATED_TOTAL_AMOUNT);
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(updatedShopOrder);

        restShopOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shopOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shopOrderDTO))
            )
            .andExpect(status().isOk());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedShopOrderToMatchAllProperties(updatedShopOrder);
    }

    @Test
    @Transactional
    void putNonExistingShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shopOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shopOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shopOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateShopOrderWithPatch() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopOrder using partial update
        ShopOrder partialUpdatedShopOrder = new ShopOrder();
        partialUpdatedShopOrder.setId(shopOrder.getId());

        partialUpdatedShopOrder.placedAt(UPDATED_PLACED_AT);

        restShopOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShopOrder))
            )
            .andExpect(status().isOk());

        // Validate the ShopOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShopOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedShopOrder, shopOrder),
            getPersistedShopOrder(shopOrder)
        );
    }

    @Test
    @Transactional
    void fullUpdateShopOrderWithPatch() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopOrder using partial update
        ShopOrder partialUpdatedShopOrder = new ShopOrder();
        partialUpdatedShopOrder.setId(shopOrder.getId());

        partialUpdatedShopOrder.placedAt(UPDATED_PLACED_AT).status(UPDATED_STATUS).totalAmount(UPDATED_TOTAL_AMOUNT);

        restShopOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShopOrder))
            )
            .andExpect(status().isOk());

        // Validate the ShopOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShopOrderUpdatableFieldsEquals(partialUpdatedShopOrder, getPersistedShopOrder(partialUpdatedShopOrder));
    }

    @Test
    @Transactional
    void patchNonExistingShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shopOrderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shopOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shopOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShopOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopOrder.setId(longCount.incrementAndGet());

        // Create the ShopOrder
        ShopOrderDTO shopOrderDTO = shopOrderMapper.toDto(shopOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(shopOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteShopOrder() throws Exception {
        // Initialize the database
        insertedShopOrder = shopOrderRepository.saveAndFlush(shopOrder);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the shopOrder
        restShopOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, shopOrder.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return shopOrderRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ShopOrder getPersistedShopOrder(ShopOrder shopOrder) {
        return shopOrderRepository.findById(shopOrder.getId()).orElseThrow();
    }

    protected void assertPersistedShopOrderToMatchAllProperties(ShopOrder expectedShopOrder) {
        assertShopOrderAllPropertiesEquals(expectedShopOrder, getPersistedShopOrder(expectedShopOrder));
    }

    protected void assertPersistedShopOrderToMatchUpdatableProperties(ShopOrder expectedShopOrder) {
        assertShopOrderAllUpdatablePropertiesEquals(expectedShopOrder, getPersistedShopOrder(expectedShopOrder));
    }
}
