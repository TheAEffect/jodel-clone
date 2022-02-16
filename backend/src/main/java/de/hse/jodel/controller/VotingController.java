package de.hse.jodel.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.User;
import de.hse.jodel.model.Voting;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class VotingController {

    @Inject
    PostController postController;

    @Inject
    CommentController commentController;

    /**
     * Votes for a post (upvote or downvote)
     *
     * @param post Post to vote on
     * @param user User casting the vote
     * @param type Voting.TYPE (UP or DOWN)
     * @return JsonObject containing vote details or null if user has already voted
     */
    @Transactional
    public JsonObject votePost(Post post, User user, Voting.TYPE type) {
        if (post == null || user == null || type == null) {
            return null;
        }

        if (Voting.hasVoted(post, user)) {
            return null;
        }

        Voting voting = new Voting();
        voting.post = post;
        voting.user = user;
        voting.type = type;
        voting.persistAndFlush();

        int value = (type == Voting.TYPE.UP) ? 1 : -1;
        postController.updateVoting(post.id, type, value);

        return new JsonObject()
                .put("type", type.name())
                .put("yours", true);
    }

    /**
     * Votes for a comment (upvote or downvote)
     *
     * @param comment Comment to vote on
     * @param user User casting the vote
     * @param type Voting.TYPE (UP or DOWN)
     * @return JsonObject containing vote details or null if user has already voted
     */
    @Transactional
    public JsonObject voteComment(Comment comment, User user, Voting.TYPE type) {
        if (comment == null || user == null || type == null) {
            return null;
        }

        if (Voting.hasVoted(comment, user)) {
            return null;
        }

        Voting voting = new Voting();
        voting.comment = comment;
        voting.user = user;
        voting.type = type;
        voting.persistAndFlush();

        int value = (type == Voting.TYPE.UP) ? 1 : -1;
        commentController.updateVoting(comment.id, type, value);

        return new JsonObject()
                .put("type", type.name())
                .put("yours", true);
    }
}