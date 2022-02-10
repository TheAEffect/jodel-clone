package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.jboss.logging.Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "votings")
public class Voting extends PanacheEntityBase {
    public enum TYPE {
        UP, DOWN
    }

    @Id
    @TableGenerator(name = "votingSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "votings", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "votingSeq")

    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id")
    @JsonIgnore
    @JsonIgnoreProperties({ "autoDistance", "email", "role" })
    public User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "comment_id", referencedColumnName="id")
    public Comment comment;

    @JsonBackReference
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id")
    public Post post;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public TYPE type;

    @Transient
    public boolean yours;

    public static List<Voting> getVotings(Object e) {
        List<Voting> allVotings = Voting.listAll();
        List<Voting> filteredVotings = new ArrayList<>();

        if (e instanceof Post || e instanceof Comment) {
            for (Voting voting : allVotings) {
                if ((voting.comment != null && voting.comment.equals(e)) || (voting.post != null && voting.post.equals(e))) {
                    filteredVotings.add(voting);
                }
            }
        }

        return filteredVotings;
    }

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    /**
     * checks if user is allowed to vote
     * @param e Object: either Post or Comment
     * @param user User to check
     * @return true if vote is allowed, else false
     */
    public static boolean hasPermission(Object e, User user, Voting.TYPE t) throws HttpExceptions {
        /*if(!voteAlreadyExists(e, user, t)) {
            if (e instanceof Post) {
                return !((Post) e).user.equals(user);
            } else if (e instanceof Comment) {
                return !((Comment) e).user.equals(user);
            } else {
                throw new IllegalArgumentException("Type of Object unknown");
            }
        } else {
            throw new HttpExceptions("Vote already exists", Response.Status.CONFLICT);
        }*/
        if (e instanceof Post) {
            return !((Post) e).user.equals(user);
        } else if (e instanceof Comment) {
            return !((Comment) e).user.equals(user);
        } else {
            throw new IllegalArgumentException("Type of Object unknown");
        }
    }
    /**
     * checks if vote of user already exist
     * @param e Object: either Post or Comment
     * @param user User to check
     * @return true if already exist, else false
     */
    public static boolean voteAlreadyExists(Object e, User user, Voting.TYPE t) {
        List<Voting> votes = getVotings(e);
        return votes.stream().anyMatch(o -> o.user.equals(user) && o.type.equals(t));
    }
}
