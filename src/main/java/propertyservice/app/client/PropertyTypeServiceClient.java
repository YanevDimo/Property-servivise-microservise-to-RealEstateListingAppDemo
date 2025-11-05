package propertyservice.app.client;

import propertyservice.app.dto.PropertyTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "property-type-service", url = "${property-type.service.url}")
public interface PropertyTypeServiceClient {

    @GetMapping("/api/v1/property-types/{typeId}")
    PropertyTypeDto getPropertyType(@PathVariable UUID typeId);

    @GetMapping("/api/v1/property-types/{typeId}/exists")
    Boolean propertyTypeExists(@PathVariable UUID typeId);
}


