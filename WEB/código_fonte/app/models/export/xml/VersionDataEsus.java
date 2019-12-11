package models.export.xml;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity(name = "versionDataEsus")
public class VersionDataEsus extends Model {
	@Column(name = "tag")
	private String tag;
	@OneToMany(mappedBy="tagXML")
	private List<AttributeXml> listaAttribute;
	
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public List<AttributeXml> getListaAttribute() {
		return listaAttribute;
	}
	
	public void setListaAttribute(List<AttributeXml> listaAttribute) {
		this.listaAttribute = listaAttribute;
	}
}
