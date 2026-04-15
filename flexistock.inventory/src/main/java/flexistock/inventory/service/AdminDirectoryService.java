package flexistock.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminDirectoryService {
    private static final Logger logger = LoggerFactory.getLogger(AdminDirectoryService.class);

    private final RestClient restClient;
    private final String usersServiceBaseUrl;

    public AdminDirectoryService(RestClient restClient, @Value("${users.service.base-url}") String usersServiceBaseUrl) {
        this.restClient = restClient;
        this.usersServiceBaseUrl = usersServiceBaseUrl;
    }

    public List<String> getAdminEmails(String token) {
        logger.debug("Fetching admin email recipients from users service baseUrl={}", usersServiceBaseUrl);
        String[] emails = restClient.get()
                .uri(usersServiceBaseUrl + "/users/admin-emails")
                .header("X-Auth-Token", token)
                .retrieve()
                .body(String[].class);

        List<String> recipients = emails == null ? List.of() : Arrays.stream(emails).toList();
        logger.debug("Resolved {} admin email recipients", recipients.size());
        return recipients;
    }
}
