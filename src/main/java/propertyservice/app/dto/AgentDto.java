package propertyservice.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}



