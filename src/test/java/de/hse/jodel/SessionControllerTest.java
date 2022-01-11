package de.hse.jodel;

import de.hse.jodel.controller.SessionController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

@QuarkusTest
public class SessionControllerTest {

    @Inject
    SessionController sessionController;

    @Inject
    UserController userController;

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);

    @BeforeEach
    public void clearAllFromDatabase() {
        sessionController.removeAllSessions();
        userController.removeAllUsers();
    }

    private User createUser() {
        try {
            return userController.createUser("password","password", "first@one.de", "admin");
        } catch (HttpExceptions httpExceptions) {
            httpExceptions.printStackTrace();
        }
        return null;
    }

    private void requestMocking() {
        when(request.remoteAddress()).thenReturn(address);
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
    }

    private void addressMocking() {
        when(address.host()).thenReturn("127.0.0.1");
        when(address.port()).thenReturn(8080);
        when(address.path()).thenReturn("");
    }

    @Test
    public void createNewSessionTest() {
        User user = createUser();

        addressMocking();

        requestMocking();

        String token = user.email + user.password + request.remoteAddress() + request.getHeader("User-Agent");

        Session session = sessionController.createNewSession(user, request);

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        assertTrue(BCrypt.checkpw(token, session.token));
        assertNotNull(session.createdAt);
        assertNotNull(session.lastUsed);
    }

    @Test
    public void updateSessionTest() {
        User user = createUser();

        addressMocking();

        requestMocking();

        String token = user.email + user.password + request.remoteAddress() + request.getHeader("User-Agent");

        Session session = sessionController.createNewSession(user, request);

        Session updated = sessionController.updateSession(session.token);

        assertNotEquals(session.lastUsed, updated.lastUsed);

        assertTrue((updated.lastUsed.compareTo(session.lastUsed) >= 0));

    }
}
