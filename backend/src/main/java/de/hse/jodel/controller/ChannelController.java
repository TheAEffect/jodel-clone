package de.hse.jodel.controller;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import de.hse.jodel.model.Channel;
import de.hse.jodel.model.*;
import de.hse.jodel.utils.AuthUser;
import org.jboss.logging.Logger;


@ApplicationScoped
public class ChannelController {

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(ChannelController.class);


    public List<Channel> getChannels() {
        return Channel.listAll();
    }
}