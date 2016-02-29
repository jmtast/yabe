package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Lob;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.modules.morphia.Model;

@Entity
public class Post extends Model {

	@Required
	public String title;
	@Required
	public Date postedAt;

	@Lob
	@Required
	@MaxSize(10000)
	public String content;

	@Required
	@Reference
	public User author;

	// @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@Embedded
	public List<Comment> comments;

	// @ManyToMany(cascade = CascadeType.PERSIST)
	@Reference
	public Set<Tag> tags;

	public Post(User author, String title, String content) {
		this.comments = new ArrayList<Comment>();
		this.tags = new TreeSet<Tag>();
		this.author = author;
		this.title = title;
		this.content = content;
		this.postedAt = new Date();
	}

	public Post addComment(String author, String content) {
		Comment newComment = new Comment(this, author, content).save();
		this.comments.add(newComment);
		this.save();
		return this;
	}

	public Post previous() {
		return Post.find("postedAt < ? order by postedAt desc", postedAt).first();
	}

	public Post next() {
		return Post.find("postedAt > ? order by postedAt asc", postedAt).first();
	}

	public Post tagItWith(String name) {
		tags.add(Tag.findOrCreateByName(name));
		return this;
	}

	public static List<Post> findTaggedWith(String tagName) {
		Tag tag = Tag.filter("name", tagName).first();
		if (tag != null) {
			MorphiaQuery q = Post.q();
			q.field("tags").hasThisElement(tag);
			return q.asList();
		}
		return new ArrayList();
	}

	public static List<Post> findTaggedWith(String... tags) {
		return Post.q().asList();
		// return Post
		// .find("select distinct p from Post p join p.tags as t where t.name in (:tags) group by p.id, p.author, p.title, p.content,p.postedAt having count(t.id) = :size")
		// .bind("tags", tags).bind("size", tags.length).fetch();
	}

	public String toString() {
		return title;
	}

}