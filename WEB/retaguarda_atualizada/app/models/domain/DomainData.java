package models.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


import models.general.Item;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

/**
 * Represents the domain data associated with a domain item.
 * In other words, it is each of the rows of the respective domain item
 * @author jairocalmon
 */
@Entity(name="domaindata")
public class DomainData extends Item {
	@ManyToOne
	@JoinColumn(name = "domaincode")
	private Domain domain;

	private String value;
	
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	/**
	 * Get the packed list of domain data given a domain.
	 * @param domain List of domain items to pack
	 * @return packed domain data list
	 */
	public static List<PackedDomainData> getPackedList(List<Domain> domains){
		List<PackedDomainData> packedList = new ArrayList<>();
		
		// Iterate the domain itens and fetch the respective domain data
		for (Domain domain : domains) {
			Long domainId = domain.getId();
			List<QueryFilter> filters = new ArrayList<QueryFilter>();
			filters.add(new QueryFilter("domaincode", domainId, OperatorType.EQUAL));	
			WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);			
			List<DomainData> domainData = DomainData.find(wv.where, wv.values).fetch();
			
			// Build a packed domain data to encapsulate our data
			PackedDomainData packed = new PackedDomainData(domain, domainData, true);
			
			packedList.add(packed);
		}		
		return packedList;
	}
}