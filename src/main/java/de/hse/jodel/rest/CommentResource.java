package de.hse.jodel.rest;

import de.hse.jodel.controller.CommentController;
import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {

    @Inject
    AuthUser authUser;

    @Inject
    CommentController commentController;

    private static final Logger LOGGER = Logger.getLogger(PostResource.class);

    /**
     * Put a comment to the given Post ID
     * @param id postId
     * @return Response
     */
    @PUT
    @Path("{id}")  //Post ID
    @RolesAllowed({"admin", "user"})
    public Response store(@PathParam("id") Long id, JsonObject data) throws HttpExceptions {
        if (authUser != null) {
            Post commentForPost = Post.findById(id);
            if(commentForPost != null) {
                Comment createdComment = null;

                //default [type=undefined]
                if(data.getJsonObject("optional").isEmpty()) {
                    createdComment = commentController.createComment(data.getString("text"), data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), commentForPost, authUser.getUser());
                } else {
                    //image [type=IMAGE]
                    if(data.getJsonObject("optional").getString("type").equals("Image")) {
                        String imgbase64 = data.getJsonObject("optional").getJsonObject("data").getString("value");
                        createdComment = commentController.createImageComment(imgbase64, data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), commentForPost, authUser.getUser());
                    }
                }

                createdComment = commentController.getComment(createdComment.id, createdComment.user);
                if (createdComment == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                return Response.status(Response.Status.CREATED).entity(createdComment).build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Delete a given Comment id
     * @param id commentId
     * @return Response
     */
    @DELETE
    @Path("{id}")  //Comment ID
    @RolesAllowed({"admin", "user"})
    public Response remove(@PathParam("id") Long id) throws HttpExceptions {
        LOGGER.debug("Delete Comment");
        Comment commentToBeRemoved = Comment.findById(id);
        // Check the users role (admin can delete every user)
        if(authUser != null) {
            if(commentToBeRemoved != null) {
                if (authUser.getUser().role.equals("admin")) {
                    commentController.deleteComment(commentToBeRemoved, -1);
                    return Response.status(Response.Status.OK).build();
                } else {
                    // if normal user only allow deletion of own comments
                    if (commentToBeRemoved.user.equals(authUser.getUser())) {
                        commentController.deleteComment(commentToBeRemoved, -1);
                        return Response.status(Response.Status.OK).build();
                    } else {
                        return Response.status(Response.Status.FORBIDDEN).build();
                    }
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
