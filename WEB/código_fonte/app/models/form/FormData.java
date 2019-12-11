package models.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import models.general.Item;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="FormData")
@DiscriminatorValue(value="FormData")
public class FormData extends Item {
	
	@Type(type = "org.hibernate.type.TextType")
	private String value;
	
	@Type(type = "org.hibernate.type.TextType")
	private String valueDesc;
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date synchronizedAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date targetReferenceCreatedAt;
	
	@JoinColumn(name = "id_form", nullable = false)
	@ManyToOne
	private Form form;
	
	public FormData() {
		this.childForms = new ArrayList<Form>();
	}
	
	@Transient	
	private List<Form> childForms;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {	
		this.value = value;
	}

	public Date getSynchronizedAt() {
		return synchronizedAt;
	}

	public void setSynchronizedAt(Date synchronizedAt) {
		this.synchronizedAt = synchronizedAt;
	}

	public Date getTargetReferenceCreatedAt() {
		return targetReferenceCreatedAt;
	}

	public void setTargetReferenceCreatedAt(Date targetReferenceCreatedAt) {
		this.targetReferenceCreatedAt = targetReferenceCreatedAt;
	}

	public String getValueDesc() {
		return valueDesc;
	}

	public void setValueDesc(String valueDesc) {
		this.valueDesc = valueDesc;
	}
	
	public List<Form> getChildForms() {
		return childForms;
	}

	public void setChildForms(List<Form> childForms) {
		this.childForms = Form.setSummaryFields(childForms);		
	}

	public Form getForm() {
		return form;
	}
	
	public void setForm(Form form) {
		this.form = form;
	}	
	
	public static List<FormData> search(List<QueryFilter> filters){
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<FormData> formsData = FormData.find(wv.where, wv.values).fetch();
		return formsData;
	}
	
	
}
