package propertyservice.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import propertyservice.app.dto.PropertyCreateDto;
import propertyservice.app.dto.PropertyDto;
import propertyservice.app.dto.PropertyUpdateDto;
import propertyservice.app.entity.PropertyStatus;
import propertyservice.app.exeption.PropertyNotFoundException;
import propertyservice.app.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyRestController.class)
class PropertyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID propertyId = UUID.randomUUID();
    private final UUID agentId = UUID.randomUUID();
    private final UUID cityId = UUID.randomUUID();
    private final UUID propertyTypeId = UUID.randomUUID();

    @Test
    void getAllProperties_ShouldReturn200() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        List<PropertyDto> properties = Collections.singletonList(propertyDto);
        
        when(propertyService.getAllProperties()).thenReturn(properties);

        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(propertyId.toString()));

        verify(propertyService, times(1)).getAllProperties();
    }

    @Test
    void getAllProperties_WithSearchParams_ShouldCallSearchProperties() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        List<PropertyDto> properties = Collections.singletonList(propertyDto);
        
        when(propertyService.searchProperties(eq("test"), eq(cityId), eq(propertyTypeId), any(BigDecimal.class)))
                .thenReturn(properties);

        mockMvc.perform(get("/api/v1/properties")
                        .param("search", "test")
                        .param("cityId", cityId.toString())
                        .param("propertyTypeId", propertyTypeId.toString())
                        .param("maxPrice", "100000"))
                .andExpect(status().isOk());

        verify(propertyService, times(1)).searchProperties(eq("test"), eq(cityId), eq(propertyTypeId), any(BigDecimal.class));
    }

    @Test
    void getPropertyById_WhenExists_ShouldReturn200() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        
        when(propertyService.getPropertyById(propertyId)).thenReturn(propertyDto);

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propertyId.toString()))
                .andExpect(jsonPath("$.title").value("Test Property"));

        verify(propertyService, times(1)).getPropertyById(propertyId);
    }

    @Test
    void getPropertyById_WhenNotExists_ShouldReturn404() throws Exception {
        when(propertyService.getPropertyById(propertyId))
                .thenThrow(new PropertyNotFoundException("Property not found"));

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNotFound());

        verify(propertyService, times(1)).getPropertyById(propertyId);
    }

    @Test
    void createProperty_WhenValid_ShouldReturn201() throws Exception {
        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("New Property")
                .description("Description")
                .price(new BigDecimal("200000"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(3)
                .bathrooms(2)
                .squareFeet(1500)
                .address("123 Test St")
                .build();

        PropertyDto propertyDto = createPropertyDto();
        
        when(propertyService.createProperty(any(PropertyCreateDto.class))).thenReturn(propertyDto);

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(propertyId.toString()));

        verify(propertyService, times(1)).createProperty(any(PropertyCreateDto.class));
    }

    @Test
    void createProperty_WhenInvalid_ShouldReturn400() throws Exception {
        PropertyCreateDto createDto = PropertyCreateDto.builder()
                .title("")
                .price(null)
                .build();

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProperty_WhenValid_ShouldReturn200() throws Exception {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Updated Property")
                .price(new BigDecimal("250000"))
                .build();

        PropertyDto propertyDto = createPropertyDto();
        propertyDto.setTitle("Updated Property");
        
        when(propertyService.updateProperty(eq(propertyId), any(PropertyUpdateDto.class))).thenReturn(propertyDto);

        mockMvc.perform(put("/api/v1/properties/{id}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Property"));

        verify(propertyService, times(1)).updateProperty(eq(propertyId), any(PropertyUpdateDto.class));
    }

    @Test
    void updateProperty_WhenNotExists_ShouldReturn404() throws Exception {
        PropertyUpdateDto updateDto = PropertyUpdateDto.builder()
                .title("Updated Property")
                .build();

        when(propertyService.updateProperty(eq(propertyId), any(PropertyUpdateDto.class)))
                .thenThrow(new PropertyNotFoundException("Property not found"));

        mockMvc.perform(put("/api/v1/properties/{id}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProperty_WhenExists_ShouldReturn204() throws Exception {
        doNothing().when(propertyService).deleteProperty(propertyId);

        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNoContent());

        verify(propertyService, times(1)).deleteProperty(propertyId);
    }

    @Test
    void deleteProperty_WhenNotExists_ShouldReturn404() throws Exception {
        doThrow(new PropertyNotFoundException("Property not found"))
                .when(propertyService).deleteProperty(propertyId);

        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFeaturedProperties_ShouldReturn200() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        propertyDto.setIsFeatured(true);
        List<PropertyDto> properties = List.of(propertyDto);
        
        when(propertyService.getFeaturedProperties()).thenReturn(properties);

        mockMvc.perform(get("/api/v1/properties/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isFeatured").value(true));

        verify(propertyService, times(1)).getFeaturedProperties();
    }

    @Test
    void toggleFeatured_ShouldReturn200() throws Exception {
        doNothing().when(propertyService).toggleFeatured(propertyId);

        mockMvc.perform(put("/api/v1/properties/{id}/feature", propertyId))
                .andExpect(status().isOk());

        verify(propertyService, times(1)).toggleFeatured(propertyId);
    }

    @Test
    void getPropertiesByAgent_ShouldReturn200() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        List<PropertyDto> properties = Collections.singletonList(propertyDto);
        
        when(propertyService.getPropertiesByAgent(agentId)).thenReturn(properties);

        mockMvc.perform(get("/api/v1/properties/agent/{agentId}", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].agentId").value(agentId.toString()));

        verify(propertyService, times(1)).getPropertiesByAgent(agentId);
    }

    @Test
    void getPropertiesByCity_ShouldReturn200() throws Exception {
        PropertyDto propertyDto = createPropertyDto();
        List<PropertyDto> properties = Collections.singletonList(propertyDto);
        
        when(propertyService.getPropertiesByCity(cityId)).thenReturn(properties);

        mockMvc.perform(get("/api/v1/properties/city/{cityId}", cityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].cityId").value(cityId.toString()));

        verify(propertyService, times(1)).getPropertiesByCity(cityId);
    }

    private PropertyDto createPropertyDto() {
        return PropertyDto.builder()
                .id(propertyId)
                .title("Test Property")
                .description("Test Description")
                .price(new BigDecimal("100000"))
                .agentId(agentId)
                .cityId(cityId)
                .propertyTypeId(propertyTypeId)
                .status(PropertyStatus.FOR_SALE)
                .bedrooms(3)
                .bathrooms(2)
                .squareFeet(1500)
                .address("123 Test St")
                .isFeatured(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

