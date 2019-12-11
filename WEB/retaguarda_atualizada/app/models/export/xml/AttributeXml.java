package models.export.xml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity(name = "attributexml")
public class AttributeXml extends Model{
	@Column(name = "attribute", nullable = false)
	private String attribute;
	@Column(name = "value", nullable = false)
	private String value;
	@ManyToOne
	@JoinColumn(name="idtagxml", nullable = false)
	private VersionDataEsus tagXML;

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public VersionDataEsus getTagXML() {
		return tagXML;
	}

	public void setTagXML(VersionDataEsus tagXML) {
		this.tagXML = tagXML;
	}
}
