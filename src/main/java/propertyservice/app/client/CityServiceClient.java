package propertyservice.app.client;

import propertyservice.app.dto.CityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "city-service", url = "${city.service.url}")
public interface CityServiceClient {

    @GetMapping("/api/v1/cities/{cityId}")
    CityDto getCity(@PathVariable UUID cityId);

    @GetMapping("/api/v1/cities/{cityId}/exists")
    Boolean cityExists(@PathVariable UUID cityId);
}



