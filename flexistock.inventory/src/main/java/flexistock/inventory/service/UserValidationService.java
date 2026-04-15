package flexistock.inventory.service;

import flexistock.inventory.dto.response.UserValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserValidationService {
    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);

    private final RestClient restClient;
    private final String usersServiceBaseUrl;

    public UserValidationService(RestClient restClient, @Value("${users.service.base-url}") String usersServiceBaseUrl) {
        this.restClient = restClient;
        this.usersServiceBaseUrl = usersServiceBaseUrl;
    }

    public AuthenticatedUser validate(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("Authentication failed because X-Auth-Token header was missing or blank");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Auth-Token header is required");
        }

        logger.debug("Validating user token with users service baseUrl={}", usersServiceBaseUrl);
        UserValidationResponse response = restClient.get()
                .uri(usersServiceBaseUrl + "/auth/validate")
                .header("X-Auth-Token", token)
                .retrieve()
                .body(UserValidationResponse.class);

        if (response == null || response.id() == null) {
            logger.error("Authentication failed because users service returned an invalid response");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication response");
        }

        logger.debug("Validated user userId={} role={}", response.id(), response.role());
        return new AuthenticatedUser(response.id(), response.name(), response.email(), response.role(), token);
    }
}
