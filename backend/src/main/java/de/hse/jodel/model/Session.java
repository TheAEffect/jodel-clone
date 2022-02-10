package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;


@Entity
@Table(name = "sessions")
public class Session extends PanacheEntityBase {

    @Id
    @Column(name = "token", unique = true, nullable = false)
    @JsonIgnore
    public String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    public User user;

    @Column(name = "last_used")
    @JsonIgnore
    public Timestamp lastUsed;

    @Column(name = "created_at")
    @JsonIgnore
    public Timestamp createdAt;


    public static Session findByToken(String token) {
        return find("token", token).firstResult();
    }

    public static List<Session> findByUser(User user) {
        List<Session> sessions = Session.listAll();

        List<Session> userSessions = new ArrayList<>();

        for (Session session : sessions) {
            if (session.user.equals(user)) {
                userSessions.add(session);
            }
        }

        return userSessions;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Session session = (Session) obj;
        return Objects.equals(token, session.token);
    }

}
