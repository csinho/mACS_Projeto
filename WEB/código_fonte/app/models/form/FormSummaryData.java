package models.form;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import models.general.Item;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="FormSummaryData")
public class FormSummaryData extends Item {
	
	@JoinColumn(name = "id_form", nullable = false)
	@ManyToOne
	private Form form;
	
	@Type(type = "org.hibernate.type.TextType")
	private String value;
	
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}	
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {	
		this.value = value;
	}
	
	public static List<FormSummaryData> geFormSummaryData(Long formId){	
		List<QueryFilter> filters = new ArrayList<QueryFilter>();		
		filters.add(new QueryFilter("id_form", formId, OperatorType.EQUAL));
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);	
		return FormSummaryData.find(wv.where, wv.values).fetch();
	}	
}
