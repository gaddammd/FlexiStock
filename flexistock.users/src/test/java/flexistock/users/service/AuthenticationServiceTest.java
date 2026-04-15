package flexistock.users.service;

import flexistock.users.dto.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private LoginHistoryService loginHistoryService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void signupAllowsFacultyEmailDomain() {
        User createdUser = new User(UUID.randomUUID(), "Test User", "person@mail.uc.edu", "secret", "USER", false);
        when(userService.createUser("Test User", "person@mail.uc.edu", "secret")).thenReturn(createdUser);

        User result = authenticationService.signup("Test User", "person@mail.uc.edu", "secret");

        assertEquals(createdUser, result);
        verify(userService).createUser("Test User", "person@mail.uc.edu", "secret");
    }

    @Test
    void signupAllowsStudentEmailDomainIgnoringCaseAndWhitespace() {
        User createdUser = new User(UUID.randomUUID(), "Test User", " Person@UCMail.UC.EDU ", "secret", "USER", false);
        when(userService.createUser("Test User", " Person@UCMail.UC.EDU ", "secret")).thenReturn(createdUser);

        User result = authenticationService.signup("Test User", " Person@UCMail.UC.EDU ", "secret");

        assertEquals(createdUser, result);
        verify(userService).createUser("Test User", " Person@UCMail.UC.EDU ", "secret");
    }

    @Test
    void signupRejectsNonUcEmailDomain() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.signup("Test User", "person@example.com", "secret")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email must end with @mail.uc.edu or @ucmail.uc.edu", exception.getReason());
    }
}
