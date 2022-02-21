package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@Entity
@Table(name = "surveys_votes")
public class SurveyVote extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "surveyVotesSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "surveysVotes", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "surveyVotesSeq")
    @Column(name = "id")
    @JsonIgnore
    public Long id;

    @Column(name = "post_id")
    public Long post_id;

    @Column(name = "survey_options_id")
    public Long options_id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id")
    @JsonIgnore
    public User user;
 
    /**
     * Finds a survey vote for a specific user and post
     *
     * @param user the user who voted
     * @param postId the post ID of the survey
     * @return the SurveyVote or null if the user has not voted yet
     */
    public static SurveyVote findVote(User user, long postId) {
        return find("user = ?1 and post_id = ?2", user, postId).firstResult();
    }
}