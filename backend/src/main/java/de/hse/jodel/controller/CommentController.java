package de.hse.jodel.controller;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import de.hse.jodel.model.Voting;
import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.exception.HttpExceptions;
import org.jboss.logging.Logger;


@ApplicationScoped
public class CommentController {
    @Inject
    EntityManager em;

    @Inject
    PostController postController;

    private static final Logger LOGGER = Logger.getLogger(CommentController.class);
    
    public List<Comment> getComments() {
    	 return Comment.listAll();
    }

    public Comment getComment(Long id, User user) throws HttpExceptions {
   	 	Comment c = Comment.findById(id);
        if(c != null) {
            c.yours = c.user.equals(user);
            return c;
        } else {
            throw new HttpExceptions(null, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Creates a new comment
     * @param text comment
     * @param longitude longitude
     * @param latitude latitude
     * @param city city
     * @param post post of the comment
     * @param user user of the comment
     * @return created Comment
     */
    @Transactional
    public Comment createComment(String text, Double longitude, Double latitude, String city, Post post, User user) {
        try {
            Comment comment = new Comment();
            comment.text = text;
            comment.longitude = longitude;
            comment.latitude = latitude;
            comment.city = city;
            comment.user = user;
            comment.post_id = post.id;

            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            comment.postedAt = new Date(currentDate.getTime());

            comment.votingValue = 0;

            Object[] jodelNumber = Comment.getJodelNumber(user.id, post.id, post.user.id);
            comment.jodelNumber = Long.parseLong((String.valueOf(jodelNumber[0])));
            comment.jodelNumberColor = (String) jodelNumber[1];

            comment.persistAndFlush();
            postController.updateCommentNumber(comment.post_id, 1);

            return comment;
        } catch (PersistenceException exception) {
            LOGGER.error("Comment creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }

        return null;
    }

    /**
     * Creates a new comment
     * @param longitude longitude
     * @param latitude latitude
     * @param city city
     * @param post post of the comment
     * @param user user of the comment
     * @return created Comment
     */
    @Transactional
    public Comment createImageComment(String imgbase64, Double longitude, Double latitude, String city, Post post, User user) {
        try {
            Comment comment = new Comment();
            comment.text = null;
            comment.type = Comment.TYPE.IMAGE;
            comment.longitude = longitude;
            comment.latitude = latitude;
            comment.city = city;
            comment.user = user;
            comment.post_id = post.id;

            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            comment.postedAt = new Date(currentDate.getTime());

            comment.votingValue = 0;

            Object[] jodelNumber = Comment.getJodelNumber(user.id, post.id, post.user.id);
            comment.jodelNumber = Long.parseLong((String.valueOf(jodelNumber[0])));
            comment.jodelNumberColor = (String) jodelNumber[1];

            comment.image = null;
            try {
                String[] encoded = imgbase64.split(",");
                byte [] barr = Base64.getDecoder().decode(encoded[1]);
                comment.image = barr;
            } catch (Exception e){
                e.printStackTrace();
                throw new HttpExceptions("NOT AN IMAGE", Response.Status.CONFLICT);
            }

            comment.persistAndFlush();
            postController.updateCommentNumber(comment.post_id, 1);

            return comment;
        } catch (PersistenceException | HttpExceptions exception) {
            LOGGER.error("Comment creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
        }

        return null;
    }

    /**
     * Updates voting
     * @param id id of the comment
     * @param t Voting.TYPE t
     * @param value the value
     * @return true if no errors, else false
     */
    @Transactional
    public boolean updateVoting(Long id, Voting.TYPE t, int value) {
        Comment comment = Comment.findById(id);
        if(comment != null) {
            try {
                comment.votingValue += value;
                comment.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Post Update Comment");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return false;
    }

    /**
     * Deletes Comment
     * @param comment given comment
     * @param value value of comment_numbers
     */
    @Transactional
    public void deleteComment(Comment comment, int value) {
        try {
            Query del = em.createQuery("DELETE FROM Comment c WHERE id = " +comment.id);
            del.executeUpdate();
            postController.updateCommentNumber(comment.post_id, value);
        } catch (SecurityException | IllegalStateException  e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes all comments from DB
     */
    @Transactional
    public void removeAllComments() {
    	try {

    	    Query del = em.createQuery("DELETE FROM Comment c");
    	    del.executeUpdate();

    	} catch (SecurityException | IllegalStateException  e) {
    	    e.printStackTrace();
    	}
    }
}