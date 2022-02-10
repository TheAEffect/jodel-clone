package de.hse.jodel;

import java.util.List;

import javax.inject.Inject;

import de.hse.jodel.controller.*;
import de.hse.swa.jpa.jodel.controller.*;
import de.hse.jodel.utils.exception.HttpExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hse.jodel.model.User;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserControllerTest {
	@Inject
    VotingController votingController;
	@Inject
    CommentController commentController;

	@Inject
    SessionController sessionController;

    @Inject
    UserController userController;

	@Inject
    PostController postController;

	public void addTwoUsers() throws HttpExceptions {
		User first = userController.createUser("password","password",
				"first@two.de", "admin");
		User second = userController.createUser( "password", "password",
				"second@two.de", "admin");
	}

	
	private void printUser(User user) {
		System.out.println("id: " + user.id);
	}
	
	@BeforeEach
	public void clearAllFromDatabase() {
		votingController.removeAllVotings();
		commentController.removeAllComments();
		postController.removeAllPosts();
		sessionController.removeAllSessions();
		userController.removeAllUsers();
	}

	@Test
	void addUser_1() throws HttpExceptions {
		User first = userController.createUser("password", "password",
				"first@one.de", "admin");
		List<User> users = User.listAll();
		assertEquals(users.size(),1);
		printUser(users.get(0));
	}
	
	@Test
	void addUser_2() throws HttpExceptions {
		addTwoUsers();
		List<User> users = User.listAll();
		assertEquals(users.size(),2);
		printUser(users.get(1));
	}

	@Test
	void findByUsername() throws HttpExceptions {
		User first = userController.createUser( "password", "password",
				"first@one.de", "admin");
		List<User> users = User.listAll();
		printUser(users.get(0));


		User test = User.findByUsername("First");
	}

	@Test
	void uniqueUser() {
		try {
			User first = userController.createUser("password", "password",
					"first@one.de", "admin");
		} catch (HttpExceptions httpExceptions) {
			httpExceptions.printStackTrace();
		}

		assertThrows(HttpExceptions.class, () -> userController.createUser( "password",
				"password","first@one.de", "admin"));
	}

	@Test
	void updateUser() throws HttpExceptions {
		User first = userController.createUser( "password", "password",
				"first@one.de", "admin");
		List<User> users = User.listAll();
		printUser(users.get(0));

		User updated = userController.updateUser(first.id, "password",
				"secondPw","secondPw",  false, "user");

		assertEquals("first_update@one.de", updated.email);
	}

	@Test
	void deleteUser() throws HttpExceptions {
		User first = userController.createUser("password", "password",
				"first@one.de", "admin");
		List<User> users = User.listAll();
		printUser(users.get(0));

		userController.removeUser(first.id);
		List<User> users2 = User.listAll();
		assertEquals(0, users2.size());
	}
}