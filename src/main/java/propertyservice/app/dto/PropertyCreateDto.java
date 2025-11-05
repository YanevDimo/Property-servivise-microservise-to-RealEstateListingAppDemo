package propertyservice.app.dto;

import propertyservice.app.entity.PropertyStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCreateDto {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Agent ID is required")
    private UUID agentId;
    
    @NotNull(message = "City ID is required")
    private UUID cityId;
    
    @NotNull(message = "Property Type ID is required")
    private UUID propertyTypeId;
    
    @Builder.Default
    private PropertyStatus status = PropertyStatus.DRAFT;
    
    @Min(value = 0, message = "Bedrooms must be 0 or greater")
    private Integer bedrooms;
    
    @Min(value = 0, message = "Bathrooms must be 0 or greater")
    private Integer bathrooms;
    
    @Min(value = 0, message = "Square feet must be 0 or greater")
    private Integer squareFeet;
    
    private String address;
    
    private List<String> features;
    
    private List<String> imageUrls; // Image URLs to associate with the property
}

