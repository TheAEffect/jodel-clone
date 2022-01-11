package de.hse.jodel.controller;

import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class AuthController {
    
    @Inject
    SessionController sessionController;

    private static final Logger LOGGER = Logger.getLogger(AuthController.class);

    /**
     * Creates a session cookie with the token as value and default max age -1
     *
     * @param token Session token
     * @return NewCookie
     */
    private NewCookie buildResponseCookie(String token) {
        return buildResponseCookie(token, NewCookie.DEFAULT_MAX_AGE);
    }

    /**
     * Creates a new session cookie
     *
     * @param token Session token
     * @return NewCookie
     */
    public NewCookie buildResponseCookie(String token, int maxAge) {

        return new NewCookie(
                "jodel-session",
                token,
                "/",
                "localhost",
                "",
                maxAge,
                false,
                true
        );
    }

    /**
     * Login
     *
     * @param email String username
     * @param password String password
     * @param request  HttpServerRequest
     * @return Response
     */
    public Response login(String email, String password, HttpServerRequest request) {
        LOGGER.debug("Login is executed");

        User user = User.findByEmail(email);

        if (user != null && BCrypt.checkpw(password, user.password)) {
            Session session = sessionController.createNewSession(user, request);
            return Response.status(Response.Status.OK).cookie(buildResponseCookie(session.token)).entity(user).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Email oder Passwort falsch").build();
    }

    /**
     * Logs the user out
     *
     * @param token The session token to remove the session
     * @return Response
     */
    public Response logout(String token) {
        try {
            sessionController.removeSession(token);
        } catch (Exception exception) {
            LOGGER.error(exception.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.OK).cookie(buildResponseCookie(token, 0)).entity("Logout successful").build();
    }
}
