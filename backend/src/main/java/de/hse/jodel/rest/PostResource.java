package de.hse.jodel.rest;

import de.hse.jodel.controller.PostController;
import de.hse.jodel.model.Post;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.controller.SurveyOptionController;
import de.hse.jodel.model.Channel;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    @Inject
    AuthUser authUser;

    @Inject
    PostController postController;

    @Inject
    SurveyOptionController surveyOptionController;

    private static final Logger LOGGER = Logger.getLogger(PostResource.class);

    /**
     * Gets posts
     * @param latitude latitude for position
     * @param longitude longitude for position
     * @return Response
     */
    @GET
    @RolesAllowed({"admin", "user"})
    public Response index(@QueryParam("channel") long channel, @QueryParam("criteria") String criteria, @QueryParam("latitude") Double latitude, @QueryParam("longitude") Double longitude) throws HttpExceptions {
        if(channel != 0) {
            return Response.status(Response.Status.OK).entity(postController.getPosts(channel, criteria, latitude, longitude)).build();
        } else {
            return Response.status(Response.Status.OK).entity(postController.getPosts(criteria, latitude, longitude)).build();
        }
    }

    /**
     * shows specific post
     * @param id post id
     * @return Response
     * @throws HttpExceptions error
     */
    @GET
    @Path("{id}")
    @RolesAllowed({"admin", "user"})
    public Response show(@PathParam("id") Long id) throws HttpExceptions {
        return Response.status(Response.Status.OK).entity(postController.getPost(id, authUser.getUser())).build();
    }

    /**
     * shows specific post
     * @return Response
     * @throws HttpExceptions error
     */
    @GET
    @Path("mine")
    @RolesAllowed({"admin", "user"})
    public Response mine(@QueryParam("postsby") String by) throws HttpExceptions {
        return Response.status(Response.Status.OK).entity(postController.getMine(by, authUser.getUser())).build();
    }

    /**
     * Creates Post
     * @param data object data (text, longitude, latitude, city, color)
     * @return Response
     * @throws HttpExceptions error
     */
    @PUT
    @RolesAllowed({"admin", "user"})
    public Response store(JsonObject data) throws HttpExceptions {
        LOGGER.debug("Create post");

        if (authUser != null) {
            Post createdPost = null;

            Channel channel = Channel.findById(data.getLong("channelid"));
            if(channel != null) {
                //default [type=undefined]
                if (data.getJsonObject("optional").isEmpty()) {
                    createdPost = postController.createPost(channel, data.getString("hashtag"), data.getString("text"), data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), data.getString("color"), authUser.getUser());
                } else {
                    //image [type=IMAGE]
                    if (data.getJsonObject("optional").getString("type").equals("Image")) {
                        String imgbase64 = data.getJsonObject("optional").getJsonObject("data").getString("value");
                        createdPost = postController.createImagePost(channel, imgbase64, data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), data.getString("color"), authUser.getUser());
                        //link [type=LINK]
                    } else if (data.getJsonObject("optional").getString("type").equals("Link")) {
                        String link = data.getJsonObject("optional").getJsonObject("data").getString("value");
                        createdPost = postController.createLinkPost(channel, link, data.getString("hashtag"), data.getString("text"), data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), data.getString("color"), authUser.getUser());
                        //link [type=SURVEY]
                    } else if (data.getJsonObject("optional").getString("type").equals("Survey")) {
                        JsonArray choices = data.getJsonObject("optional").getJsonArray("data");
                        if (choices.size() < 2 || choices.size() > 4) {
                            throw new HttpExceptions("No valid survey", Response.Status.CONFLICT);
                        } else {
                            createdPost = postController.createSurveyPost(channel, data.getString("hashtag"), data.getString("text"), data.getDouble("longitude"), data.getDouble("latitude"), data.getString("city"), data.getString("color"), authUser.getUser());
                            for (Object o : choices) {
                                surveyOptionController.createSurveyOption(createdPost.id, o.toString());
                            }
                        }
                    }
                }
                createdPost = postController.getPost(createdPost.id, createdPost.user);
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (createdPost == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            return Response.status(Response.Status.CREATED).entity(createdPost).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Delete a given Post id
     * @param id postId
     * @return Response
     */
    @DELETE
    @Path("{id}")  //Post ID
    @RolesAllowed({"admin", "user"})
    public Response remove(@PathParam("id") Long id) throws HttpExceptions {
        LOGGER.debug("Delete Post");
        Post postToBeRemoved = Post.findById(id);
        // Check the users role (admin can delete every post)
        if(authUser != null) {
            if(postToBeRemoved != null) {
                if (authUser.getUser().role.equals("admin")) {
                    postController.deletePost(postToBeRemoved.id);
                    return Response.status(Response.Status.OK).build();
                } else {
                    // if normal user only allow deletion of own posts
                    if (postToBeRemoved.user.equals(authUser.getUser())) {
                        postController.deletePost(postToBeRemoved.id);
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
