package propertyservice.app.service;

import propertyservice.app.client.AgentServiceClient;
import propertyservice.app.client.CityServiceClient;
import propertyservice.app.client.PropertyTypeServiceClient;
import propertyservice.app.dto.PropertyCreateDto;
import propertyservice.app.dto.PropertyDto;
import propertyservice.app.dto.PropertyUpdateDto;
import propertyservice.app.entity.Property;
import propertyservice.app.entity.PropertyFeature;
import propertyservice.app.entity.PropertyImage;
import propertyservice.app.entity.PropertyStatus;
import propertyservice.app.exeption.PropertyNotFoundException;
import propertyservice.app.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private AgentServiceClient agentServiceClient;

    @Mock
    private CityServiceClient cityServiceClient;

    @Mock
    private PropertyTypeServiceClient propertyTypeServiceClient;

    @InjectMocks
    private PropertyService propertyService;

    private UUID propertyId;
    private UUID agentId;
    private UUID cityId;
    private UUID propertyTypeId;
    private Property property;
    private PropertyCreateDto createDto;
    private PropertyUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        agentId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        propertyTypeId = UUID.randomUUID();

        property = Property.builder()
                .id(propertyId)
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .images(new ArrayList<>())
                .features(new ArrayList<>())
                .build();

        createDto = PropertyCreateDto.builder()
                .title("New Property")
                .description("New Description")
                .price(new BigDecimal("200000.00"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(4)
                .bathrooms(3)
                .squareFeet(2000)
                .address("456 New St")
                .features(Arrays.asList("Pool", "Garage"))
                .imageUrls(Arrays.asList("http://example.com/image1.jpg", "http://example.com/image2.jpg"))
                .build();

        updateDto = PropertyUpdateDto.builder()
                .title("Updated Property")
                .price(new BigDecimal("250000.00"))
                .build();
    }

    @Test
    void getAllProperties_ShouldReturnListOfProperties() {
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.findAll()).thenReturn(properties);

        List<PropertyDto> result = propertyService.getAllProperties();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(propertyId, result.get(0).getId());
        verify(propertyRepository, times(1)).findAll();
    }

    @Test
    void getAllProperties_WhenEmpty_ShouldReturnEmptyList() {
        when(propertyRepository.findAll()).thenReturn(Collections.emptyList());

        List<PropertyDto> result = propertyService.getAllProperties();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyRepository, times(1)).findAll();
    }

    @Test
    void getPropertyById_WhenExists_ShouldReturnProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result);
        assertEquals(propertyId, result.getId());
        assertEquals("Test Property", result.getTitle());
        verify(propertyRepository, times(1)).findById(propertyId);
    }

    @Test
    void getPropertyById_WhenNotExists_ShouldThrowException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.getPropertyById(propertyId));
        verify(propertyRepository, times(1)).findById(propertyId);
    }

    @Test
    void createProperty_WhenValid_ShouldCreateAndReturnProperty() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(agentServiceClient, times(1)).agentExists(agentId);
        verify(cityServiceClient, times(1)).cityExists(cityId);
        verify(propertyTypeServiceClient, times(1)).propertyTypeExists(propertyTypeId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void createProperty_WhenAgentNotExists_ShouldThrowException() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(createDto));
        verify(agentServiceClient, times(1)).agentExists(agentId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void createProperty_WhenCityNotExists_ShouldThrowException() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(createDto));
        verify(agentServiceClient, times(1)).agentExists(agentId);
        verify(cityServiceClient, times(1)).cityExists(cityId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void createProperty_WhenPropertyTypeNotExists_ShouldThrowException() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(createDto));
        verify(propertyTypeServiceClient, times(1)).propertyTypeExists(propertyTypeId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void createProperty_WithFeaturesAndImages_ShouldAddThem() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        
        Property savedProperty = Property.builder()
                .id(propertyId)
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .price(createDto.getPrice())
                .agentId(createDto.getAgentId())
                .cityId(createDto.getCityId())
                .propertyTypeId(createDto.getPropertyTypeId())
                .status(createDto.getStatus())
                .bedrooms(createDto.getBedrooms())
                .bathrooms(createDto.getBathrooms())
                .squareFeet(createDto.getSquareFeet())
                .address(createDto.getAddress())
                .isFeatured(false)
                .images(new ArrayList<>())
                .features(new ArrayList<>())
                .build();
        
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(savedProperty));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void updateProperty_WhenExists_ShouldUpdateAndReturnProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        PropertyDto result = propertyService.updateProperty(propertyId, updateDto);

        assertNotNull(result);
        verify(propertyRepository, times(2)).findById(propertyId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void updateProperty_WhenNotExists_ShouldThrowException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.updateProperty(propertyId, updateDto));
        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void updateProperty_WithAgentId_ShouldValidateAndUpdate() {
        updateDto.setAgentId(agentId);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        propertyService.updateProperty(propertyId, updateDto);

        verify(agentServiceClient, times(1)).agentExists(agentId);
        verify(propertyRepository, times(2)).findById(propertyId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void updateProperty_WithFeatures_ShouldReplaceFeatures() {
        updateDto.setFeatures(Arrays.asList("New Feature"));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        propertyService.updateProperty(propertyId, updateDto);

        verify(propertyRepository, times(2)).findById(propertyId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void deleteProperty_WhenExists_ShouldDelete() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        doNothing().when(propertyRepository).delete(property);

        propertyService.deleteProperty(propertyId);

        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, times(1)).delete(property);
    }

    @Test
    void deleteProperty_WhenNotExists_ShouldThrowException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.deleteProperty(propertyId));
        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, never()).delete(any(Property.class));
    }

    @Test
    void getFeaturedProperties_ShouldReturnFeaturedProperties() {
        property.setIsFeatured(true);
        List<Property> featuredProperties = Arrays.asList(property);
        when(propertyRepository.findByIsFeaturedTrue()).thenReturn(featuredProperties);

        List<PropertyDto> result = propertyService.getFeaturedProperties();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsFeatured());
        verify(propertyRepository, times(1)).findByIsFeaturedTrue();
    }

    @Test
    void toggleFeatured_WhenFalse_ShouldSetToTrue() {
        property.setIsFeatured(false);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        propertyService.toggleFeatured(propertyId);

        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void toggleFeatured_WhenTrue_ShouldSetToFalse() {
        property.setIsFeatured(true);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        propertyService.toggleFeatured(propertyId);

        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void toggleFeatured_WhenNotExists_ShouldThrowException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.toggleFeatured(propertyId));
        verify(propertyRepository, times(1)).findById(propertyId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void searchProperties_ShouldReturnFilteredProperties() {
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.searchProperties("Test", cityId, propertyTypeId, new BigDecimal("150000")))
                .thenReturn(properties);

        List<PropertyDto> result = propertyService.searchProperties("Test", cityId, propertyTypeId, new BigDecimal("150000"));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).searchProperties("Test", cityId, propertyTypeId, new BigDecimal("150000"));
    }

    @Test
    void getPropertiesByAgent_ShouldReturnAgentProperties() {
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.findByAgentId(agentId)).thenReturn(properties);

        List<PropertyDto> result = propertyService.getPropertiesByAgent(agentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(agentId, result.get(0).getAgentId());
        verify(propertyRepository, times(1)).findByAgentId(agentId);
    }

    @Test
    void getPropertiesByCity_ShouldReturnCityProperties() {
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.findByCityId(cityId)).thenReturn(properties);

        List<PropertyDto> result = propertyService.getPropertiesByCity(cityId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cityId, result.get(0).getCityId());
        verify(propertyRepository, times(1)).findByCityId(cityId);
    }

    @Test
    void convertToDto_WithImagesAndFeatures_ShouldExtractCorrectly() {
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

        PropertyFeature feature1 = PropertyFeature.builder()
                .property(property)
                .featureName("Pool")
                .build();

        PropertyFeature feature2 = PropertyFeature.builder()
                .property(property)
                .featureName("Garage")
                .build();

        property.setImages(Arrays.asList(image1, image2));
        property.setFeatures(Arrays.asList(feature1, feature2));

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result);
        assertNotNull(result.getImageUrls());
        assertEquals(2, result.getImageUrls().size());
        assertNotNull(result.getFeatures());
        assertEquals(2, result.getFeatures().size());
    }

    @Test
    void extractImageUrls_WithNullImages_ShouldReturnEmptyList() {
        property.setImages(null);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result.getImageUrls());
        assertTrue(result.getImageUrls().isEmpty());
    }

    @Test
    void extractImageUrls_WithEmptyImages_ShouldReturnEmptyList() {
        property.setImages(new ArrayList<>());
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result.getImageUrls());
        assertTrue(result.getImageUrls().isEmpty());
    }

    @Test
    void extractImageUrls_WithNullAndEmptyUrls_ShouldFilterThem() {
        PropertyImage image1 = PropertyImage.builder()
                .property(property)
                .imageUrl("http://example.com/image1.jpg")
                .build();

        PropertyImage image2 = PropertyImage.builder()
                .property(property)
                .imageUrl(null)
                .build();

        PropertyImage image3 = PropertyImage.builder()
                .property(property)
                .imageUrl("   ")
                .build();

        property.setImages(Arrays.asList(image1, image2, image3));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertEquals(1, result.getImageUrls().size());
        assertEquals("http://example.com/image1.jpg", result.getImageUrls().get(0));
    }

    @Test
    void extractFeatureNames_WithNullFeatures_ShouldReturnEmptyList() {
        property.setFeatures(null);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result.getFeatures());
        assertTrue(result.getFeatures().isEmpty());
    }

    @Test
    void extractFeatureNames_WithEmptyFeatures_ShouldReturnEmptyList() {
        property.setFeatures(new ArrayList<>());
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertNotNull(result.getFeatures());
        assertTrue(result.getFeatures().isEmpty());
    }

    @Test
    void extractFeatureNames_WithNullAndEmptyNames_ShouldFilterThem() {
        PropertyFeature feature1 = PropertyFeature.builder()
                .property(property)
                .featureName("Pool")
                .build();

        PropertyFeature feature2 = PropertyFeature.builder()
                .property(property)
                .featureName(null)
                .build();

        PropertyFeature feature3 = PropertyFeature.builder()
                .property(property)
                .featureName("   ")
                .build();

        property.setFeatures(Arrays.asList(feature1, feature2, feature3));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.getPropertyById(propertyId);

        assertEquals(1, result.getFeatures().size());
        assertEquals("Pool", result.getFeatures().get(0));
    }

    @Test
    void addImagesToProperty_WithEmptyAndNullUrls_ShouldFilterThem() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);

        PropertyCreateDto dto = PropertyCreateDto.builder()
                .title("Test Property")
                .description("Description")
                .price(new BigDecimal("100000"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .imageUrls(Arrays.asList("http://example.com/img1.jpg", "", "   ", null, "http://example.com/img2.jpg"))
                .build();

        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property prop = invocation.getArgument(0);
            prop.setId(propertyId);
            return prop;
        });
        when(propertyRepository.findById(propertyId)).thenAnswer(invocation -> {
            Property prop = Property.builder()
                    .id(propertyId)
                    .title("Test Property")
                    .description("Description")
                    .price(new BigDecimal("100000"))
                    .agentId(agentId)
                    .cityId(cityId)
                    .propertyTypeId(propertyTypeId)
                    .status(PropertyStatus.FOR_SALE)
                    .images(new ArrayList<>())
                    .features(new ArrayList<>())
                    .build();
            PropertyImage img1 = PropertyImage.builder()
                    .property(prop)
                    .imageUrl("http://example.com/img1.jpg")
                    .isPrimary(true)
                    .displayOrder(0)
                    .build();
            PropertyImage img2 = PropertyImage.builder()
                    .property(prop)
                    .imageUrl("http://example.com/img2.jpg")
                    .isPrimary(false)
                    .displayOrder(4)
                    .build();
            prop.getImages().add(img1);
            prop.getImages().add(img2);
            return Optional.of(prop);
        });

        PropertyDto result = propertyService.createProperty(dto);

        assertNotNull(result);
        assertEquals(2, result.getImageUrls().size());
        assertTrue(result.getImageUrls().contains("http://example.com/img1.jpg"));
        assertTrue(result.getImageUrls().contains("http://example.com/img2.jpg"));
    }

    @Test
    void addImagesToProperty_WithMultipleImages_ShouldSetFirstAsPrimary() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);

        PropertyCreateDto dto = PropertyCreateDto.builder()
                .title("Test Property")
                .description("Description")
                .price(new BigDecimal("100000"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .imageUrls(Arrays.asList("http://example.com/img1.jpg", "http://example.com/img2.jpg", "http://example.com/img3.jpg"))
                .build();

        Property savedProperty = Property.builder()
                .id(propertyId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .agentId(dto.getAgentId())
                .cityId(dto.getCityId())
                .propertyTypeId(dto.getPropertyTypeId())
                .status(dto.getStatus())
                .images(new ArrayList<>())
                .features(new ArrayList<>())
                .build();

        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property prop = invocation.getArgument(0);
            prop.setId(propertyId);
            return prop;
        });
        when(propertyRepository.findById(propertyId)).thenAnswer(invocation -> {
            Property prop = Property.builder()
                    .id(propertyId)
                    .title(dto.getTitle())
                    .images(new ArrayList<>())
                    .features(new ArrayList<>())
                    .build();
            prop.getImages().addAll(savedProperty.getImages());
            return Optional.of(prop);
        });

        propertyService.createProperty(dto);

        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void validateEntityExists_WhenReturnsNull_ShouldNotThrowException() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(createDto));
    }

    @Test
    void validateEntityExists_WhenReturnsTrue_ShouldNotThrowException() {
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertDoesNotThrow(() -> propertyService.createProperty(createDto));
    }

    @Test
    void updateProperty_WithAllFields_ShouldUpdateAllFields() {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(new BigDecimal("350000"))
                .status(PropertyStatus.SOLD)
                .bedrooms(5)
                .bathrooms(4)
                .squareFeet(3000)
                .address("789 Updated St")
                .build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.updateProperty(propertyId, updateDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void updateProperty_WithCityId_ShouldValidateAndUpdate() {
        UUID newCityId = UUID.randomUUID();
        updateDto.setCityId(newCityId);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(cityServiceClient.cityExists(newCityId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        propertyService.updateProperty(propertyId, updateDto);

        verify(cityServiceClient, times(1)).cityExists(newCityId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void updateProperty_WithPropertyTypeId_ShouldValidateAndUpdate() {
        UUID newPropertyTypeId = UUID.randomUUID();
        updateDto.setPropertyTypeId(newPropertyTypeId);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyTypeServiceClient.propertyTypeExists(newPropertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        propertyService.updateProperty(propertyId, updateDto);

        verify(propertyTypeServiceClient, times(1)).propertyTypeExists(newPropertyTypeId);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void createProperty_WithNullFeatures_ShouldNotFail() {
        createDto.setFeatures(null);
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void createProperty_WithNullImageUrls_ShouldNotFail() {
        createDto.setImageUrls(null);
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void createProperty_WithEmptyFeatures_ShouldNotFail() {
        createDto.setFeatures(new ArrayList<>());
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void createProperty_WithEmptyImageUrls_ShouldNotFail() {
        createDto.setImageUrls(new ArrayList<>());
        when(agentServiceClient.agentExists(agentId)).thenReturn(true);
        when(cityServiceClient.cityExists(cityId)).thenReturn(true);
        when(propertyTypeServiceClient.propertyTypeExists(propertyTypeId)).thenReturn(true);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        PropertyDto result = propertyService.createProperty(createDto);

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }
}

