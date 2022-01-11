package de.hse.jodel.rest;

import de.hse.jodel.controller.AuthController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthController authController;

    @Inject
    UserController userController;

    @Inject
    AuthUser authUser;

    @Context
    HttpServerRequest request;

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class);

    /**
     * Tests login
     * @param data object data (username, password)
     * @return Response
     */
    @POST
    @PermitAll
    @Path("login")
    public Response login(JsonObject data) {
        LOGGER.debug("Login is entered");

        return authController.login(data.getString("username"), data.getString("password"), request);
    }

    /**
     * Register
     * @param data object data (username, password, password_confirm, email)
     * @return Response
     * @throws HttpExceptions error
     */
    @POST
    @PermitAll
    @Path("register")
    public Response response(JsonObject data) throws HttpExceptions {
        LOGGER.debug("Register is entered");

        if (data.getString("password") == null || data.getString("password_confirm") == null || data.getString("email") == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        User user = userController.createUser(data.getString("password"), data.getString("password_confirm"), data.getString("email"), "user");

        if (user != null) {
            return Response.status(Response.Status.CREATED).entity("Registrierung erfolgreich").build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();

    }

    /**
     * Logout
     * @return Response
     */
    @POST
    @PermitAll
    @Path("logout")
    public Response logout() {
        LOGGER.debug("Logout is entered");

        return authController.logout(request.getCookie("jodel-session").getValue());
    }


}
