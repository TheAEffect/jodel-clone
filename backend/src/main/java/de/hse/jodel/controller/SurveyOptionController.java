package de.hse.jodel.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import de.hse.jodel.model.SurveyOption;
import org.jboss.logging.Logger;


@ApplicationScoped
public class SurveyOptionController {


    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(PostController.class);

    /*
    public List<Survey> getSurveyOptions(Long post_id) {
        return Survey.findSurveysByPostId(post_id);
    }
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
        } catch (PersistenceException exception) {
            LOGGER.error("Survey creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }

        return null;
    }

    /**
     *
     * @param optionId
     * @return
     */
    @Transactional
    public boolean updateVotingNumber(Long optionId) {
        SurveyOption surveyOption = SurveyOption.findById(optionId);
        if(surveyOption != null) {
            try {
                surveyOption.votes ++;
                surveyOption.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Increase Voting");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return false;
    }
}