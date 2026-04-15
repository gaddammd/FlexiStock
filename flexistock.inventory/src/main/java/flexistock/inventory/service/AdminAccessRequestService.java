package flexistock.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AdminAccessRequestService {
    private static final Logger logger = LoggerFactory.getLogger(AdminAccessRequestService.class);

    private final RestClient restClient;
    private final String usersServiceBaseUrl;

    public AdminAccessRequestService(RestClient restClient, @Value("${users.service.base-url}") String usersServiceBaseUrl) {
        this.restClient = restClient;
        this.usersServiceBaseUrl = usersServiceBaseUrl;
    }

    public ResponseEntity<String> requestAdminAccess(String token, Map<String, Object> payload) {
        logger.debug("Forwarding admin access request to users service baseUrl={}", usersServiceBaseUrl);

        RestClient.RequestBodySpec request = restClient.post()
                .uri(usersServiceBaseUrl + "/request-admin-access")
                .contentType(MediaType.APPLICATION_JSON);

        if (token != null && !token.isBlank()) {
            request.header("X-Auth-Token", token);
        }

        return request
                .body(payload == null ? Map.of() : payload)
                .retrieve()
                .toEntity(String.class);
    }
}
