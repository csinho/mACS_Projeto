package models.general;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.domain.Equipe;
import models.domain.Municipio;
import models.domain.UnidadeSaude;
import play.db.jpa.Model;

@Entity(name="userdata")
public class UserData extends Model {

	@OneToOne
	@JoinColumn(name = "id_user")
	private Usuario user;
	
	private String cns;
	
	@ManyToOne
	@JoinColumn(name = "tipo")
	private UserType tipo;
	
	@ManyToOne
	@JoinColumn(name = "id_municipio")
	private Municipio municipio;
	
	@ManyToOne
	@JoinColumn(name = "id_unidadesaude")
	private UnidadeSaude unidadeSaude;
	
	@ManyToOne
	@JoinColumn(name = "id_equipe")
	private Equipe equipe;
	
	private String microarea;

	public Usuario getUser() {
		return user;
	}

	public void setUser(Usuario user) {
		this.user = user;
	}
	
	public String getCns() {
		return cns;
	}

	public void setCns(String cns) {
		this.cns = cns;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public UnidadeSaude getUnidadeSaude() {
		return unidadeSaude;
	}

	public void setUnidadeSaude(UnidadeSaude unidadeSaude) {
		this.unidadeSaude = unidadeSaude;
	}

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}

	public UserType getTipo() {
		return tipo;
	}

	public void setTipo(UserType tipo) {
		this.tipo = tipo;
	}

	public String getMicroarea() {
		return microarea;
	}

	public void setMicroarea(String microarea) {
		this.microarea = microarea;
	}

}
