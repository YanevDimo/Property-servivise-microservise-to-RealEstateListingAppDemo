package propertyservice.app.client;

import propertyservice.app.dto.AgentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "agent-service", url = "${agent.service.url}")
public interface AgentServiceClient {

    @GetMapping("/api/v1/agents/{agentId}")
    AgentDto getAgent(@PathVariable UUID agentId);

    @GetMapping("/api/v1/agents/{agentId}/exists")
    Boolean agentExists(@PathVariable UUID agentId);
}


