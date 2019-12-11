package models.form;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import models.general.Item;
import types.JSONObjectUserType;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

/**
 * Type of form
 * @author joaoraf
 *
 */
@Entity(name="formTemplate")
@TypeDef(name = "CustomJsonObject", typeClass = JSONObjectUserType.class)
public class FormTemplate extends Item {	
	
	@Type(type = "CustomJsonObject")
	@Column(columnDefinition="json")
	protected Object value;
	
	protected Integer order;
	
	protected Boolean isExportable;
	
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {		
		this.value = value;
	}
	
	public Boolean getIsExportable() {
		return isExportable;
		
	}

	public void setIsExportable(Boolean isExportable) {
		this.isExportable = isExportable;
	}

	public static List<FormTemplate> getWithFilters(List<QueryFilter> filters){				
		List<FormTemplate> formTemplates = new ArrayList<FormTemplate>();
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		formTemplates = FormTemplate.find(wv.where + " order by order", wv.values).fetch();
		return formTemplates;		
	}
}