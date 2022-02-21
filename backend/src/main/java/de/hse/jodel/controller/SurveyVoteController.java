package de.hse.jodel.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    private static final Logger LOGGER = Logger.getLogger(SurveyVoteController.class);

    /**
     * Records a survey vote and updates the option and post counters
     *
     * @param user the user casting the vote
     * @param postId ID of the post containing the survey
     * @param optionId ID of the chosen survey option
     * @return the created SurveyVote or null if creation failed
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
        } catch (Exception exception) {
            LOGGER.error("Failed to create survey vote", exception);
            return null;
        }
    }
}