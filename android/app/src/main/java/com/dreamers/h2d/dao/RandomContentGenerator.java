package com.dreamers.h2d.dao;

import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.dao.model.User;

import java.util.Random;
import java.util.UUID;

public class RandomContentGenerator {
	private static final String[] NAMES = new String[] {"Alexandria", "Beatrice", "Carmen", "Diego", "Ema", "Farah", "Georg", "Hadrian"};
	private static final String[] TOPICS = new String[] {"UML", "design patterns", "data structures", "persistent data",
			"modelling", "software construction", "exam", "mini project", "group project", "singleton", "observer",
			"factory", "strategy", "state", "facade", "DAO", "IntelliJ", "Android Studio", "AVL tree", "tree balancing",
			"concurrency"};
	private static final String[] POST_NAMES = new String[] {"Question about %s", "I love %s", "Study session: %s",
			"Practicing %s", "I don't understand %s", "Applications of %s", "How to implement %s?"};

	/**
	 * Fills the DAOs with a reasonable amount of data, for testing purposes
	 */
	public static void populateRandomData() {
		// Generate users
		for (int i = 0; i < 20; i++) {
			RandomContentGenerator.generateUser();
		}

		// Each user gets 1-3 posts
		java.util.Iterator<User> userIt = UserDAO.getInstance().getAll();
		while (userIt.hasNext()) {
			User u = userIt.next();
			int postCount = 1 + random.nextInt(3);
			for (int i = 0; i < postCount; i++) {
				String postName = String.format(
						chooseRandomFromArray(POST_NAMES),
						chooseRandomFromArray(TOPICS));
				Post post = new Post(UUID.randomUUID(), u.getUUID(), postName);
				PostDAO.getInstance().add(post);
			}
		}

		// 50 messages spread across posts
		for (int i = 0; i < 250; i++) {
			RandomContentGenerator.generateComment();
		}
	}

	private static final Random random = new Random();

	/**
	 * Uniformly randomly selects an item from a generic array
	 * @param array the array of objects to choose from
	 * @return a random element, or a RuntimeException if array is empty or null
	 * @param <T> the type of array
	 */
	private static <T> T chooseRandomFromArray(T[] array) {
		if (array == null || array.length == 0)
			throw new RuntimeException("Targeted array is empty");
		return array[random.nextInt(array.length)];
	}

	public static void generateUser() {
		String username = chooseRandomFromArray(NAMES) + random.nextInt(1000);
		User.Role role = random.nextInt(4) == 0 ? User.Role.Admin : User.Role.Member;
		User user = new User(UUID.randomUUID(), role, username, generatePassword());
		UserDAO.getInstance().add(user);
	}

	private static String generatePassword() {
		return "password" + random.nextInt(10000);
	}

	public static void generatePost() {
		User user = UserDAO.getInstance().getRandom();
		if (user == null) return;
		String postName = String.format(
				chooseRandomFromArray(POST_NAMES),
				chooseRandomFromArray(TOPICS)
		);		Post post = new Post(UUID.randomUUID(), user.getUUID(), postName);
		PostDAO.getInstance().add(post);
	}

	public static void generateComment() {
		generateComment(PostDAO.getInstance().getRandom());
	}

	public static void generateComment(Post post) {
		User user = UserDAO.getInstance().getRandom();
		if (post == null || user == null) return;

		String content = String.format("Hello from %s", user.username());

		long timestamp = System.currentTimeMillis() - random.nextInt(200000);

		Message message = new Message(UUID.randomUUID(), user.getUUID(), post.getUUID(), timestamp, content);
		post.messages.insert(message);
		com.dreamers.h2d.dao.MessageDAO.getInstance().add(message);
	}
}
