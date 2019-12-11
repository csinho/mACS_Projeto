package models.domain;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

/**
 * Represents each of the domain types of the system.
 * eg.: unidadefederativa, cbo etc.
 * @author jairocalmon
 */
@Entity(name="domain")
public class Domain extends Model {
	private String slug;
	private String description;
	
	@Column(nullable = true, name="updatedat")
	private Date updatedAt;
	
	public String getSlug() {
		return slug;
	}
	
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
		
	
	/**
	 * Return the packed domain data.
	 * Iterate the domain items and fetch the respective data.
	 * return a list of PackedDomainData
	 * @throws ParseException 
	 */
	public static List<PackedDomainData> packedData(List<QueryFilter> filters) throws ParseException{
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<Domain> domains = Domain.find(wv.where, wv.values).fetch();
		List<PackedDomainData> domainDataPacked = DomainData.getPackedList(domains);
		return domainDataPacked;		
	}
	
}
