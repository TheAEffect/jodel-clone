package de.hse.jodel.controller;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import de.hse.jodel.model.Channel;

@ApplicationScoped
public class ChannelController {

    /**
     * Retrieves all channels
     * @return list of all channels
     */
    public List<Channel> getChannels() {
        return Channel.listAll();
    }
}