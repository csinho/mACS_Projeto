package models.general;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.joda.time.DateTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import types.JSONObjectUserType;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

//@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public class Item extends Model {	
	
	private String slug;
	
	@Column(nullable = true)
	protected String type;
	
	@Column(nullable = true, name="description")
	protected String desc;
	
	@Column(nullable = true)
	protected String parentSlug;
	
	@Column(nullable = true)
	protected String parentEntity;
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date createdAt;
	
	
	
	protected String status;
	
	protected String dataType;
	
	protected Float formVersion; 
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getParentSlug() {
		return parentSlug;
	}

	public void setParentSlug(String parentSlug) {
		this.parentSlug = parentSlug;
	}

	public String getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(String parentEntity) {
		this.parentEntity = parentEntity;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Float getFormVersion() {
		return formVersion;
	}

	public void setFormVersion(Float formVersion) {
		this.formVersion = formVersion;
	}

}
