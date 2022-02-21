

package de.hse.jodel.rest;

import de.hse.jodel.controller.PostController;
import de.hse.jodel.controller.SurveyVoteController;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.SurveyVote;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/survey")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SurveyResource {

    @Inject
    AuthUser authUser;

    @Inject
    SurveyVoteController surveyVoteController;

    @Inject
    PostController postController;

    /**
     * Adds a vote to a survey option for the given post
     *
     * @param postId post ID containing the survey
     * @param data JSON payload with option ID ("id")
     * @return Response with the updated Post object or error status
     * @throws HttpExceptions if user lookup or authorization fails
     */
    @PUT
    @Path("{id}")
    @RolesAllowed({"admin", "user"})
    public Response addVote(@PathParam("id") Long postId, JsonObject data) throws HttpExceptions {
        User user = authUser != null ? authUser.getUser() : null;
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (data == null || !data.containsKey("id")) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        SurveyVote existingVote = SurveyVote.findVote(user, postId);
        if (existingVote != null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Long optionId = data.getLong("id");
        surveyVoteController.createSurveyVote(user, postId, optionId);
        Post post = postController.getPost(postId, user);
        if (post == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(post).build();
    }
}

