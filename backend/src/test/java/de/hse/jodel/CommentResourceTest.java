package de.hse.jodel;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.put;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import de.hse.jodel.controller.*;
import de.hse.jodel.model.Comment;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import de.hse.swa.jpa.jodel.controller.*;
import de.hse.swa.jpa.jodel.model.*;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.mockito.Mockito;

@QuarkusTest
class CommentResourceTest {

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

	HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

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

	/**
	 * Creates a Post via the postController
	 * @return the created Post
	 */
	private Post createPost(String hashtag, String text, Double longitude, Double latitude, String city, String color, User user) {
		try {
			return postController.createPost(1, hashtag, text, longitude, latitude, city, color, user);
		} catch (HttpExceptions httpExceptions) {
			httpExceptions.printStackTrace();
		}
		return null;
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
	 * Tests to create a comment -> created
	 */
	@Test
	public void createComment() {
		Post post = createPost("hashtag","Post1", 48.0, 9.0, "Albstadt", "#FF9908", user);
		JsonObject requestParams = new JsonObject();
		requestParams.put("text", "Kommentar1"); // Cast
		requestParams.put("longitude", 9.1);
		requestParams.put("latitude", 48.1);
		requestParams.put("city", "Albstadt");

		Response response = given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.body(requestParams.toString())
				.put("/comment/"+post.id);
		response.then().statusCode(201);
		assertEquals(1, ((Post)Post.findById(post.id)).comment_number);
	}

	/**
	 * Tests to create a comment on a missing post -> Not acceptable
	 */
	@Test
	public void createCommentOnMissingPost() {
		JsonObject requestParams = new JsonObject();
		requestParams.put("text", "Kommentar1"); // Cast
		requestParams.put("longitude", 9.1);
		requestParams.put("latitude", 48.1);

		Random random = new Random();
		int randomNum = random.nextInt((10000 - 9000)) + 9000;

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.body(requestParams.toString())
				.put("/comment/"+randomNum)
				.then()
				.statusCode(406);
	}

	/**
	 * Tests to delete an own created comment -> OK
	 */
	@Test
	public void deleteComment() {
		Post post = createPost("hashtag","Post1", 48.0, 9.0, "Albstadt", "#FF9908", user);
		Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, user);

		Response response = given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/comment/"+comment.id);
		response.then().statusCode(200);
		assertEquals(0, ((Post)Post.findById(post.id)).comment_number);
	}

	/**
	 * Tests to delete a missing comment -> not found
	 */
	@Test
	public void deleteMissingComment() {
		Random random = new Random();
		int randomNum = random.nextInt((10000 - 9000)) + 9000;

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/comment/"+randomNum)
				.then()
				.statusCode(404);
	}

	/**
	 * Tests to delete a post created by another user -> forbidden
	 */
	@Test
	public void deleteForeignComment() throws HttpExceptions {
		User foreignUser = createForeignUser();
		Post post = postController.createPost(1, "hashtag", "Beitrag1", 9.0, 48.2, "Albstadt", "#FF9908", user);
		Comment comment = commentController.createComment("Kommentar1",48.0,9.0, "Albstadt", post, foreignUser);

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/comment/"+comment.id)
				.then()
				.statusCode(403);
	}
}

