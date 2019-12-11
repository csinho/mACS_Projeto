package models.form;

import java.util.List;

import models.domain.Domain;
import models.domain.DomainData;

public class UserFormsPacked {
	private String entitySlug;
	private String description;
    private List<Form> items;
    private Boolean emptyExistingItems = true;

    public String getEntitySlug() {
		return entitySlug;
	}

	public void setEntitySlug(String entitySlug) {
		this.entitySlug = entitySlug;
	}
    
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UserFormsPacked(){
    	entitySlug = "form";
    	description = "Formulários salvos";
    }
    
	public UserFormsPacked(List<Form> items, Boolean emptyExistingItems) {		
		entitySlug = "form";
		description = "Formulários salvos";
		this.emptyExistingItems = emptyExistingItems;
		this.items = items;
	}
		
	public List<Form> getItems() {
		return items;
	}
	
	public void setItems(List<Form> items) {
		this.items = items;
	}
	
	public Boolean getEmptyExistingItems() {
		return emptyExistingItems;
	}
	
	public void setEmptyExistingItems(Boolean emptyExistingItems) {
		this.emptyExistingItems = emptyExistingItems;
	}
}
