package propertyservice.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import propertyservice.app.client.AgentServiceClient;
import propertyservice.app.client.CityServiceClient;
import propertyservice.app.client.PropertyTypeServiceClient;
import propertyservice.app.dto.PropertyCreateDto;
import propertyservice.app.dto.PropertyDto;
import propertyservice.app.dto.PropertyUpdateDto;
import propertyservice.app.entity.Property;
import propertyservice.app.entity.PropertyStatus;
import propertyservice.app.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PropertyRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @MockitoBean
    private AgentServiceClient agentServiceClient;

    @MockitoBean
    private CityServiceClient cityServiceClient;

    @MockitoBean
    private PropertyTypeServiceClient propertyTypeServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID agentId;
    private UUID cityId;
    private UUID propertyTypeId;
    private Property existingProperty;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();

        agentId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        propertyTypeId = UUID.randomUUID();

        when(agentServiceClient.agentExists(any(UUID.class))).thenReturn(true);
        when(cityServiceClient.cityExists(any(UUID.class))).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(any(UUID.class))).thenReturn(true);

        existingProperty = Property.builder()
                .title("Existing Property")
                .description("Existing Description")
                .price(new BigDecimal("150000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(2)
                .bathrooms(1)
                .squareFeet(1000)
                .address("123 Existing St")
                .isFeatured(false)
                .build();

        existingProperty = propertyRepository.save(existingProperty);
    }

    @Test
    void getAllProperties_ShouldReturnAllProperties() throws Exception {
        Property property2 = Property.builder()
                .title("Second Property")
                .description("Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property2);

        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @Test
    void getAllProperties_WithSearchParams_ShouldReturnFilteredProperties() throws Exception {
        Property property2 = Property.builder()
                .title("Luxury Villa")
                .description("Beautiful villa")
                .price(new BigDecimal("500000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property2);

        mockMvc.perform(get("/api/v1/properties")
                        .param("search", "Luxury")
                        .param("maxPrice", "600000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Luxury Villa"));
    }

    @Test
    void getPropertyById_WhenExists_ShouldReturnProperty() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{id}", existingProperty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingProperty.getId().toString()))
                .andExpect(jsonPath("$.title").value("Existing Property"))
                .andExpect(jsonPath("$.price").value(150000.00))
                .andExpect(jsonPath("$.bedrooms").value(2));
    }

    @Test
    void getPropertyById_WhenNotExists_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/properties/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createProperty_WhenValid_ShouldCreateAndReturnProperty() throws Exception {
        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("New Property")
                .description("New Description")
                .price(new BigDecimal("250000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(3)
                .bathrooms(2)
                .squareFeet(1500)
                .address("456 New St")
                .features(List.of("Pool", "Garage"))
                .imageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"))
                .build();

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Property"))
                .andExpect(jsonPath("$.price").value(250000.00))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features.length()").value(2))
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls.length()").value(2));

        List<Property> properties = propertyRepository.findAll();
        assertEquals(2, properties.size());
        assertTrue(properties.stream().anyMatch(p -> p.getTitle().equals("New Property")));
    }

    @Test
    void createProperty_WhenInvalidData_ShouldReturn400() throws Exception {
        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("")
                .price(null)
                .agentId(null)
                .build();

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProperty_WhenAgentNotExists_ShouldReturn500() throws Exception {
        when(agentServiceClient.agentExists(agentId)).thenReturn(false);

        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("New Property")
                .description("Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateProperty_WhenValid_ShouldUpdateAndReturnProperty() throws Exception {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Updated Property")
                .price(new BigDecimal("300000.00"))
                .bedrooms(4)
                .build();

        mockMvc.perform(put("/api/v1/properties/{id}", existingProperty.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Property"))
                .andExpect(jsonPath("$.price").value(300000.00))
                .andExpect(jsonPath("$.bedrooms").value(4));

        Property updated = propertyRepository.findById(existingProperty.getId()).orElseThrow();
        assertEquals("Updated Property", updated.getTitle());
        assertEquals(new BigDecimal("300000.00"), updated.getPrice());
        assertEquals(4, updated.getBedrooms());
    }

    @Test
    void updateProperty_WhenNotExists_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Updated Property")
                .build();

        mockMvc.perform(put("/api/v1/properties/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProperty_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() throws Exception {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Partially Updated")
                .build();

        mockMvc.perform(put("/api/v1/properties/{id}", existingProperty.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Partially Updated"))
                .andExpect(jsonPath("$.price").value(150000.00))
                .andExpect(jsonPath("$.bedrooms").value(2));

        Property updated = propertyRepository.findById(existingProperty.getId()).orElseThrow();
        assertEquals("Partially Updated", updated.getTitle());
        assertEquals(new BigDecimal("150000.00"), updated.getPrice());
        assertEquals(2, updated.getBedrooms());
    }

    @Test
    void deleteProperty_WhenExists_ShouldDeleteProperty() throws Exception {
        mockMvc.perform(delete("/api/v1/properties/{id}", existingProperty.getId()))
                .andExpect(status().isNoContent());

        assertFalse(propertyRepository.findById(existingProperty.getId()).isPresent());
    }

    @Test
    void deleteProperty_WhenNotExists_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/properties/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFeaturedProperties_ShouldReturnOnlyFeaturedProperties() throws Exception {
        existingProperty.setIsFeatured(true);
        propertyRepository.save(existingProperty);

        Property property2 = Property.builder()
                .title("Non-Featured")
                .description("Description")
                .price(new BigDecimal("100000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .isFeatured(false)
                .build();
        propertyRepository.save(property2);

        Property property3 = Property.builder()
                .title("Featured Property")
                .description("Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .isFeatured(true)
                .build();
        propertyRepository.save(property3);

        mockMvc.perform(get("/api/v1/properties/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isFeatured").value(true))
                .andExpect(jsonPath("$[1].isFeatured").value(true));
    }

    @Test
    void toggleFeatured_ShouldToggleFeaturedStatus() throws Exception {
        assertFalse(existingProperty.getIsFeatured());

        mockMvc.perform(put("/api/v1/properties/{id}/feature", existingProperty.getId()))
                .andExpect(status().isOk());

        Property updated = propertyRepository.findById(existingProperty.getId()).orElseThrow();
        assertTrue(updated.getIsFeatured());

        mockMvc.perform(put("/api/v1/properties/{id}/feature", existingProperty.getId()))
                .andExpect(status().isOk());

        Property toggled = propertyRepository.findById(existingProperty.getId()).orElseThrow();
        assertFalse(toggled.getIsFeatured());
    }

    @Test
    void toggleFeatured_WhenNotExists_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/properties/{id}/feature", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPropertiesByAgent_ShouldReturnPropertiesForAgent() throws Exception {
        UUID differentAgentId = UUID.randomUUID();
        Property property2 = Property.builder()
                .title("Agent Property")
                .description("Description")
                .price(new BigDecimal("180000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property2);

        Property property3 = Property.builder()
                .title("Other Agent Property")
                .description("Description")
                .price(new BigDecimal("220000.00"))
                .agentId(differentAgentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property3);

        mockMvc.perform(get("/api/v1/properties/agent/{agentId}", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].agentId").value(agentId.toString()))
                .andExpect(jsonPath("$[1].agentId").value(agentId.toString()));
    }

    @Test
    void getPropertiesByCity_ShouldReturnPropertiesForCity() throws Exception {
        UUID differentCityId = UUID.randomUUID();
        Property property2 = Property.builder()
                .title("City Property")
                .description("Description")
                .price(new BigDecimal("190000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property2);

        Property property3 = Property.builder()
                .title("Other City Property")
                .description("Description")
                .price(new BigDecimal("210000.00"))
                .agentId(agentId)
                .cityId(differentCityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .build();
        propertyRepository.save(property3);

        mockMvc.perform(get("/api/v1/properties/city/{cityId}", cityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cityId").value(cityId.toString()))
                .andExpect(jsonPath("$[1].cityId").value(cityId.toString()));
    }

    @Test
    void createProperty_WithImagesAndFeatures_ShouldPersistCorrectly() throws Exception {
        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("Property with Images")
                .description("Description")
                .price(new BigDecimal("350000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .features(List.of("Swimming Pool", "Garden", "Garage"))
                .imageUrls(List.of("http://example.com/img1.jpg", "http://example.com/img2.jpg", "http://example.com/img3.jpg"))
                .build();

        String response = mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.features.length()").value(3))
                .andExpect(jsonPath("$.imageUrls.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        PropertyDto createdDto = objectMapper.readValue(response, PropertyDto.class);
        Property saved = propertyRepository.findById(createdDto.getId()).orElseThrow();

        assertEquals(3, saved.getFeatures().size());
        assertEquals(3, saved.getImages().size());
        assertTrue(saved.getImages().get(0).getIsPrimary());
        assertEquals(0, saved.getImages().get(0).getDisplayOrder());
    }

    @Test
    void updateProperty_WithFeatures_ShouldReplaceFeatures() throws Exception {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .features(List.of("New Feature 1", "New Feature 2"))
                .build();

        mockMvc.perform(put("/api/v1/properties/{id}", existingProperty.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features.length()").value(2))
                .andExpect(jsonPath("$.features[0]").value("New Feature 1"))
                .andExpect(jsonPath("$.features[1]").value("New Feature 2"));

        Property updated = propertyRepository.findById(existingProperty.getId()).orElseThrow();
        assertEquals(2, updated.getFeatures().size());
    }
}


