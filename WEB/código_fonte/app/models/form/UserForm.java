package models.form;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import models.general.Device;
import models.general.Usuario;
import play.db.jpa.Model;

@Entity(name="userform")
public class UserForm extends Model {
	
	@JoinColumn(name = "id_form", nullable = false)
	@ManyToOne
	private Form form;
	
	@JoinColumn(name = "id_deviceuser", nullable = false)
	@ManyToOne
	private Usuario deviceUser;
	
	private String formSlug;
	
	@JoinColumn(name = "id_device", nullable = false)
	@ManyToOne
	private Device device;
	
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Usuario getDeviceUser() {
		return deviceUser;
	}

	public void setDeviceUser(Usuario deviceUser) {
		this.deviceUser = deviceUser;
	}

	public String getFormSlug() {
		return formSlug;
	}

	public void setFormSlug(String formSlug) {
		this.formSlug = formSlug;
	}

}
