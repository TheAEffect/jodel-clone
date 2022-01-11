package de.hse.jodel.controller;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;


@ApplicationScoped
public class SessionController {

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(SessionController.class);

    /**
     * Method to create a session token
     * @param user The user of the session
     * @param request The HttpServerRequest
     * @return String token
     */
    private String createNewToken(User user, HttpServerRequest request) {
        String salt = BCrypt.gensalt(10);
        return BCrypt.hashpw(user.email + user.password + request.remoteAddress() + request.getHeader("User-Agent"), salt);
    }

    /**
     * Create a session for the user
     * @param user The user of the session
     * @param request The HttpServerRequest
     * @return Session object
     */
    @Transactional
    public Session createNewSession(User user, HttpServerRequest request) {
        Cookie sessionCookie = request.getCookie("jodel-session");

        Session session = null;

        if (sessionCookie != null) {
            session = Session.findByToken(sessionCookie.getValue());
        }

        if (session == null) {
            LOGGER.debug("No session found");
            try {
                String token = createNewToken(user, request);

                session = new Session();

                session.token = token;
                session.user = user;

                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                session.lastUsed = new Timestamp(now.getTime());
                session.createdAt = new Timestamp(now.getTime());

                session.persistAndFlush();
            } catch (PersistenceException exception) {
                LOGGER.error("Session persistence exception");
                exception.printStackTrace();
            }
        } else {
            session = updateSession(session.token);
        }

        return session;
    }

    /**
     * Updates a Session last used time
     * @param token String session token
     * @return Session object
     */
    @Transactional
    public Session updateSession(String token) {
        Session session = Session.findByToken(token);

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        session.lastUsed = new Timestamp(now.getTime());

        session.persistAndFlush();

        return session;
    }

    /**
     * Removes a session from the database
     * @param token String session token
     */
    @Transactional
    public boolean removeSession(String token) {
        Session session = Session.findByToken(token);

        if (session == null) {
            return false;
        }

        session.delete();
        return true;
    }

    @Transactional
    public boolean removeAllSessionsFromUser(User user) {

        boolean status = false;

        List<Session> userSessions = Session.findByUser(user);

        if (!userSessions.isEmpty()) {

            for (Session session : userSessions) {
                status = removeSession(session.token);
            }

        } else {
            status = true;
        }

        return status;
    }

    /**
     * Removes all sessions from the database
     */
    @Transactional
    public void removeAllSessions() {
       Session.deleteAll();
    }
}