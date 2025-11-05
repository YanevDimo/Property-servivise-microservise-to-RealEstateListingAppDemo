package propertyservice.app.repository;

import propertyservice.app.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {
    
    List<PropertyImage> findByProperty_Id(UUID propertyId);
    
    List<PropertyImage> findByProperty_IdOrderByDisplayOrderAsc(UUID propertyId);
}
