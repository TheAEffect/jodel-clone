package de.hse.jodel.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "surveys_options")
public class SurveyOption extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "surveySeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "surveys", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "surveySeq")
    @Column(name = "id")
    public Long id;

    @Column(name = "post_id")
    public Long post_id;

    @Column(name = "option")
    public String option;

    @Column(name = "number_votes")
    public Integer votes;

    /**
     * Finds all options for a post and masks vote counts if the user has not voted yet
     *
     * @param postId the post ID of the survey
     * @param user the user viewing the survey
     * @return list of survey options with vote counts (masked as null if user has not voted)
     */
    public static List<SurveyOption> findSurveysOfPostId(Long postId, User user) {
        SurveyVote vote = SurveyVote.findVote(user, postId);
        List<SurveyOption> surveys = list("post_id", postId);

        if (vote == null) {
            surveys.forEach(survey -> survey.votes = null);
        }
        return surveys;
    }
}