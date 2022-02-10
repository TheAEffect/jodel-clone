package de.hse.jodel.rest;

import de.hse.jodel.controller.ChannelController;
import de.hse.jodel.utils.AuthUser;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/channel")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelResource {

    @Inject
    ChannelController channelController;

    private static final Logger LOGGER = Logger.getLogger(PostResource.class);


    /**
     * Gets channels
     * @return Response
     */
    @GET
    @RolesAllowed({"admin", "user"})
    public Response index() {
        return Response.status(Response.Status.OK).entity(channelController.getChannels()).build();
    }
}

