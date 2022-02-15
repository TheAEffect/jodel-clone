package de.hse.jodel.rest;

import de.hse.jodel.controller.ChannelController;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/channel")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelResource {

    @Inject
    ChannelController channelController;

    /**
     * Gets all channels
     * @return Response containing list of channels
     */
    @GET
    @RolesAllowed({"admin", "user"})
    public Response index() {
        return Response.ok(channelController.getChannels()).build();
    }
}

