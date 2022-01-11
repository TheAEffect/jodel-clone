package de.hse.jodel.utils;

import de.hse.jodel.model.User;

import javax.enterprise.context.RequestScoped;

/**
 * Auth User
 */
@RequestScoped
public class AuthUser {
    private User user;

    /**
     * Setter
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Getter
     * @return User
     */
    public User getUser() {
        return this.user;
    }
}
