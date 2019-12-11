package models.domain;

import java.util.ArrayList;
import java.util.List;



public class PackedDomainData {
    private Domain domain;
    private List<DomainData> items;
    private Boolean emptyExistingItems = true;

    
    public PackedDomainData(){
    	this.items = new ArrayList<DomainData>();
    }
    
	public PackedDomainData(Domain domain, List<DomainData> items, Boolean emptyExistingItems) {
		this.domain = domain;
		this.emptyExistingItems = emptyExistingItems;
		this.items = items;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public List<DomainData> getItems() {
		return items;
	}
	
	public void setItems(List<DomainData> items) {
		this.items = items;
	}
	public void addItems(List<DomainData> items) {
		this.items.addAll(items);
	}
	
	public Boolean getEmptyExistingItems() {
		return emptyExistingItems;
	}
	
	public void setEmptyExistingItems(Boolean emptyExistingItems) {
		this.emptyExistingItems = emptyExistingItems;
	}
}