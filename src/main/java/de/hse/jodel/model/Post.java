package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.*;

import javax.persistence.*;


@Entity
@Table(name = "posts")
public class Post extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "postSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "posts", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "postSeq")
    @Column(name = "id")
    public Long id;

    @Column(name = "text", length = 45)
    public String text;

    @JsonIgnore
    @Column(name = "longitude")
    public Double longitude;

    @JsonIgnore
    @Column(name = "latitude")
    public Double latitude;

    @Column(name = "city")
    public String city;

    @Column(name = "posted_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date postedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    @JsonIgnore
    @JsonIgnoreProperties({ "id", "email", "role", "autoDistance" })
    public User user;

    @Column(name = "color")
    public String color;

    @Column(name = "hashtag")
    public String hashtag;

    @Column(name = "link")
    public String link;

    @Lob
    @Column(name = "image")
    public byte[] image;

    @Column(name = "voting_value")
    public Integer votingValue;

    @Column(name = "comment_number")
    public int comment_number;

    @Column(name = "survey_votes")
    public Integer survey_votes;

    @Transient
    public List<Comment> comments;

    @Transient
    public boolean yours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", referencedColumnName = "id")
    @JsonIgnoreProperties({ "id", "info", "symbol" })
    public Channel channel;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public Post.TYPE type;

    @JsonManagedReference
    @OneToMany
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties({ "id", "comment" })
    public Set<Voting> votings;

    @Transient
    @JsonIgnoreProperties({ "post_id", "survey_id" })
    public List<SurveyOption> surveys;

    @Transient
    public Long vote_id;

    public enum TYPE {
        IMAGE, LINK, SURVEY
    }

    public static List<Post> findByUser(User user) {
        List<Post> posts = Post.listAll();

        List<Post> userSessions = Collections.<Post>emptyList();

        for (Post post : posts) {
            if (post.user.equals(user)) {
                post.yours = true;
                userSessions.add(post);
            }
        }

        return userSessions;
    }


    public static Post findByID(Long postId) {
        Post post = Post.findById(postId);

        return post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }
}