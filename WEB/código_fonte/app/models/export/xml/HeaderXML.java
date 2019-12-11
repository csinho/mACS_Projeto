package models.export.xml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity(name="headerxml")
public class HeaderXML extends Model {
	
	
	@ManyToOne
	@JoinColumn(name = "idnamespace")
	private NamespaceXML namespaceXml;
	
	@Column(name = "tagdefault", nullable = false)
	private boolean tagDefault;
	
	public NamespaceXML getNamespaceXml() {
		return namespaceXml;
	}
	
	public void setNamespaceXml(NamespaceXML namespaceXml) {
		this.namespaceXml = namespaceXml;
	}
	
	public boolean getTagDefault(){
		return tagDefault;
	}
	
	public void setTagDefault(boolean tagDefault) {
		this.tagDefault = tagDefault;
	}
}
