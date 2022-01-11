package de.hse.jodel.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.*;

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

    @Column(name = "`option`")
    public String option;

    @Column(name = "number_votes")
    public Integer votes;

    public static List<SurveyOption> findSurveysOfPostId(Long postId, User user) {
        SurveyVote votes = SurveyVote.findVote(user, postId);

        //List<SurveyVotes> surveys = SurveyOption.listAll();

        List<SurveyOption> surveys = SurveyOption.list("post_id", postId);

        surveys.forEach(survey -> {
            if(votes == null) {
                survey.votes = null;
            }
        });
/*
        List<SurveyOption> surveyFiltered = new ArrayList<>();
        for (SurveyOption survey : surveys) {
            if (survey.post_id.equals(post_id)) {
                //survey.survey_votes = null;
                surveyFiltered.add(survey);
            }
        }
        return surveyFiltered;
*/
        return surveys;
        //return SurveyOption.list("post_id", post_id);
    }
}