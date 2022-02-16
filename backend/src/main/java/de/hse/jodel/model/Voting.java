package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.*;

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

    /**
     * Checks if a user has already voted on a post
     */
    public static boolean hasVoted(Post post, User user) {
        return count("post = ?1 and user = ?2", post, user) > 0;
    }

    /**
     * Checks if a user has already voted on a comment
     */
    public static boolean hasVoted(Comment comment, User user) {
        return count("comment = ?1 and user = ?2", comment, user) > 0;
    }
}
