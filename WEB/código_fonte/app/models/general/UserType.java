package models.general;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * The type of user of the system: agente, gestor, médico, enfermeiro
 */
@Entity(name="usertype")
public class UserType extends Model {
	private String description;

	private Boolean canUseDevice = false;
	
	private String typeSlug;
	
	public String getTypeslug() {
		return typeSlug;
	}

	public void setTypeslug(String typeslug) {
		this.typeSlug = typeslug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Boolean getCanUseDevice() {
		return canUseDevice;
	}

	public void setCanUseDevice(Boolean canUseDevice) {
		this.canUseDevice = canUseDevice;
	}
	
	public static final String AGENTE = "agente";
	public static final String MEDICO = "medico";
	public static final String ENFERMEIRO = "enfermeiro";
	
	

}
