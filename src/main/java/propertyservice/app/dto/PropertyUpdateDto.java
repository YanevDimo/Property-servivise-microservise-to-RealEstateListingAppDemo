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
public class PropertyUpdateDto {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private UUID agentId;
    private UUID cityId;
    private UUID propertyTypeId;
    private PropertyStatus status;
    
    @Min(value = 0, message = "Bedrooms must be 0 or greater")
    private Integer bedrooms;
    
    @Min(value = 0, message = "Bathrooms must be 0 or greater")
    private Integer bathrooms;
    
    @Min(value = 0, message = "Square feet must be 0 or greater")
    private Integer squareFeet;
    
    private String address;
    private List<String> features;
}


