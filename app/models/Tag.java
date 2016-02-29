package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Entity;

import play.data.validation.Required;
import play.modules.morphia.Model;

@Entity
public class Tag extends Model implements Comparable<Tag> {

	@Required
	public String name;

	private Tag(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public int compareTo(Tag otherTag) {
		return name.compareTo(otherTag.name);
	}

	public static Tag findOrCreateByName(String name) {
		Tag tag = Tag.find("byName", name).first();
		if (tag == null) {
			tag = new Tag(name);
			tag.save();
		}
		return tag;
	}

	public static List<Map> getCloud() {
		return new ArrayList<Map>();
		// List<Map> result = Tag
		// .find("select new map(t.name as tag, count(p.id) as pound) from Post p join p.tags as t group by t.name order by t.name")
		// .fetch();
		// return result;
	}
}