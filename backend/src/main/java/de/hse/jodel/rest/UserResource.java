package de.hse.jodel.rest;

import de.hse.jodel.controller.AuthController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserController userController;

    @Inject
    AuthController authController;

    @Inject
    AuthUser authUser;

    @Context
    HttpServerRequest request;

    private static final Logger LOGGER = Logger.getLogger(UserResource.class);

    /**
     * Information about the user
     * @return Response
     */
    @GET
    @RolesAllowed({"admin", "user"})
    @Path("/whoami")
    public Response whoami() {

        if (authUser != null) {
            return Response.status(Response.Status.OK).entity(authUser.getUser()).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    }

    /**
     * Index
     * @return Response
     */
    @GET
    @RolesAllowed({"admin"})
    public Response index() {
        return Response.status(Response.Status.OK).entity(userController.getUsers()).build();
    }

    /**
     * Shows specific profile (only admin)
     * @param username the username
     * @return Response
     */
    @GET
    @RolesAllowed({"admin"})
    @Path("{username}")
    public Response show(@PathParam("username") String username) {
        return Response.status(Response.Status.OK).entity(userController.getUser(username)).build();
    }

    /**
     * Patches user
     * @param data object data (role, autoDistance, username, password, newPassword, newPassword2)
     * @return Response
     * @throws HttpExceptions error
     */
    @PATCH
    @RolesAllowed({"admin", "user"})
    public Response update(JsonObject data) throws  HttpExceptions {
        LOGGER.debug("UPDATE USER");
        Long id = authUser.getUser().id;
        String role = "user";
        boolean autoDistance = false;

        if(authUser.getUser().role.equals("admin")) {
            if (data.getString("role") != null) {
                role = data.getString("role");
            } else {
                role = authUser.getUser().role;
            }
        }

        if (data.getBoolean("autoDistance") != null) {
            autoDistance = data.getBoolean("autoDistance");
        }

        if(data.getString("oldPassword") == null && data.getString("newPassword") == null && data.getString("newPassword2") == null) {
            return Response.status(Response.Status.OK).entity(userController.updateDistance(id, autoDistance, role)).build();
        }
        return Response.status(Response.Status.OK).entity(userController.updateUser(id, data.getString("oldPassword"),
                data.getString("newPassword"), data.getString("newPassword2"),
                autoDistance, role)).entity("Passwort erfolgreich geändert").build();
    }

    /**
     * Creates user
     * @param data object data (username, password, password_confirmation, email, role)
     * @return Reposnce
     * @throws HttpExceptions error
     */
    @PUT
    @RolesAllowed("admin")
    public Response store(JsonObject data) throws HttpExceptions {
        LOGGER.debug("Create user");

        if (authUser != null) {
            User createdUser = userController.createUser(data.getString("password"), data.getString("password_confirmation"), data.getString("email"), data.getString("role"));

            LOGGER.debug("User created");

            if (createdUser == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Deletes user
     * @return Responce
     */
    @DELETE
    @RolesAllowed({"admin", "user"})
    public Response destroy() {
            // if normal user only allow deletion of own account
                if (userController.removeUser(authUser.getUser().id)) {
                    // Get current session token from the cookie
                    String token = request.getCookie("jodel-session").getValue();

                    // return response with new cookie with 0 max age
                    return Response.status(Response.Status.OK).cookie(authController.buildResponseCookie(token, 0)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
    }
}
