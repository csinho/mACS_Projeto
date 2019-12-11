package models.form;

import java.util.List;

import models.domain.Domain;
import models.domain.DomainData;

public class UserFormSearchablePacked {
	private String entitySlug;
	private String description;
    private List<FormSearchable> items;
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

	public UserFormSearchablePacked(){
    	entitySlug = "formSearchable";
    	description = "Dados buscáveis de formulários salvos";
    }
    
	public UserFormSearchablePacked(List<FormSearchable> items, Boolean emptyExistingItems) {		
		entitySlug = "formSearchable";
		description = "Dados buscáveis de formulários salvos";
		this.emptyExistingItems = emptyExistingItems;
		this.items = items;
	}
		
	public List<FormSearchable> getItems() {
		return items;
	}
	
	public void setItems(List<FormSearchable> items) {
		this.items = items;
	}
	
	public Boolean getEmptyExistingItems() {
		return emptyExistingItems;
	}
	
	public void setEmptyExistingItems(Boolean emptyExistingItems) {
		this.emptyExistingItems = emptyExistingItems;
	}
}
