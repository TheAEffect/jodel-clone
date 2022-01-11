package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.*;

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

    public static List<SurveyOption> findSurveysByPostId(Long post_id) {
        return list("post_id", post_id);
    }

    public static SurveyVote findVote(User user, long postId) {
        List<SurveyVote> votes = SurveyVote.listAll();

        for (SurveyVote vote : votes) {
            if(vote.post_id.equals(postId) && vote.user.id.equals(user.id)) {
                return vote;
            }
        }
        return null;
    }
}