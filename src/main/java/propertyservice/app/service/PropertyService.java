package propertyservice.app.service;

import propertyservice.app.dto.*;
import propertyservice.app.entity.Property;
import propertyservice.app.entity.PropertyFeature;
import propertyservice.app.entity.PropertyImage;
import propertyservice.app.exeption.PropertyNotFoundException;
import propertyservice.app.repository.PropertyRepository;
import propertyservice.app.client.AgentServiceClient;
import propertyservice.app.client.CityServiceClient;
import propertyservice.app.client.PropertyTypeServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AgentServiceClient agentServiceClient;
    private final CityServiceClient cityServiceClient;
    private final PropertyTypeServiceClient propertyTypeServiceClient;

    @Transactional(readOnly = true)
    public List<PropertyDto> getAllProperties() {
        log.debug("Fetching all properties");
        return convertToDtoList(propertyRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PropertyDto getPropertyById(UUID id) {
        log.debug("Fetching property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        return convertToDto(property);
    }

    @Transactional
    public PropertyDto createProperty(PropertyCreateDto dto) {
        log.info("Creating new property with title: {}", dto.getTitle());
        
        // Validate foreign keys
        validateAgent(dto.getAgentId());
        validateCity(dto.getCityId());
        validatePropertyType(dto.getPropertyTypeId());

        // Create property
        Property property = Property.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .agentId(dto.getAgentId())
                .cityId(dto.getCityId())
                .propertyTypeId(dto.getPropertyTypeId())
                .status(dto.getStatus())
                .bedrooms(dto.getBedrooms())
                .bathrooms(dto.getBathrooms())
                .squareFeet(dto.getSquareFeet())
                .address(dto.getAddress())
                .isFeatured(false)
                .build();

        // Lists are initialized via @Builder.Default in Property entity

        // Add features
        addFeaturesToProperty(property, dto.getFeatures());

        // Add images
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            log.debug("Adding {} images to property", dto.getImageUrls().size());
            for (int i = 0; i < dto.getImageUrls().size(); i++) {
                String imageUrl = dto.getImageUrls().get(i);
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    PropertyImage image = PropertyImage.builder()
                            .property(property)
                            .imageUrl(imageUrl.trim())
                            .isPrimary(i == 0) // First image is primary
                            .displayOrder(i)
                            .build();
                    property.getImages().add(image);
                }
            }
        }

        // Save property 
        Property savedProperty = propertyRepository.save(property);
        log.info("Property created successfully with id: {}", savedProperty.getId());
        
        return convertToDto(reloadPropertyWithRelations(savedProperty.getId()));
    }

    @Transactional
    public PropertyDto updateProperty(UUID id, PropertyUpdateDto dto) {
        log.info("Updating property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));

        // Validate and update foreign keys if provided
        if (dto.getAgentId() != null) {
            validateAgent(dto.getAgentId());
            property.setAgentId(dto.getAgentId());
        }
        if (dto.getCityId() != null) {
            validateCity(dto.getCityId());
            property.setCityId(dto.getCityId());
        }
        if (dto.getPropertyTypeId() != null) {
            validatePropertyType(dto.getPropertyTypeId());
            property.setPropertyTypeId(dto.getPropertyTypeId());
        }

        // Update fields
        if (dto.getTitle() != null) property.setTitle(dto.getTitle());
        if (dto.getDescription() != null) property.setDescription(dto.getDescription());
        if (dto.getPrice() != null) property.setPrice(dto.getPrice());
        if (dto.getStatus() != null) property.setStatus(dto.getStatus());
        if (dto.getBedrooms() != null) property.setBedrooms(dto.getBedrooms());
        if (dto.getBathrooms() != null) property.setBathrooms(dto.getBathrooms());
        if (dto.getSquareFeet() != null) property.setSquareFeet(dto.getSquareFeet());
        if (dto.getAddress() != null) property.setAddress(dto.getAddress());

        // Update features
        if (dto.getFeatures() != null) {
            property.getFeatures().clear();
            addFeaturesToProperty(property, dto.getFeatures());
        }

        Property updatedProperty = propertyRepository.save(property);
        return convertToDto(reloadPropertyWithRelations(updatedProperty.getId()));
    }

    @Transactional
    public void deleteProperty(UUID id) {
        log.info("Deleting property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        propertyRepository.delete(property);
        log.info("Property deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getFeaturedProperties() {
        log.debug("Fetching featured properties");
        return convertToDtoList(propertyRepository.findByIsFeaturedTrue());
    }

    @Transactional
    public void toggleFeatured(UUID id) {
        log.info("Toggling featured status for property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        property.setIsFeatured(!property.getIsFeatured());
        propertyRepository.save(property);
        log.info("Property featured status updated to: {}", property.getIsFeatured());
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> searchProperties(String search, UUID cityId, UUID propertyTypeId, BigDecimal maxPrice) {
        log.debug("Searching properties with search: {}, cityId: {}, propertyTypeId: {}, maxPrice: {}", 
                search, cityId, propertyTypeId, maxPrice);
        return convertToDtoList(propertyRepository.searchProperties(search, cityId, propertyTypeId, maxPrice));
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getPropertiesByAgent(UUID agentId) {
        log.debug("Fetching properties for agent with id: {}", agentId);
        return convertToDtoList(propertyRepository.findByAgentId(agentId));
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getPropertiesByCity(UUID cityId) {
        log.debug("Fetching properties for city with id: {}", cityId);
        return convertToDtoList(propertyRepository.findByCityId(cityId));
    }

    private PropertyDto convertToDto(Property property) {
        // Extract image URLs - collections are already loaded via fetch joins
        List<String> imageUrls = new ArrayList<>();
        if (property.getImages() != null) {
            imageUrls = property.getImages().stream()
                    .map(PropertyImage::getImageUrl)
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        // Extract features - collections are already loaded via fetch joins
        List<String> features = new ArrayList<>();
        if (property.getFeatures() != null) {
            features = property.getFeatures().stream()
                    .map(PropertyFeature::getFeatureName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        return PropertyDto.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .price(property.getPrice())
                .agentId(property.getAgentId())
                .cityId(property.getCityId())
                .propertyTypeId(property.getPropertyTypeId())
                .status(property.getStatus())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .squareFeet(property.getSquareFeet())
                .address(property.getAddress())
                .isFeatured(property.getIsFeatured())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .imageUrls(imageUrls)
                .features(features)
                .build();
    }

    // Helper methods to reduce code duplication
    
    private List<PropertyDto> convertToDtoList(List<Property> properties) {
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private Property reloadPropertyWithRelations(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
    }
    
    private void validateAgent(UUID agentId) {
        Boolean agentExists = agentServiceClient.agentExists(agentId);
        if (Boolean.FALSE.equals(agentExists)) {
            log.warn("Agent not found with id: {}", agentId);
            throw new RuntimeException("Agent not found with id: " + agentId);
        }
    }
    
    private void validateCity(UUID cityId) {
        Boolean cityExists = cityServiceClient.cityExists(cityId);
        if (Boolean.FALSE.equals(cityExists)) {
            log.warn("City not found with id: {}", cityId);
            throw new RuntimeException("City not found with id: " + cityId);
        }
    }
    
    private void validatePropertyType(UUID propertyTypeId) {
        Boolean propertyTypeExists = propertyTypeServiceClient.propertyTypeExists(propertyTypeId);
        if (Boolean.FALSE.equals(propertyTypeExists)) {
            log.warn("Property type not found with id: {}", propertyTypeId);
            throw new RuntimeException("Property type not found with id: " + propertyTypeId);
        }
    }
    
    private void addFeaturesToProperty(Property property, List<String> features) {
        if (features != null && !features.isEmpty()) {
            log.debug("Adding {} features to property", features.size());
            for (String featureName : features) {
                PropertyFeature feature = PropertyFeature.builder()
                        .property(property)
                        .featureName(featureName)
                        .build();
                property.getFeatures().add(feature);
            }
        }
    }
}

