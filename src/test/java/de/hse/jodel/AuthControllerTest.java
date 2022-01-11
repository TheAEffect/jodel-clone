package de.hse.jodel;

import de.hse.jodel.controller.SessionController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.controller.AuthController;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthControllerTest {

    @Inject
    AuthController authController;

    @Inject
    SessionController sessionController;

    @Inject
    UserController userController;


    @BeforeEach
    public void clearAllFromDatabase() {
        sessionController.removeAllSessions();
        userController.removeAllUsers();
    }

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);

    private void requestMocking() {
        when(request.remoteAddress()).thenReturn(address);
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
    }

    private void addressMocking() {
        when(address.host()).thenReturn("127.0.0.1");
        when(address.port()).thenReturn(8080);
        when(address.path()).thenReturn("");
    }

    private User createUser() {
        try {
            return userController.createUser("password", "password", "first@one.de", "admin");
        } catch (HttpExceptions httpExceptions) {
            httpExceptions.printStackTrace();
        }
        return null;
    }

    @Test
    public void loginSuccessTest() {
        User user = createUser();
        addressMocking();
        requestMocking();

        Response response = authController.login(user.email, "password", request);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void loginFailureTest() {
        User user = createUser();
        addressMocking();
        requestMocking();

        Response response = authController.login(user.email, "assword", request);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void logoutTest() {
        User user = createUser();
        addressMocking();
        requestMocking();

        Response response = authController.login(user.email, "password", request);

        Response logout = authController.logout(response.getCookies().get("jodel-session").getValue());
        assertEquals(200, logout.getStatus());
    }

}
