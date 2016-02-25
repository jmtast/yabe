package models;

import java.util.Date;

import javax.persistence.Lob;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.modules.morphia.Model;

@Entity
public class Comment extends Model {

	@Required
	public String author;

	@Required
	public Date postedAt;

	@Lob
	@Required
	@MaxSize(10000)
	public String content;

	@Required
	@Reference
	public Post post;

	@Required
	public User postAuthor;

	public Comment(Post post, String author, String content) {
		this.post = post;
		this.author = author;
		this.content = content;
		this.postedAt = new Date();
		this.postAuthor = post.author;
	}

	public String toString() {
		return postedAt.toString() + " by " + author;
	}

}