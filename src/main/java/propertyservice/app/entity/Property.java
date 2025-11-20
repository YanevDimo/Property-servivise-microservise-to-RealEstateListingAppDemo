package propertyservice.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"images", "features"})
@EqualsAndHashCode(exclude = {"images", "features"})
public class Property {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(name = "agent_id", nullable = false)
    private UUID agentId;  // Changed from @ManyToOne Agent
    
    @Column(name = "city_id", nullable = false)
    private UUID cityId;  // Changed from @ManyToOne City
    
    @Column(name = "property_type_id", nullable = false)
    private UUID propertyTypeId;  // Changed from @ManyToOne PropertyType
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;
    
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer squareFeet;
    private String address;
    
    @Builder.Default
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<PropertyImage> images = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<PropertyFeature> features = new ArrayList<>();
    
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}