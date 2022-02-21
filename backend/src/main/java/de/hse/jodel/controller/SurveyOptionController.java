package de.hse.jodel.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import de.hse.jodel.model.SurveyOption;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SurveyOptionController {

    private static final Logger LOGGER = Logger.getLogger(SurveyOptionController.class);

    /**
     * Creates a survey option for a post
     *
     * @param postId post ID to associate the option with
     * @param option text description of the survey option
     * @return the created SurveyOption or null if creation failed
     */
    @Transactional
    public SurveyOption createSurveyOption(long postId, String option) {
        try {
            SurveyOption surveyOption = new SurveyOption();
            surveyOption.post_id = postId;
            surveyOption.votes = 0;
            surveyOption.option = option;
            surveyOption.persistAndFlush();

            return surveyOption;
        } catch (Exception exception) {
            LOGGER.error("Failed to create survey option", exception);
            return null;
        }
    }

    /**
     * Increments the vote count for a survey option
     *
     * @param optionId ID of the survey option
     * @return true if vote count was successfully incremented, else false
     */
    @Transactional
    public boolean updateVotingNumber(Long optionId) {
        SurveyOption surveyOption = SurveyOption.findById(optionId);
        if (surveyOption != null) {
            try {
                surveyOption.votes++;
                surveyOption.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Failed to increase survey vote count", exception);
            }
        }
        return false;
    }
}