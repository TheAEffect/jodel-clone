package de.hse.jodel;

import de.hse.jodel.controller.AuthController;
import de.hse.jodel.controller.SessionController;
import de.hse.jodel.controller.UserController;
import de.hse.jodel.model.Session;
import de.hse.jodel.model.User;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import static io.restassured.RestAssured.given;

@QuarkusTest
class UserResourceTest {

	@Inject
    AuthController authController;
	@Inject
    SessionController sessionController;
    @Inject
    UserController userController;

	HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

	User user;
	User user2;
	Cookie cookie;

	/**
	 * Creates a user vie the UserController
	 * @return the created User
	 */
	private User createUser() {
		try {
			return userController.createUser("First", "password", "password", "first@one.de", "admin");
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
			return userController.createUser("Second", "password", "password", "second@one.de", "user");
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
		user2 = createForeignUser();
		Session session = sessionController.createNewSession(user, request);
		NewCookie nC = authController.buildResponseCookie(session.token, NewCookie.DEFAULT_MAX_AGE);

		cookie = new Cookie.Builder(nC.getName(), nC.getValue())
				.setSecured(false)
				.setComment(nC.getComment())
				.build();
	}

	/**
	 * Tests to get whoami -> ok
	 */
	@Test
	public void whoamiTest() {
		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.get("/user/whoami")
				.then()
				.statusCode(200);
	}

	/**
	 * Tests to get all users -> OK
	 */
	@Test
	void getAllUsers() throws HttpExceptions {
		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.get("/user")
				.then()
				.statusCode(200);

	}

	/**
	 * Tests to update the own userdata -> ok
	 */
	@Test
	void updateUserTest() throws HttpExceptions {
		JsonObject requestParams = new JsonObject();
		requestParams.put("username", "First1");

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.body(requestParams.toString())
				.patch("/user")
				.then()
				.statusCode(200);
	}

	/**
	 * Tests to create a new user -> created
	 */
	@Test
	void createUserTest() throws HttpExceptions {
		JsonObject requestParams = new JsonObject();
		requestParams.put("username", "Created");
		requestParams.put("password", "password");
		requestParams.put("password_confirmation", "password");
		requestParams.put("email", "created@test.de");
		requestParams.put("role", "user");

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.body(requestParams.toString())
				.put("/user")
				.then()
				.statusCode(201);
	}

	/**
	 * Tests to create a new user -> created
	 */
	@Test
	void removeUserTest() throws HttpExceptions {

		given()
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie)
				.when()
				.delete("/user/Second")
				.then()
				.statusCode(200);
	}
}