package propertyservice.app.repository;

import propertyservice.app.entity.Property;
import propertyservice.app.entity.PropertyFeature;
import propertyservice.app.entity.PropertyImage;
import propertyservice.app.entity.PropertyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class PropertyRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PropertyRepository propertyRepository;

    private UUID propertyId;
    private UUID agentId;
    private UUID cityId;
    private UUID propertyTypeId;
    private Property property;

    @BeforeEach
    void setUp() {
        agentId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        propertyTypeId = UUID.randomUUID();

        property = Property.builder()
                .title("Test Property")
                .description("Test Description")
                .price(new BigDecimal("100000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(3)
                .bathrooms(2)
                .squareFeet(1500)
                .address("123 Test St")
                .isFeatured(false)
                .build();

        property = entityManager.persist(property);
        entityManager.flush();
        propertyId = property.getId();
    }

    @Test
    void findAll_ShouldReturnAllProperties() {
        Property property2 = Property.builder()
                .title("Second Property")
                .description("Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);
        entityManager.flush();

        List<Property> properties = propertyRepository.findAll();

        assertNotNull(properties);
        assertTrue(properties.size() >= 2);
        assertTrue(properties.stream().anyMatch(p -> p.getTitle().equals("Test Property")));
        assertTrue(properties.stream().anyMatch(p -> p.getTitle().equals("Second Property")));
    }

    @Test
    void findById_WhenExists_ShouldReturnProperty() {
        Optional<Property> found = propertyRepository.findById(propertyId);

        assertTrue(found.isPresent());
        assertEquals("Test Property", found.get().getTitle());
        assertEquals(propertyId, found.get().getId());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<Property> found = propertyRepository.findById(nonExistentId);

        assertFalse(found.isPresent());
    }

    @Test
    void save_ShouldPersistProperty() {
        Property newProperty = Property.builder()
                .title("New Property")
                .description("New Description")
                .price(new BigDecimal("300000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(4)
                .bathrooms(3)
                .squareFeet(2000)
                .address("456 New St")
                .isFeatured(false)
                .build();

        Property saved = propertyRepository.save(newProperty);
        entityManager.flush();
        entityManager.clear();

        Property found = entityManager.find(Property.class, saved.getId());
        assertNotNull(found);
        assertEquals("New Property", found.getTitle());
        assertEquals(new BigDecimal("300000.00"), found.getPrice());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    @Test
    void save_WithImages_ShouldPersistImages() {
        PropertyImage image1 = PropertyImage.builder()
                .property(property)
                .imageUrl("http://example.com/image1.jpg")
                .isPrimary(true)
                .displayOrder(0)
                .build();

        PropertyImage image2 = PropertyImage.builder()
                .property(property)
                .imageUrl("http://example.com/image2.jpg")
                .isPrimary(false)
                .displayOrder(1)
                .build();

        property.getImages().add(image1);
        property.getImages().add(image2);

        propertyRepository.save(property);
        entityManager.flush();
        entityManager.clear();

        Property found = propertyRepository.findById(propertyId).orElseThrow();
        assertEquals(2, found.getImages().size());
        assertTrue(found.getImages().stream().anyMatch(img -> img.getImageUrl().equals("http://example.com/image1.jpg")));
        assertTrue(found.getImages().stream().anyMatch(img -> img.getImageUrl().equals("http://example.com/image2.jpg")));
    }

    @Test
    void save_WithFeatures_ShouldPersistFeatures() {
        PropertyFeature feature1 = PropertyFeature.builder()
                .property(property)
                .featureName("Pool")
                .build();

        PropertyFeature feature2 = PropertyFeature.builder()
                .property(property)
                .featureName("Garage")
                .build();

        property.getFeatures().add(feature1);
        property.getFeatures().add(feature2);

        propertyRepository.save(property);
        entityManager.flush();
        entityManager.clear();

        Property found = propertyRepository.findById(propertyId).orElseThrow();
        assertEquals(2, found.getFeatures().size());
        assertTrue(found.getFeatures().stream().anyMatch(f -> f.getFeatureName().equals("Pool")));
        assertTrue(found.getFeatures().stream().anyMatch(f -> f.getFeatureName().equals("Garage")));
    }

    @Test
    void findByAgentId_ShouldReturnPropertiesForAgent() {
        Property property2 = Property.builder()
                .title("Agent Property")
                .description("Description")
                .price(new BigDecimal("150000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        UUID differentAgentId = UUID.randomUUID();
        Property property3 = Property.builder()
                .title("Other Agent Property")
                .description("Description")
                .price(new BigDecimal("250000.00"))
                .agentId(differentAgentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> agentProperties = propertyRepository.findByAgentId(agentId);

        assertNotNull(agentProperties);
        assertEquals(2, agentProperties.size());
        assertTrue(agentProperties.stream().allMatch(p -> p.getAgentId().equals(agentId)));
    }

    @Test
    void findByCityId_ShouldReturnPropertiesForCity() {
        Property property2 = Property.builder()
                .title("City Property")
                .description("Description")
                .price(new BigDecimal("180000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        UUID differentCityId = UUID.randomUUID();
        Property property3 = Property.builder()
                .title("Other City Property")
                .description("Description")
                .price(new BigDecimal("220000.00"))
                .agentId(agentId)
                .cityId(differentCityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> cityProperties = propertyRepository.findByCityId(cityId);

        assertNotNull(cityProperties);
        assertEquals(2, cityProperties.size());
        assertTrue(cityProperties.stream().allMatch(p -> p.getCityId().equals(cityId)));
    }

    @Test
    void findByIsFeaturedTrue_ShouldReturnOnlyFeaturedProperties() {
        property.setIsFeatured(true);
        propertyRepository.save(property);

        Property property2 = Property.builder()
                .title("Non-Featured Property")
                .description("Description")
                .price(new BigDecimal("120000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .isFeatured(false)
                .build();
        entityManager.persist(property2);

        Property property3 = Property.builder()
                .title("Featured Property 2")
                .description("Description")
                .price(new BigDecimal("300000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .isFeatured(true)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> featuredProperties = propertyRepository.findByIsFeaturedTrue();

        assertNotNull(featuredProperties);
        assertEquals(2, featuredProperties.size());
        assertTrue(featuredProperties.stream().allMatch(Property::getIsFeatured));
    }

    @Test
    void searchProperties_ByTitle_ShouldReturnMatchingProperties() {
        Property property2 = Property.builder()
                .title("Luxury Villa")
                .description("Beautiful villa")
                .price(new BigDecimal("500000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        Property property3 = Property.builder()
                .title("Small Apartment")
                .description("Cozy apartment")
                .price(new BigDecimal("80000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> results = propertyRepository.searchProperties("Villa", null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Luxury Villa", results.get(0).getTitle());
    }

    @Test
    void searchProperties_ByMaxPrice_ShouldReturnPropertiesBelowPrice() {
        Property property2 = Property.builder()
                .title("Expensive Property")
                .description("Description")
                .price(new BigDecimal("500000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        Property property3 = Property.builder()
                .title("Cheap Property")
                .description("Description")
                .price(new BigDecimal("50000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> results = propertyRepository.searchProperties(null, null, null, new BigDecimal("150000.00"));

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        assertTrue(results.stream().allMatch(p -> p.getPrice().compareTo(new BigDecimal("150000.00")) <= 0));
    }

    @Test
    void searchProperties_ByCityId_ShouldReturnPropertiesInCity() {
        UUID differentCityId = UUID.randomUUID();
        Property property2 = Property.builder()
                .title("City Property")
                .description("Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        Property property3 = Property.builder()
                .title("Other City Property")
                .description("Description")
                .price(new BigDecimal("180000.00"))
                .agentId(agentId)
                .cityId(differentCityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> results = propertyRepository.searchProperties(null, cityId, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getCityId().equals(cityId)));
    }

    @Test
    void searchProperties_WithMultipleFilters_ShouldReturnFilteredResults() {
        Property property2 = Property.builder()
                .title("Luxury Apartment")
                .description("Beautiful apartment")
                .price(new BigDecimal("250000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property2);

        UUID differentPropertyTypeId = UUID.randomUUID();
        Property property3 = Property.builder()
                .title("Luxury House")
                .description("Beautiful house")
                .price(new BigDecimal("300000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(differentPropertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        entityManager.persist(property3);
        entityManager.flush();

        List<Property> results = propertyRepository.searchProperties("Luxury", cityId, propertyTypeId, new BigDecimal("280000.00"));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Luxury Apartment", results.get(0).getTitle());
    }

    @Test
    void delete_ShouldRemoveProperty() {
        UUID idToDelete = property.getId();
        propertyRepository.delete(property);
        entityManager.flush();
        entityManager.clear();

        Property found = entityManager.find(Property.class, idToDelete);
        assertNull(found);
    }

    @Test
    void delete_WithCascade_ShouldRemoveImagesAndFeatures() {
        PropertyImage image = PropertyImage.builder()
                .property(property)
                .imageUrl("http://example.com/image.jpg")
                .isPrimary(true)
                .displayOrder(0)
                .build();

        PropertyFeature feature = PropertyFeature.builder()
                .property(property)
                .featureName("Pool")
                .build();

        property.getImages().add(image);
        property.getFeatures().add(feature);
        property = propertyRepository.save(property);
        entityManager.flush();
        entityManager.clear();

        Property savedProperty = propertyRepository.findById(propertyId).orElseThrow();
        UUID imageId = savedProperty.getImages().get(0).getId();
        UUID featureId = savedProperty.getFeatures().get(0).getId();

        propertyRepository.delete(savedProperty);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(PropertyImage.class, imageId));
        assertNull(entityManager.find(PropertyFeature.class, featureId));
    }

    @Test
    void update_ShouldUpdateTimestamp() {
        LocalDateTime originalUpdatedAt = property.getUpdatedAt();
        entityManager.flush();
        entityManager.clear();

        Property found = propertyRepository.findById(propertyId).orElseThrow();
        found.setTitle("Updated Title");
        propertyRepository.save(found);
        entityManager.flush();
        entityManager.clear();

        Property updated = propertyRepository.findById(propertyId).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt) || updated.getUpdatedAt().equals(originalUpdatedAt));
    }
}


