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
import org.jboss.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(PostResource.class);


    /**
     * Put a comment to the given Post ID
     * @param data object data (postcomment, id, vote)
     * @return Response
     */
    @PUT
    @RolesAllowed({"admin", "user"})
    public Response store(JsonObject data) throws HttpExceptions {
        String postcomment = data.getString("postcomment");
        String vote = data.getString("vote");
        long id = data.getLong("id");
        LOGGER.error("Create Vote");
        postcomment = postcomment.toLowerCase();
        if (authUser != null) {
            if(postcomment.equals("comment") || postcomment.equals("post")) {
               try {
                   if(!vote.equals("") && (Voting.TYPE.valueOf(vote.toUpperCase()).equals(Voting.TYPE.UP)
                           || Voting.TYPE.valueOf(vote.toUpperCase()).equals(Voting.TYPE.DOWN))) {
                       if (postcomment.equals("post")) {
                           Post post = Post.findById(id);
                           Object[] arr = votingController.setVoting(post, authUser.getUser(), Voting.TYPE.valueOf(vote.toUpperCase()));
                           if((boolean) arr[0]) {
                               User votedUser = post.user;
                               if(Voting.TYPE.valueOf(vote.toUpperCase()).equals(Voting.TYPE.UP)) {
                                   if(!post.user.equals(authUser.getUser())) {
                                       userController.increaseKarma(authUser.getUser()); // voter
                                       userController.increaseKarma(votedUser); // voted user
                                   }
                               } else {
                                   if(!post.user.equals(authUser.getUser())) {
                                       userController.decreaseKarma(authUser.getUser()); // voter
                                       userController.decreaseKarma(votedUser); // voted user
                                   }
                               }
                               return Response.status(Response.Status.OK).entity(arr[1]).build();
                           } else {
                               return Response.status(Response.Status.FORBIDDEN).build();
                           }
                       } else {
                           Comment comment = Comment.findById(id);
                           Object[] arr = votingController.setVoting(comment, authUser.getUser(), Voting.TYPE.valueOf(vote.toUpperCase()));
                           if((boolean) arr[0]) {
                               User votedUser = comment.user;
                               if(Voting.TYPE.valueOf(vote.toUpperCase()).equals(Voting.TYPE.UP)) {
                                   userController.increaseKarma(authUser.getUser()); // voter
                                   userController.increaseKarma(votedUser); // voted user
                               } else {
                                   userController.decreaseKarma(authUser.getUser()); // voter
                                   userController.decreaseKarma(votedUser); // voted user
                               }
                               return Response.status(Response.Status.OK).entity(arr[1]).build();
                           } else {
                               return Response.status(Response.Status.FORBIDDEN).build();
                           }
                       }
                   }
               } catch (Exception exception) {
                   return Response.status(Response.Status.NOT_ACCEPTABLE).build();
               }
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
}
