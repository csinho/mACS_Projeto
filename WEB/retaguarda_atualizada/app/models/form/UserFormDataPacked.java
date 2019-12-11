package models.form;

import java.util.List;

import models.domain.Domain;
import models.domain.DomainData;

public class UserFormDataPacked {
	private String entitySlug;
	private String description;
    private List<FormData> items;
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

	public UserFormDataPacked(){
    	entitySlug = "formData";
    	description = "Dados de formulários salvos";
    }
    
	public UserFormDataPacked(List<FormData> items, Boolean emptyExistingItems) {		
		entitySlug = "formData";
		description = "Dados de formulários salvos";
		this.emptyExistingItems = emptyExistingItems;
		this.items = items;
	}
		
	public List<FormData> getItems() {
		return items;
	}
	
	public void setItems(List<FormData> items) {
		this.items = items;
	}
	
	public Boolean getEmptyExistingItems() {
		return emptyExistingItems;
	}
	
	public void setEmptyExistingItems(Boolean emptyExistingItems) {
		this.emptyExistingItems = emptyExistingItems;
	}
}
