package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.util.*;
import java.lang.Math;


@Entity
@Table(name = "comments")
public class Comment extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "commentSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "comments", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "commentSeq")

    @Column(name = "id")
    public Long id;

    @Column(name = "text")
    public String text;

    @Column(name = "longitude")
    @JsonIgnore
    public Double longitude;

    @Column(name = "latitude")
    @JsonIgnore
    public Double latitude;

    @Column(name = "city")
    public String city;

    @Column(name = "posted_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date postedAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName="id")
    @JsonIgnore
    @JsonIgnoreProperties({ "id", "email", "role", "autoDistance" })
    public User user;

    @Column(name = "post_id")
    public long post_id;

    @Column(name = "voting_value")
    public int votingValue;

    @Lob
    @Column(name = "image")
    public byte[] image;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public Comment.TYPE type;

    @Column(name = "jodel_number")
    public Long jodelNumber;

    @Column(name = "jodel_number_color")
    public String jodelNumberColor;

    @JsonManagedReference
    @OneToMany
    @JoinColumn(name = "comment_id")
    @JsonIgnoreProperties({ "id", "comment" })
    public Set<Voting> votings;

    @Transient
    public boolean yours;

    public enum TYPE {
        IMAGE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    private static String getRandomColor() {
        final String[] letters = {"#FF9908","#FFBA00","#DD5F5F","#06A3CB","#8ABDB0","#9EC41C"};
        return letters[(int) Math.floor(Math.random() * 6)];
    }

    public static Object[] getJodelNumber(long user_id, long post_id, long post_user_id) {
        Object[] jodelNumber = new Object[2];
        if(user_id != post_user_id) {
            List<Comment> list = Comment.find("post_id = ?1", post_id).list();
            if(list.size() == 0) {
                jodelNumber[0] = 1;
                jodelNumber[1] = getRandomColor();
                return jodelNumber;
            }else {
                Comment c = Comment.find("post_id = ?1 and author_id = ?2", post_id, user_id).firstResult();
                if(c != null) {
                    jodelNumber[0] = c.jodelNumber;
                    jodelNumber[1] = c.jodelNumberColor;
                    return jodelNumber;
                } else {
                    Comment c2 = Comment.find("post_id = ?1 order by jodel_number DESC", post_id).firstResult();
                    jodelNumber[0] = c2.jodelNumber + 1;
                    jodelNumber[1] = getRandomColor();
                    return jodelNumber;
                }
            }
        } else {
            //OP
            jodelNumber[0] = 0;
            jodelNumber[1] = null;
            return jodelNumber;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}