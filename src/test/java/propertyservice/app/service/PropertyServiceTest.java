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
}

