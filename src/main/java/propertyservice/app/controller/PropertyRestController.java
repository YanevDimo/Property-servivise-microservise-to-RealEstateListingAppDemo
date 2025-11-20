package propertyservice.app.controller;

import propertyservice.app.dto.PropertyCreateDto;
import propertyservice.app.dto.PropertyDto;
import propertyservice.app.dto.PropertyUpdateDto;
import propertyservice.app.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static propertyservice.app.controller.ResponseHelper.*;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyRestController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<List<PropertyDto>> getAllProperties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) UUID propertyTypeId,
            @RequestParam(required = false) Double maxPrice) {
        
        if (search != null || cityId != null || propertyTypeId != null || maxPrice != null) {
            BigDecimal maxPriceDecimal = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
            return ok(propertyService.searchProperties(search, cityId, propertyTypeId, maxPriceDecimal));
        }
        
        return ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto> getPropertyById(@PathVariable UUID id) {
        return ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    public ResponseEntity<PropertyDto> createProperty(@Valid @RequestBody PropertyCreateDto dto) {
        return created(propertyService.createProperty(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyDto> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody PropertyUpdateDto dto) {
        return ok(propertyService.updateProperty(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable UUID id) {
        propertyService.deleteProperty(id);
        return noContent();
    }

    @GetMapping("/featured")
    public ResponseEntity<List<PropertyDto>> getFeaturedProperties() {
        return ok(propertyService.getFeaturedProperties());
    }

    @PutMapping("/{id}/feature")
    public ResponseEntity<Void> toggleFeatured(@PathVariable UUID id) {
        propertyService.toggleFeatured(id);
        return ok();
    }


    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<PropertyDto>> getPropertiesByAgent(@PathVariable UUID agentId) {
        return ok(propertyService.getPropertiesByAgent(agentId));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<PropertyDto>> getPropertiesByCity(@PathVariable UUID cityId) {
        return ok(propertyService.getPropertiesByCity(cityId));
    }
}
