package models.export.xml;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity(name = "groupxml")
public class GroupXML extends Model{
	@Column(name = "name", nullable = false)
	private String name;
	@OneToMany(mappedBy = "groupXML")
	private List<FormFieldTagXML> listFormFieldTegXML;
		
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
		
	public List<FormFieldTagXML> getListFormFieldTegXML() {
		return listFormFieldTegXML;
	}
	
	public void setListFormFieldTegXML(List<FormFieldTagXML> listFormFieldTegXML) {
		this.listFormFieldTegXML = listFormFieldTegXML;
	}
}