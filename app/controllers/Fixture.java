package controllers;

import models.Post;
import models.User;

public class Fixture {
	public static void loadModels() {

		// Create Users
		User bob = new User("bob@gmail.com", "secret", "Bob").save();
		bob.isAdmin = true;
		bob.save();
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
