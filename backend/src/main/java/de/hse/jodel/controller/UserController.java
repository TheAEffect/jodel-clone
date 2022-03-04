package de.hse.jodel.controller;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.exception.HttpExceptions;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class UserController {

    @Inject
    SessionController sessionController;

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    /**
     * Returns all users from the database
     *
     * @return list of all users
     */
    public List<User> getUsers() {
        return User.listAll();
    }

    /**
     * Creates a new user with hashed password
     *
     * @param password raw password
     * @param password2 password confirmation
     * @param email unique email address
     * @param role user role ("user" or "admin")
     * @return created User object
     * @throws HttpExceptions if validation fails or email is already taken
     */
    @Transactional
    public User createUser(String password, String password2, String email, String role) throws HttpExceptions {
        if (email == null || email.isEmpty() || password == null || password.isEmpty() || password2 == null || password2.isEmpty()) {
            throw new HttpExceptions("Bitte alle Felder ausfüllen", Response.Status.NOT_ACCEPTABLE);
        } else if (!password.equals(password2)) {
            throw new HttpExceptions("Passwörter stimmen nicht überein", Response.Status.NOT_ACCEPTABLE);
        }
        try {
            String salt = BCrypt.gensalt(10);
            User user = new User();
            user.email = email;
            user.password = BCrypt.hashpw(password, salt);
            user.role = role != null ? role : "user";
            user.karma = 0;
            user.persistAndFlush();

            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("Failed to create user", exception);
            throw new HttpExceptions("Ein Konto mit dieser Email besteht bereits", Response.Status.CONFLICT);
        }
    }

    /**
     * Updates user password after verifying the old password
     *
     * @param id user ID
     * @param oldPassword current password
     * @param newPassword new password
     * @param newPassword2 new password confirmation
     * @return updated User object
     * @throws HttpExceptions if password check fails
     */
    @Transactional
    public User updatePassword(Long id, String oldPassword, String newPassword, String newPassword2) throws HttpExceptions {
        User user = User.findById(id);
        if (user == null) {
            throw new HttpExceptions("User nicht gefunden", Response.Status.NOT_FOUND);
        }

        if (oldPassword == null || oldPassword.isEmpty() || !BCrypt.checkpw(oldPassword, user.password)) {
            throw new HttpExceptions("Passwort falsch", Response.Status.UNAUTHORIZED);
        }

        if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(newPassword2)) {
            throw new HttpExceptions("Passwörter stimmen nicht überein", Response.Status.NOT_ACCEPTABLE);
        }

        try {
            String salt = BCrypt.gensalt(10);
            user.password = BCrypt.hashpw(newPassword, salt);
            user.persistAndFlush();
            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("Failed to update password", exception);
            throw new HttpExceptions("Fehler beim speichern", Response.Status.CONFLICT);
        }
    }

    /**
     * Updates auto-distance and role settings for a user
     *
     * @param id user ID
     * @param autoDistance automatic distance preference
     * @param role user role
     * @return updated User object
     * @throws HttpExceptions if user not found or persistence fails
     */
    @Transactional
    public User updateDistance(Long id, boolean autoDistance, String role) throws HttpExceptions {
        User user = User.findById(id);
        if (user == null) {
            throw new HttpExceptions("User nicht gefunden", Response.Status.NOT_FOUND);
        }

        try {
            user.autoDistance = autoDistance;
            if (role != null) {
                user.role = role;
            }

            user.persistAndFlush();
            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("Failed to update user distance/settings", exception);
            throw new HttpExceptions("Fehler beim speichern", Response.Status.CONFLICT);
        }
    }

    /**
     * Removes a user and all related sessions, posts, and comments
     *
     * @param id user ID
     * @return true if deleted successfully, else false
     */
    @Transactional
    public boolean removeUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            return false;
        }
        boolean status = sessionController.removeAllSessionsFromUser(user);
        if (status) {
            Comment.delete("user = ?1", user);
            Post.delete("user = ?1", user);
            status = User.deleteById(id);
        }
        
        return status;
    }

    /**
     * Removes all users from the database
     */
    @Transactional
    public void removeAllUsers() {
        User.deleteAll();
    }

    /**
     * Increases user karma by 2
     *
     * @param u user whose karma should be increased
     */
    @Transactional
    public void increaseKarma(User u) {
        if (u != null && u.id != null) {
            User user = User.findById(u.id);
            if (user != null) {
                user.karma += 2;
                user.persistAndFlush();
            }
        }
    }

    /**
     * Decreases user karma by 2
     *
     * @param u user whose karma should be decreased
     */
    @Transactional
    public void decreaseKarma(User u) {
        if (u != null && u.id != null) {
            User user = User.findById(u.id);
            if (user != null) {
                user.karma -= 2;
                user.persistAndFlush();
            }
        }
    }
}