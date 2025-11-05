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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AgentServiceClient agentServiceClient;
    private final CityServiceClient cityServiceClient;
    private final PropertyTypeServiceClient propertyTypeServiceClient;

    @Transactional(readOnly = true)
    public List<PropertyDto> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        
        // Force initialization of lazy-loaded collections for all properties
        for (Property property : properties) {
            if (property.getImages() != null) {
                property.getImages().size(); // Force initialization
            }
            if (property.getFeatures() != null) {
                property.getFeatures().size(); // Force initialization
            }
        }
        
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyDto getPropertyById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        
        // Force initialization of lazy-loaded collections
        if (property.getImages() != null) {
            property.getImages().size(); // Force initialization
        }
        if (property.getFeatures() != null) {
            property.getFeatures().size(); // Force initialization
        }
        
        return convertToDto(property);
    }

    @Transactional
    public PropertyDto createProperty(PropertyCreateDto dto) {
        // Validate agent exists
        Boolean agentExists = agentServiceClient.agentExists(dto.getAgentId());
        if (Boolean.FALSE.equals(agentExists)) {
            throw new RuntimeException("Agent not found with id: " + dto.getAgentId());
        }

        // Validate city exists
        Boolean cityExists = cityServiceClient.cityExists(dto.getCityId());
        if (Boolean.FALSE.equals(cityExists)) {
            throw new RuntimeException("City not found with id: " + dto.getCityId());
        }

        // Validate property type exists
        Boolean propertyTypeExists = propertyTypeServiceClient.propertyTypeExists(dto.getPropertyTypeId());
        if (Boolean.FALSE.equals(propertyTypeExists)) {
            throw new RuntimeException("Property type not found with id: " + dto.getPropertyTypeId());
        }

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

        // Ensure lists are initialized (important for @Builder)
        if (property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }
        if (property.getFeatures() == null) {
            property.setFeatures(new ArrayList<>());
        }

        // Add features
        if (dto.getFeatures() != null && !dto.getFeatures().isEmpty()) {
            for (String featureName : dto.getFeatures()) {
                PropertyFeature feature = PropertyFeature.builder()
                        .property(property)
                        .featureName(featureName)
                        .build();
                property.getFeatures().add(feature);
            }
        }

        // Add images
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            System.out.println("=== IMAGE CREATION DEBUG ===");
            System.out.println("Creating property with " + dto.getImageUrls().size() + " images: " + dto.getImageUrls());
            System.out.println("Property images list before: " + (property.getImages() == null ? "NULL" : property.getImages().size()));
            
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
                    System.out.println("✓ Added image " + (i + 1) + " to property: " + imageUrl);
                } else {
                    System.out.println("✗ Skipping empty image URL at index " + i);
                }
            }
            System.out.println("Property images list after: " + property.getImages().size());
            System.out.println("=== END IMAGE DEBUG ===");
        } else {
            System.out.println("⚠️ No image URLs provided in PropertyCreateDto");
            System.out.println("dto.getImageUrls() = " + dto.getImageUrls());
        }

        // Save property (cascade should save images)
        System.out.println("Saving property with " + property.getImages().size() + " images...");
        System.out.println("Images list before save: " + property.getImages());
        Property savedProperty = propertyRepository.saveAndFlush(property); // Use saveAndFlush to ensure immediate persistence
        System.out.println("Property saved with ID: " + savedProperty.getId());
        
        // Verify images were saved
        if (savedProperty.getImages() != null) {
            System.out.println("Saved property has " + savedProperty.getImages().size() + " images in memory");
            savedProperty.getImages().forEach(img -> 
                System.out.println("  - Image: " + img.getImageUrl() + " (ID: " + img.getId() + ")")
            );
        } else {
            System.out.println("⚠️ Saved property has NULL images collection!");
        }
        
        // Force initialization of images collection to ensure they're loaded
        // This is needed because @OneToMany uses LAZY fetching by default
        if (savedProperty.getImages() != null) {
            savedProperty.getImages().size(); // Force initialization
        }
        
        return convertToDto(savedProperty);
    }

    @Transactional
    public PropertyDto updateProperty(UUID id, PropertyUpdateDto dto) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));

        // Validate foreign keys if provided
        if (dto.getAgentId() != null) {
            Boolean agentExists = agentServiceClient.agentExists(dto.getAgentId());
            if (Boolean.FALSE.equals(agentExists)) {
                throw new RuntimeException("Agent not found with id: " + dto.getAgentId());
            }
            property.setAgentId(dto.getAgentId());
        }

        if (dto.getCityId() != null) {
            Boolean cityExists = cityServiceClient.cityExists(dto.getCityId());
            if (Boolean.FALSE.equals(cityExists)) {
                throw new RuntimeException("City not found with id: " + dto.getCityId());
            }
            property.setCityId(dto.getCityId());
        }

        if (dto.getPropertyTypeId() != null) {
            Boolean propertyTypeExists = propertyTypeServiceClient.propertyTypeExists(dto.getPropertyTypeId());
            if (Boolean.FALSE.equals(propertyTypeExists)) {
                throw new RuntimeException("Property type not found with id: " + dto.getPropertyTypeId());
            }
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
            for (String featureName : dto.getFeatures()) {
                PropertyFeature feature = PropertyFeature.builder()
                        .property(property)
                        .featureName(featureName)
                        .build();
                property.getFeatures().add(feature);
            }
        }

        Property updatedProperty = propertyRepository.save(property);
        
        // Force initialization of lazy-loaded collections
        if (updatedProperty.getImages() != null) {
            updatedProperty.getImages().size(); // Force initialization
        }
        if (updatedProperty.getFeatures() != null) {
            updatedProperty.getFeatures().size(); // Force initialization
        }
        
        return convertToDto(updatedProperty);
    }

    @Transactional
    public void deleteProperty(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        propertyRepository.delete(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getFeaturedProperties() {
        List<Property> properties = propertyRepository.findByIsFeaturedTrue();
        
        // Force initialization of lazy-loaded collections
        for (Property property : properties) {
            if (property.getImages() != null) {
                property.getImages().size(); // Force initialization
            }
            if (property.getFeatures() != null) {
                property.getFeatures().size(); // Force initialization
            }
        }
        
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleFeatured(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + id));
        property.setIsFeatured(!property.getIsFeatured());
        propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> searchProperties(String search, UUID cityId, UUID propertyTypeId, BigDecimal maxPrice) {
        List<Property> properties = propertyRepository.searchProperties(search, cityId, propertyTypeId, maxPrice);
        
        // Force initialization of lazy-loaded collections
        for (Property property : properties) {
            if (property.getImages() != null) {
                property.getImages().size(); // Force initialization
            }
            if (property.getFeatures() != null) {
                property.getFeatures().size(); // Force initialization
            }
        }
        
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getPropertiesByAgent(UUID agentId) {
        List<Property> properties = propertyRepository.findByAgentId(agentId);
        
        // Force initialization of lazy-loaded collections
        for (Property property : properties) {
            if (property.getImages() != null) {
                property.getImages().size(); // Force initialization
            }
            if (property.getFeatures() != null) {
                property.getFeatures().size(); // Force initialization
            }
        }
        
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> getPropertiesByCity(UUID cityId) {
        List<Property> properties = propertyRepository.findByCityId(cityId);
        
        // Force initialization of lazy-loaded collections
        for (Property property : properties) {
            if (property.getImages() != null) {
                property.getImages().size(); // Force initialization
            }
            if (property.getFeatures() != null) {
                property.getFeatures().size(); // Force initialization
            }
        }
        
        return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PropertyDto convertToDto(Property property) {
        // Ensure images collection is initialized (handle lazy loading)
        List<String> imageUrls = new ArrayList<>();
        if (property.getImages() != null) {
            try {
                imageUrls = property.getImages().stream()
                        .map(PropertyImage::getImageUrl)
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // If lazy loading fails, log and return empty list
                System.err.println("Error loading images for property " + property.getId() + ": " + e.getMessage());
                imageUrls = new ArrayList<>();
            }
        }

        List<String> features = property.getFeatures().stream()
                .map(PropertyFeature::getFeatureName)
                .collect(Collectors.toList());

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
}

