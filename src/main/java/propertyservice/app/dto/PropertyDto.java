package propertyservice.app.dto;

import propertyservice.app.entity.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private UUID agentId;
    private UUID cityId;
    private UUID propertyTypeId;
    private PropertyStatus status;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer squareFeet;
    private String address;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private List<String> features;
}



