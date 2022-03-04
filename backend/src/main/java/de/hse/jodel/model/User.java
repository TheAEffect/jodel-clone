package de.hse.jodel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "UserSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "users", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "UserSeq")
    @Column(name = "id")
    @JsonIgnore
    public Long id;

    @Column(name = "karma")
    public long karma;

    @Column(name = "email")
    public String email;

    @Column(name = "auto_distance")
    public boolean autoDistance;

    @Column(name = "password")
    @JsonIgnore
    public String password;

    @Column
    public String role;

    /**
     * Finds a user by email address
     *
     * @param email user email
     * @return User object or null if not found
     */
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;     
        return Objects.equals(id, user.id);
    }
}
