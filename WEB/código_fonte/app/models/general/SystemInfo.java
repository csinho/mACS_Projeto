package models.general;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity(name = "SystemInfo")
public class SystemInfo extends Model {
	@Column(name = "idsystem", nullable = false)
	private String idSystem;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "version", nullable = false)
	private String version;
	
	public String getIdSystem() {
		return idSystem;
	}
	
	public void setIdSystem(String idSystem) {
		this.idSystem = idSystem;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
