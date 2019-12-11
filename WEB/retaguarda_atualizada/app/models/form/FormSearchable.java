package models.form;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import models.general.Item;

@Entity(name="FormSearchable")
public class FormSearchable extends Item {
	
	@Type(type = "org.hibernate.type.TextType")
	private String value;
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date synchronizedAt;
	
	@Type(type = "org.hibernate.type.TextType")
	private String valueDesc;
	
	@JoinColumn(name = "id_form", nullable = false)
	@ManyToOne
	private Form form;
	
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
	
	public String getValueDesc() {
		return valueDesc;
	}

	public void setValueDesc(String valueDesc) {
		this.valueDesc = valueDesc;
	}
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}
}
