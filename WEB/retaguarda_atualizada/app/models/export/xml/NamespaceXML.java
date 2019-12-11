package models.export.xml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity(name = "namespacexml")
public class NamespaceXML extends Model{
	
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "nametagxml", nullable = false)
	private String nameTagXML;
	@Column(name = "prefix", nullable =  false)
	private String prefix;
	@Column(name="url", nullable = false)
	private String url;
		
	public String getName(){
		return name;
	}
	
	public void setName(String description) {
		this.name = description;
	}
	
	public String getNameTagXML() {
		return nameTagXML;
	}
	
	public void setNameTagXML(String nameTagXML) {
		this.nameTagXML = nameTagXML;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefixNamespace) {
		this.prefix = prefixNamespace;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String urlNamesapce) {
		this.url = urlNamesapce;
	}
}
