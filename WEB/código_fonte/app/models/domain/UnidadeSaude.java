package models.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="unidadesaude")
public class UnidadeSaude extends Model {
	@Column(name = "cod_unidade")
	private Long code;
	
	@Column(name = "cod_cnes")
	private Long cnes;
	
	@Column(name = "cod_ibge")
	private Long igbe;

	@Column(name = "nom_fantasia")
	private String nomeFantasia;
	
	@Column(name = "des_endereco")
	private String endereco;
	
	@Column(name = "des_bairro")
	private String bairro;
	
	@Column(name = "nom_telefone")
	private String telefone;
	
	@Column(name = "des_email")
	private String email;
	
	@Column(name = "cnpj")
	private String cnpj;
	
	@ManyToOne
	@JoinColumn(name = "municipio_id")
	private Municipio municipio;

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public Long getCnes() {
		return cnes;
	}

	public void setCnes(Long cnes) {
		this.cnes = cnes;
	}

	public Long getIgbe() {
		return igbe;
	}

	public void setIgbe(Long igbe) {
		this.igbe = igbe;
	}

	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}
	
	public String getCnpj() {
		return cnpj;
	}
	
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	
    public static List<UnidadeSaude> list(List<QueryFilter> filters, int pagina, int porPagina) {
    	WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		return UnidadeSaude.find(wv.where + " order by nomeFantasia", wv.values).fetch(pagina, porPagina);		
    }
    
    public static Long total(List<QueryFilter> filters) {
    	WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		return UnidadeSaude.count(wv.where, wv.values);
    }
    
}
