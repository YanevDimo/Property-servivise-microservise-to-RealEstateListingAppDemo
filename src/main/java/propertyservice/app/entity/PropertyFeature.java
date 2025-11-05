package propertyservice.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "property_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"property"})
@EqualsAndHashCode(exclude = {"property"})
public class PropertyFeature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    
    @Column(nullable = false)
    private String featureName;
    
    private String description;
}

