package de.hse.jodel.controller;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;


@ApplicationScoped
public class UserController {

    @Inject
    SessionController sessionController;

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    /**
     * Returns all users from the database
     * @return List containing user objects
     */
    public List<User> getUsers() {
        return User.listAll();
    }

    /**
     * Returns specific user based on the id
     * @param username username of the user
     * @return User object
     */
    public User getUser(String username) {
        return User.findByUsername(username);
    }

    /**
     * Creates a new user
     * @param password String containing the users password
     * @param email The unique user email address
     * @return User object
     */
    @Transactional
    public User createUser(String password, String password2, String email, String role) throws HttpExceptions {
        LOGGER.debug("User controller");
        String salt = BCrypt.gensalt(10);
        try {
            if(email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                throw new HttpExceptions("Bitte alle Felder ausfüllen", Response.Status.NOT_ACCEPTABLE);
            } else if(!password.equals(password2)) {
                throw new HttpExceptions("Passwörter stimmen nicht überein", Response.Status.NOT_ACCEPTABLE);
            }
            User user = new User();
            //user.username = username;
            user.email = email;
            user.password = BCrypt.hashpw(password, salt);
            user.role = role;
            user.karma = 0;

            user.persistAndFlush();

            //LOGGER.debug("USER:" + user.username);
            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("User creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Ein Konto mit dieser Email besteht bereits", Response.Status.CONFLICT);
        }
    }

    /**
     * Updates the data of a specific user
     * @param id The user id
     * @return User object
     */
    @Transactional
    public User updateUser(Long id, String oldPassword, String newPassword, String newPassword2, boolean autoDistance, String role) throws HttpExceptions {
        LOGGER.debug("Controller Update User");
        try {
            User user = User.findById(id);

            if(oldPassword != null && !oldPassword.isEmpty()) {
                if (BCrypt.checkpw(oldPassword, user.password)) {
                    if (!newPassword.isEmpty() && !newPassword2.isEmpty() && newPassword.equals(newPassword2)) {
                        String salt = BCrypt.gensalt(10);
                        user.password = BCrypt.hashpw(newPassword, salt);
                    } else {
                        throw new HttpExceptions("Passwörter stimmen nicht überein", Response.Status.NOT_ACCEPTABLE);
                    }
                } else {
                    throw new HttpExceptions("Passwort falsch", Response.Status.UNAUTHORIZED);
                }
            }

            if (user.autoDistance != autoDistance) {
                user.autoDistance = autoDistance;
            }

            if (!user.role.equals(role)) {
                user.role = role;
            }

            user.persistAndFlush();
            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("User update");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Fehler beim speichern", Response.Status.CONFLICT);
        }
    }


    /**
     * Updates the data of a specific user
     * @param id The user id
     * @return User object
     */
    @Transactional
    public User updateDistance(Long id, boolean autoDistance, String role) throws HttpExceptions {
        try {
            User user = User.findById(id);

            if (user.autoDistance != autoDistance) {
                user.autoDistance = autoDistance;
            }

            if (!user.role.equals(role)) {
                user.role = role;
            }

            user.persistAndFlush();
            return user;
        } catch (PersistenceException exception) {
            LOGGER.error("User update");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Fehler beim speichern", Response.Status.CONFLICT);
        }
    }

    /**
     * Remove a user from the database
     * @param id User id
     */
    @Transactional
    public boolean removeUser(Long id) {
        boolean status = false;

        User user = User.findById(id);

        if (user != null) {
            status = sessionController.removeAllSessionsFromUser(user);

            if (status) {
                Comment.delete("author_id = " + user.id);
                Post.delete("author_id = " + user.id);
                status = User.deleteById(id);
            }
        }

        return status;
    }

    /**
     * Remove all users from the database
     */
    @Transactional
    public void removeAllUsers() {
        User.deleteAll();
    }

    @Transactional
    public void increaseKarma(User u) throws HttpExceptions {
        try {
            User user = User.findById(u.id);
            user.karma +=2;

            user.persistAndFlush();
        } catch (PersistenceException exception) {
            LOGGER.error("User update");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }
    }

    @Transactional
    public void decreaseKarma(User u) throws HttpExceptions {
        try {
            User user = User.findById(u.id);
            user.karma -=2;

            user.persistAndFlush();
        } catch (PersistenceException exception) {
            LOGGER.error("User update");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }
    }
}