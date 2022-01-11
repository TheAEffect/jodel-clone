package de.hse.jodel.controller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.User;
import de.hse.jodel.model.Voting;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;


@ApplicationScoped
public class VotingController{

    @Inject
    PostController postController;

    @Inject
    CommentController commentController;

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(PostController.class);

    public List<Voting> getVotings(Object e) {
        return Voting.getVotings(e);
    }

    public Voting getVoting(Long id) {
        return em.find(Voting.class, id);
    }

    /**
     * Creates Voting
     * @param e Object Post or Comment
     * @param user the user of the voting
     * @param t Voting.TYPE
     * @return created Voting
     * @throws HttpExceptions error
     */
    @Transactional
    public Voting createVoting(Object e, User user, Voting.TYPE t) throws HttpExceptions {
        Voting voting = new Voting();
        voting.user = user;
        voting.type = t;
        if (e instanceof Post) {
            voting.post = (Post) e;
        } else if(e instanceof Comment){
            voting.comment = (Comment) e;
        } else {
            return null;
        }
        voting.persistAndFlush();
        return voting;
    }

    /**
     * Sets Voting
     * @param e Object Post or Comment
     * @param user the user of the voting
     * @param t Voting.TYPE
     * @return Object with values if everything is fine
     * @throws HttpExceptions error
     */
    @Transactional
    public Object[] setVoting(Object e, User user, Voting.TYPE t) throws HttpExceptions {
        List<Voting> v = getVotings(e);
        Object[] arr = new Object[2];
        //if(Voting.hasPermission(e, user, t)) {

        Optional<Voting> voting = v.stream().filter(o -> o.user.equals(user)).findFirst();
                //anyMatch(o -> o.user.equals(user) && o.type.equals(t));
        //if Vote exist
        if(voting.isPresent()) {
            arr[0] = false;
            arr[1] = false;
            return arr;
        //if NO Vote exist
        } else {
            int value = 1;
            if(t.equals(Voting.TYPE.DOWN)) {
                value = -1;
            }

            JsonObject val = new JsonObject();
            val.put("type", "UP");
            val.put("yours", true);
            if(t.equals(Voting.TYPE.DOWN)) {
                val.put("type", "DOWN");
            }
            createVoting(e, user, t);
            if(e instanceof Post) {
                postController.updateVoting(((Post)e).id, t, value);
            } else {
                commentController.updateVoting(((Comment)e).id, t, value);
            }
            arr[0] = true;
            arr[1] = val;
            return arr;
        }
        //}
        //arr[0] = false;
        //arr[1] = null;
        //return arr;
    }

    /**
     * Removes Voting by given id
     * @param id voteID
     * @return true if no errors, else false
     */
    @Transactional
    public boolean removeVoting(Long id) {
        return Voting.deleteById(id);
    }

    /**
     * Removes all votings
     */
    @Transactional
    public void removeAllVotings() {
        try {

            Query del = em.createQuery("DELETE FROM Voting");
            del.executeUpdate();

        } catch (SecurityException | IllegalStateException  e) {
            e.printStackTrace();
        }
    }
}