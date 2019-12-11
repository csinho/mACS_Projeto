package models.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity(name="estado")
public class Estado extends Model {
	@Column(name = "cod_estado")
	private Long codigo;
	
	@Column(name = "nom_estado")
	private String nome;
	
	@Column(name = "sigla_estado")
	private String sigla;

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

}
