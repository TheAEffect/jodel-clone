

package de.hse.jodel.rest;

import de.hse.jodel.controller.PostController;
import de.hse.jodel.controller.SurveyVoteController;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.SurveyVote;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(PostResource.class);

    /**
     * Delete a given Post id
     * @param postId
     * @return Response
     */
    @PUT
    @Path("{id}")  //Post ID
    @RolesAllowed({"admin", "user"})
    public Response addVote(@PathParam("id") Long postId, JsonObject data) throws HttpExceptions {
        if(authUser != null) {
            User user = authUser.getUser();
            SurveyVote vote = SurveyVote.findVote(user, postId);
            if(vote == null) {
                Long optionId = data.getLong("id");
                SurveyVote surveyVote = surveyVoteController.createSurveyVote(user, postId, optionId);
                Post post = postController.getPost(postId, authUser.getUser());
                if(post == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.status(Response.Status.OK).entity(post).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}

