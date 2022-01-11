package de.hse.jodel;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import de.hse.jodel.controller.*;
import de.hse.jodel.model.*;
import de.hse.swa.jpa.jodel.controller.*;
import de.hse.swa.jpa.jodel.model.*;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class VotingResourceTest {

    @Inject
    AuthController authController;
    @Inject
    SessionController sessionController;
    @Inject
    CommentController commentController;
    @Inject
    PostController postController;
    @Inject
    UserController userController;
    @Inject
    VotingController votingController;

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);

    User user;
    Cookie cookie;

    /**
     * Creates a user vie the UserController
     * @return the created User
     */
    private User createUser() {
        try {
            return userController.createUser("password", "password", "first@one.de", "user");
        } catch (HttpExceptions httpExceptions) {
            httpExceptions.printStackTrace();
        }
        return null;
    }

    /**
     * Creates another user with different data via the UserController
     * @return the created User
     */
    private User createForeignUser() {
        try {
            return userController.createUser("password", "password", "second@one.de", "user");
        } catch (HttpExceptions httpExceptions) {
            httpExceptions.printStackTrace();
        }
        return null;
    }

    private Post createPost(String text, Double longitude, Double latitude, String city, String color, User user) {
        try {
            return postController.createPost(1,"hashtag", text, longitude, latitude, city, color, user);
        } catch (HttpExceptions httpExceptions) {
            httpExceptions.printStackTrace();
        }
        return null;
    }

    private Comment createComment(String text, Double longitude, Double latitude, String city, Post post, User user) {
        return commentController.createComment(text, longitude, latitude, city, post, user);
    }

    /**
     * Resets the DB
     */
    @BeforeEach
    public void clearAllFromDatabase() {
        userController.removeAllUsers();
    }

    /**
     * creates a new user and sets a session cookie
     */
    @BeforeEach
    public void prepare() {
        user = createUser();
        Session session = sessionController.createNewSession(user, request);
        NewCookie nC = authController.buildResponseCookie(session.token, NewCookie.DEFAULT_MAX_AGE);

        cookie = new Cookie.Builder(nC.getName(), nC.getValue())
                .setSecured(false)
                .setComment(nC.getComment())
                .build();
    }

    /**
     * Tests to create a vote on own created post -> was forbidden, now allowed (OK)
     */
    @Test
    public void createVoteOnOwnPost() {
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", user);

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "post"); // Cast
        requestParams.put("id", post.id);
        requestParams.put("vote", Voting.TYPE.DOWN);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote")
                .then()
                .statusCode(200);
    }

    /**
     * Tests to create a vote on own created comment -> was forbidden, now allowed (OK)
     */
    @Test
    public void createVoteOnOwnComment() {
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", user);
        Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, user);

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "comment"); // Cast
        requestParams.put("id", comment.id);
        requestParams.put("vote", Voting.TYPE.UP);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote")
                .then()
                .statusCode(200);
    }

    /**
     * Tests to create a vote with incorrect data -> Forbidden
     * Ex: Comment with wrong id
     */
    @Test
    public void createIncorrectVote() {
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", user);

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "comment"); // Cast
        requestParams.put("id", post.id);
        requestParams.put("vote", Voting.TYPE.UP);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote")
                .then()
                .statusCode(406);
    }

    /**
     * Tests to create a vote on foreign created post -> OK
     */
    @Test
    public void createVoteOnForeignPost() {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "post"); // Cast
        requestParams.put("id", post.id);
        requestParams.put("vote", Voting.TYPE.UP);

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);
        assertEquals(1, ((Post)Post.findById(post.id)).votingValue);
    }

    /**
     * Tests to create a vote on foreign created comment -> OK
     */
    @Test
    public void createVoteOnForeignComment() {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);
        Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, foreignUser);

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "comment"); // Cast
        requestParams.put("id", comment.id);
        requestParams.put("vote", Voting.TYPE.UP);

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);
        assertEquals(1, ((Comment)Comment.findById(comment.id)).votingValue);
    }

    /**
     * Tests the conversion of a vote at post -> OK
     * Ex:  upvote & upvote -> 0
     *      downvote & downvote -> 0
     */
    @Test
    public void revertVoteOnForeignPost() throws HttpExceptions {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);
        votingController.setVoting(post, user, Voting.TYPE.UP);         //first upvote

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "post"); // Cast
        requestParams.put("id", post.id);
        requestParams.put("vote", Voting.TYPE.UP); //second upvote

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);

        assertEquals(0, ((Post)Post.findById(post.id)).votingValue);
    }

    /**
     * Tests the conversion of a vote at comment -> OK
     * Ex:  upvote & upvote -> 0
     *      downvote & downvote -> 0
     */
    @Test
    public void revertVoteOnForeignComment() throws HttpExceptions {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);
        Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, foreignUser);
        votingController.setVoting(comment, user, Voting.TYPE.UP);         //first upvote

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "comment"); // Cast
        requestParams.put("id", comment.id);
        requestParams.put("vote", Voting.TYPE.UP); //second upvote

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);

        assertEquals(0, ((Comment)Comment.findById(comment.id)).votingValue);
    }

    /**
     * Tests to set opposite vote for a post-> OK
     */
    @Test
    public void createOppositeVoteOnForeignPost() throws HttpExceptions {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);
        votingController.setVoting(post, user, Voting.TYPE.UP);         //upvote

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "post"); // Cast
        requestParams.put("id", post.id);
        requestParams.put("vote", Voting.TYPE.DOWN); //downvote

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);

        assertEquals(-1, ((Post)Post.findById(post.id)).votingValue);
    }

    /**
     * Tests to set opposite vote for a comment -> OK
     */
    @Test
    public void createOppositeVoteOnForeignComment() throws HttpExceptions {
        User foreignUser = createForeignUser();
        Post post = createPost("Post1", 48.0, 9.0, "Albstadt", "#FF9908", foreignUser);
        Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, foreignUser);
        votingController.setVoting(comment, user, Voting.TYPE.UP);         //upvote

        JsonObject requestParams = new JsonObject();
        requestParams.put("postcomment", "comment"); // Cast
        requestParams.put("id", comment.id);
        requestParams.put("vote", Voting.TYPE.DOWN); //downvote

        Response response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .when()
                .body(requestParams.toString())
                .put("/vote");
        response.then().statusCode(200);

        assertEquals(-1, ((Comment)Comment.findById(comment.id)).votingValue);
    }
}

