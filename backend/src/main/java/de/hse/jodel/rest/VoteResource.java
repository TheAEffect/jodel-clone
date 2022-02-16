package de.hse.jodel.rest;

import de.hse.jodel.controller.UserController;
import de.hse.jodel.controller.VotingController;
import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.User;
import de.hse.jodel.model.Voting;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/vote")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VoteResource {

    @Inject
    AuthUser authUser;

    @Inject
    UserController userController;

    @Inject
    VotingController votingController;

    /**
     * Casts a vote on a post or comment
     * @param data JSON object containing postcomment ("post"|"comment"), id and vote ("UP"|"DOWN")
     * @return Response with vote details or FORBIDDEN / BAD_REQUEST
     */
    @PUT
    @RolesAllowed({"admin", "user"})
    public Response store(JsonObject data) throws HttpExceptions {
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String postcomment = data.getString("postcomment");
        String voteStr = data.getString("vote");
        Long id = data.getLong("id");

        if (postcomment == null || voteStr == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Voting.TYPE voteType;
        try {
            voteType = Voting.TYPE.valueOf(voteStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        User currentUser = authUser != null ? authUser.getUser() : null;
        if (currentUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if ("post".equalsIgnoreCase(postcomment)) {
            Post post = Post.findById(id);
            if (post == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JsonObject result = votingController.votePost(post, currentUser, voteType);
            if (result == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            // Update karma (only if not voting on own post)
            if (post.user != null && !post.user.equals(currentUser)) {
                if (voteType == Voting.TYPE.UP) {
                    userController.increaseKarma(currentUser);
                    userController.increaseKarma(post.user);
                } else {
                    userController.decreaseKarma(currentUser);
                    userController.decreaseKarma(post.user);
                }
            }

            return Response.ok(result).build();

        } else if ("comment".equalsIgnoreCase(postcomment)) {
            Comment comment = Comment.findById(id);
            if (comment == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JsonObject result = votingController.voteComment(comment, currentUser, voteType);
            if (result == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            // Update karma (only if not voting on own comment)
            if (comment.user != null && !comment.user.equals(currentUser)) {
                if (voteType == Voting.TYPE.UP) {
                    userController.increaseKarma(currentUser);
                    userController.increaseKarma(comment.user);
                } else {
                    userController.decreaseKarma(currentUser);
                    userController.decreaseKarma(comment.user);
                }
            }

            return Response.ok(result).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
