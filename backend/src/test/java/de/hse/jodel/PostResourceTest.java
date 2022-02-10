package de.hse.jodel;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import de.hse.jodel.controller.AuthController;
import de.hse.jodel.controller.PostController;
import de.hse.jodel.controller.SessionController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.Post;
import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import de.hse.swa.jpa.jodel.controller.*;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.mockito.Mockito;

@QuarkusTest
class PostResourceTest {

	@Inject
    AuthController authController;
	@Inject
    SessionController sessionController;
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
			return userController.createUser( "password", "password", "first@one.de", "user");
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
			return userController.createUser( "password", "password", "second@one.de", "user");
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
	 * Tests to create a post -> created
	 */
	@Test
	public void createPost() {
		JsonObject requestParams = new JsonObject();
		requestParams.put("text", "Beitrag1"); // Cast
		requestParams.put("longitude", 9.0);
		requestParams.put("latitude", 48.2);
		requestParams.put("city", "Albstadt");
		requestParams.put("color", "#FF9908");

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.body(requestParams.toString())
				.put("/post")
				.then()
				.statusCode(201);
	}

	/**
	 * Tests to delete an own created post -> OK
	 */
	@Test void deleteOwnPost() throws HttpExceptions {
		Post post = postController.createPost(1,"hashtag", "Beitrag1", 9.0, 48.2, "Albstadt", "#FF9908", user);

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/post/"+post.id)
				.then()
				.statusCode(200);

	}

	/**
	 * Tests to delete a post created by another user -> forbidden
	 */
	@Test void deleteForeignPost() throws HttpExceptions {
		User foreignUser = createForeignUser();
		Post post = postController.createPost(1,"hashtag", "Beitrag2", 9.0, 48.2, "Albstadt","#FF9908", foreignUser);

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/post/"+post.id)
				.then()
				.statusCode(403);
	}

	/**
	 * Tests to delete a missing post -> Not found
	 */
	@Test void deleteMissingPost() throws HttpExceptions {
		Random random = new Random();
		int randomNum = random.nextInt((10000 - 9000)) + 9000;

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/post/"+randomNum)
				.then()
				.statusCode(404);
	}
	
	/**
	 * Tests to get posts with disabled autoDistance -> OK, only 1 result (due to small radius)
	 */
	@Test void getPostsAutoDistanceDisabled() throws HttpExceptions {
		postController.createPost(1, "hashtag", "Beitrag1", 9.0, 48.0, "Buchheim","#FF9908",  user); 	//0km
		postController.createPost(1,"hashtag", "Beitrag2", 9.5, 48.5,"Römerstein","#FF9908", user);		//67km
		postController.createPost(1,"hashtag", "Beitrag3", 9.0, 48.2, "Albstadt","#FF9908", user);		//22km
		User foreignUser = createForeignUser();
		postController.createPost(1,"hashtag", "Beitrag4", 10.5, 48.2, "Eppishausen","#FF9908", foreignUser);	//114km
		postController.createPost(1,"hashtag", "Beitrag5", 9.5, 48.8, "Schorndorf","#FF9908", foreignUser);	//96km

		Response response = given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.queryParam("latitude", 48.0)
				.queryParam("longitude", 9.0)
				.get("/post");
		response.then().statusCode(200);
		JsonArray jsonArray= new JsonArray(response.getBody().asString());
		assertEquals(1, jsonArray.size());
	}

	/**
	 * Tests to get posts with enabled autoDistance -> OK, 4 results (due to growing radius)
	 */
	@Test void getPostsAutoDistanceEnabled() throws HttpExceptions {
		userController.updateUser(user.id,"","","",true, "user");
		postController.createPost(1,"hashtag", "Beitrag1", 9.0, 48.0, "Buchheim","#FF9908",user); 	//0km
		postController.createPost(1,"hashtag", "Beitrag2", 9.5, 48.5,"Römerstein","#FF9908", user);		//67km
		postController.createPost(1,"hashtag", "Beitrag3", 9.0, 48.2, "Albstadt","#FF9908", user);		//22km
		User foreignUser = createForeignUser();
		postController.createPost(1,"hashtag", "Beitrag4", 10.5, 48.2, "Eppishausen","#FF9908", foreignUser);	//114km
		postController.createPost(1,"hashtag", "Beitrag5", 9.5, 48.8, "Schorndorf","#FF9908", foreignUser);	//96km

		Response response = given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.queryParam("latitude", 48.0)
				.queryParam("longitude", 9.0)
				.get("/post");
		response.then().statusCode(200);
		JsonArray jsonArray= new JsonArray(response.getBody().asString());
		assertEquals(4, jsonArray.size());
	}
}