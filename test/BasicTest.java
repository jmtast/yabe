import java.util.List;
import java.util.Map;

import models.Comment;
import models.Post;
import models.Tag;
import models.User;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.test.UnitTest;

public class BasicTest extends UnitTest {

	@Before
	public void setup() {
		Comment.deleteAll();
		Post.deleteAll();
		Tag.deleteAll();
		User.deleteAll();
	}

	@Test
	public void createAndRetrieveUser() {
		// Create a new user and save it
		new User("bob@gmail.com", "secret", "Bob").save();

		// Retrieve the user with e-mail address bob@gmail.com
		User bob = User.find("byEmail", "bob@gmail.com").first();

		// Test
		assertNotNull(bob);
		assertEquals("Bob", bob.fullname);
	}

	@Test
	public void tryConnectAsUser() {
		// Create a new user and save it
		new User("bob@gmail.com", "secret", "Bob").save();

		// Test
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNull(User.connect("bob@gmail.com", "badpassword"));
		assertNull(User.connect("bademail@gmail.com", "secret"));
	}

	@Test
	public void createPost() {
		// Create a new user and save it
		User bob = new User("bob@gmail.com", "secret", "Bob").save();

		// Create a new post
		new Post(bob, "My first post", "Hello world").save();

		// Test that the post has been created
		assertEquals(1, Post.count());

		// Retrieve all posts created by Bob
		// Change annotations from relational to morphia (reference)
		List<Post> bobPosts = Post.q().filter("author", bob).asList();

		// Tests
		assertEquals(1, bobPosts.size());
		Post firstPost = bobPosts.get(0);
		assertNotNull(firstPost);
		assertEquals(bob, firstPost.author);
		assertEquals("My first post", firstPost.title);
		assertEquals("Hello world", firstPost.content);
		assertNotNull(firstPost.postedAt);
	}

	@Test
	public void postComments() {
		// Create a new user and save it
		User bob = new User("bob@gmail.com", "secret", "Bob").save();

		// Create a new post
		Post bobPost = new Post(bob, "My first post", "Hello world").save();

		// Post a first comment
		new Comment(bobPost, "Jeff", "Nice post").save();
		new Comment(bobPost, "Tom", "I knew that !").save();

		// Retrieve all comments
		List<Comment> bobPostComments = Comment.find("post", bobPost).asList();

		// Tests
		assertEquals(2, bobPostComments.size());

		Comment firstComment = bobPostComments.get(0);
		assertNotNull(firstComment);
		assertEquals("Jeff", firstComment.author);
		assertEquals("Nice post", firstComment.content);
		assertNotNull(firstComment.postedAt);

		Comment secondComment = bobPostComments.get(1);
		assertNotNull(secondComment);
		assertEquals("Tom", secondComment.author);
		assertEquals("I knew that !", secondComment.content);
		assertNotNull(secondComment.postedAt);
	}

	// @Ignore("Failure, expected:<0> but was:<2>")
	@Test
	public void useTheCommentsRelation() {
		// Create a new user and save it
		User bob = new User("bob@gmail.com", "secret", "Bob").save();

		// Create a new post
		Post bobPost = new Post(bob, "My first post", "Hello world").save();

		// Post a first comment
		bobPost.addComment("Jeff", "Nice post");
		bobPost.addComment("Tom", "I knew that !");

		// Count things
		assertEquals(1, User.count());
		assertEquals(1, Post.count());
		assertEquals(2, Comment.count());

		// Retrieve Bob's post
		bobPost = Post.find("author", bob).first();
		assertNotNull(bobPost);

		// Navigate to comments
		assertEquals(2, bobPost.comments.size());
		assertEquals("Jeff", bobPost.comments.get(0).author);

		// Delete the comments
		// XXX: Ask for better way to do this
		List<Comment> bobPostComments = bobPost.comments;
		for (Comment comment : bobPostComments) {
			comment.delete();
		}
		// Delete the post
		bobPost.delete();

		// Check that all comments have been deleted
		assertEquals(1, User.count());
		assertEquals(0, Post.count());
		assertEquals(0, Comment.count());
	}

	// @Ignore("A java.lang.RuntimeException has been caught, Cannot load fixture data.yml: Cannot load fixture data.yml, duplicate id 'bob' for type models.User")
	@Test
	public void fullTest() {
		loadModels();

		// Count things
		assertEquals(2, User.count());
		assertEquals(3, Post.count());
		assertEquals(3, Comment.count());

		// Try to connect as users
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNotNull(User.connect("jeff@gmail.com", "secret"));
		assertNull(User.connect("jeff@gmail.com", "badpassword"));
		assertNull(User.connect("tom@gmail.com", "secret"));

		// Find all of Bob's posts
		User bob = User.find("byEmail", "bob@gmail.com").first();
		List<Post> bobPosts = Post.find("author", bob).asList();
		assertEquals(2, bobPosts.size());

		// Find all comments related to Bob's posts
		List<Comment> bobComments = Comment.find("postAuthor.email", "bob@gmail.com").asList();
		assertEquals(3, bobComments.size());

		// Find the most recent post
		Post frontPost = Post.q().order("postedAt").first();
		assertNotNull(frontPost);
		assertEquals("About the model layer", frontPost.title);

		// Check that this post has two comments
		assertEquals(2, frontPost.comments.size());

		// Post a new comment
		frontPost.addComment("Jim", "Hello guys");
		assertEquals(3, frontPost.comments.size());
		assertEquals(4, Comment.count());
	}

	@Ignore("A org.mongodb.morphia.query.ValidationException has been caught, The field 'select' could not be "
			+ "found in 'models.Post' while validating - select; if you wish to continue please disable validation.")
	@Test
	public void testTags() {
		// Create a new user and save it
		User bob = new User("bob@gmail.com", "secret", "Bob").save();

		// Create a new post
		Post bobPost = new Post(bob, "My first post", "Hello world").save();
		Post anotherBobPost = new Post(bob, "Hop", "Hello world").save();

		// Well
		assertEquals(0, Post.findTaggedWith("Red").size());

		// Tag it now
		bobPost.tagItWith("Red").tagItWith("Blue").save();
		anotherBobPost.tagItWith("Red").tagItWith("Green").save();

		// Check
		assertEquals(2, Post.findTaggedWith("Red").size());
		assertEquals(1, Post.findTaggedWith("Blue").size());
		assertEquals(1, Post.findTaggedWith("Green").size());

		// Check multiple tags on a single post
		assertEquals(1, Post.findTaggedWith("Red", "Blue").size());
		assertEquals(1, Post.findTaggedWith("Red", "Green").size());
		assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size());
		assertEquals(0, Post.findTaggedWith("Green", "Blue").size());

		// Check tag cloud count
		List<Map> cloud = Tag.getCloud();
		assertEquals("[{pound=1, tag=Blue}, {pound=1, tag=Green}, {pound=2, tag=Red}]", cloud.toString());
	}

	private void loadModels() {

		// Create Users
		// This user is not admin, and maybe it should
		User bob = new User("bob@gmail.com", "secret", "Bob").save();
		User jeff = new User("jeff@gmail.com", "secret", "Jeff").save();

		// Create Posts
		Post firstBobPost = new Post(bob, "About the model layer",
				"The model has a central position in a Play! application. It is the domain-specific"
						+ "representation of the information on which the application operates." + ""
						+ "Martin fowler defines it as:" + ""
						+ "Responsible for representing concepts of the business, information about the "
						+ "business situation, and business rules. State that reflects the business situation "
						+ "is controlled and used here, even though the technical details of storing it are "
						+ "delegated to the infrastructure. This layer is the heart of business software.").save();

		Post secondBobPost = new Post(bob, "Just a test of YABE", "Well, it's just a test.").save();

		Post jeffPost = new Post(jeff, "The MVC application",
				"A Play! application follows the MVC architectural pattern as applied to the "
						+ "architecture of the Web." + ""
						+ "This pattern splits the application into separate layers: the Presentation "
						+ "layer and the Model layer. The Presentation layer is further split into a "
						+ "View and a Controller layer.").save();

		// Create Comments
		firstBobPost.addComment("Guest", "You are right !");
		firstBobPost.addComment("Mike", "I knew that ...");
		secondBobPost.addComment("Tom", "This post is useless ?");
	}
}
