package propertyservice.app.repository;

import propertyservice.app.entity.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    
    // Override findAll with fetch join
    @EntityGraph(attributePaths = {"images", "features"})
    @Override
    List<Property> findAll();
    
    // Override findById with fetch join
    @EntityGraph(attributePaths = {"images", "features"})
    @Override
    Optional<Property> findById(UUID id);
    
    @EntityGraph(attributePaths = {"images", "features"})
    List<Property> findByAgentId(UUID agentId);
    
    @EntityGraph(attributePaths = {"images", "features"})
    List<Property> findByCityId(UUID cityId);
    
    @EntityGraph(attributePaths = {"images", "features"})
    List<Property> findByIsFeaturedTrue();
    
    @EntityGraph(attributePaths = {"images", "features"})
    @Query("SELECT p FROM Property p WHERE " +
           "(:search IS NULL OR p.title LIKE CONCAT('%', :search, '%') OR p.description LIKE CONCAT('%', :search, '%')) AND " +
           "(:cityId IS NULL OR p.cityId = :cityId) AND " +
           "(:propertyTypeId IS NULL OR p.propertyTypeId = :propertyTypeId) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Property> searchProperties(
            @Param("search") String search,
            @Param("cityId") UUID cityId,
            @Param("propertyTypeId") UUID propertyTypeId,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
