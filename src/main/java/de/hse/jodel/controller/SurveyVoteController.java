package de.hse.jodel.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import de.hse.jodel.model.SurveyVote;
import de.hse.jodel.model.User;
import org.jboss.logging.Logger;


@ApplicationScoped
public class SurveyVoteController {

    @Inject
    SurveyOptionController surveyOptionController;


    @Inject
    PostController postController;

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(PostController.class);

    /*
    public List<Survey> getSurveyOptions(Long post_id) {
        return Survey.findSurveysByPostId(post_id);
    }
    */
    @Transactional
    public SurveyVote createSurveyVote(User user, Long postId, Long optionId) {
        try {
            SurveyVote surveyVote = new SurveyVote();
            surveyVote.user = user;
            surveyVote.post_id = postId;
            surveyVote.options_id = optionId;
            surveyVote.persistAndFlush();
            surveyOptionController.updateVotingNumber(optionId);
            postController.updateSurveyVotingNumber(postId);
            return surveyVote;
        } catch (PersistenceException exception) {
            LOGGER.error("Survey creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }

        return null;
    }

}