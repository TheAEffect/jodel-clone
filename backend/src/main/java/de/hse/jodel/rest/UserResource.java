package de.hse.jodel.rest;

import de.hse.jodel.controller.AuthController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

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

    /**
     * Information about the currently authenticated user
     *
     * @return Response containing the user object or UNAUTHORIZED
     */
    @GET
    @RolesAllowed({"admin", "user"})
    @Path("/whoami")
    public Response whoami() {
        if (authUser != null && authUser.getUser() != null) {
            return Response.ok(authUser.getUser()).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    }

    /**
     * Returns a list of all users (admin only)
     *
     * @return Response containing the list of users
     */
    @GET
    @RolesAllowed({"admin"})
    public Response index() {
        return Response.ok(userController.getUsers()).build();
    }

    /**
     * Updates user settings (autoDistance, role) or password
     *
     * @param data JSON object containing settings and/or password fields
     * @return Response containing the updated User object
     * @throws HttpExceptions if validation fails
     */
    @PATCH
    @RolesAllowed({"admin", "user"})
    public Response update(JsonObject data) throws HttpExceptions {
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        User currentUser = authUser.getUser();
        Long id = currentUser.id;
        String role = currentUser.role.equals("admin") && data.getString("role") != null
                ? data.getString("role")
                : currentUser.role;
        boolean autoDistance = data.getBoolean("autoDistance") != null
                ? data.getBoolean("autoDistance")
                : false;

        String oldPassword = data.getString("oldPassword");
        String newPassword = data.getString("newPassword");
        String newPassword2 = data.getString("newPassword2");

        User user;
        if (oldPassword == null && newPassword == null && newPassword2 == null) {
            user = userController.updateDistance(id, autoDistance, role);
        } else {
            userController.updateDistance(id, autoDistance, role);
            user = userController.updatePassword(id, oldPassword, newPassword, newPassword2);
        }

        return Response.ok(user).build();
    }

    /**
     * Creates a new user (admin only)
     *
     * @param data JSON object containing user credentials
     * @return Response containing the created User
     * @throws HttpExceptions if validation fails
     */
    @PUT
    @RolesAllowed("admin")
    public Response store(JsonObject data) throws HttpExceptions {
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (authUser != null) {
            User createdUser = userController.createUser(
                    data.getString("password"),
                    data.getString("password_confirmation"),
                    data.getString("email"),
                    data.getString("role")
            );

            if (createdUser == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Deletes the currently authenticated user's account
     *
     * @return Response clearing the session cookie
     */
    @DELETE
    @RolesAllowed({"admin", "user"})
    public Response destroy() {
        if (authUser != null && authUser.getUser() != null) {
            if (userController.removeUser(authUser.getUser().id)) {
                String token = request.getCookie("jodel-session").getValue();
                return Response.ok().cookie(authController.buildResponseCookie(token, 0)).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
