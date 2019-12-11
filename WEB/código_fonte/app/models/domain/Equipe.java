package models.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="equipe")
public class Equipe extends Model { 
	
	private String codigo;
	
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "unidade_saude_id")
	private UnidadeSaude unidadeSaude;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	
    public static List<Equipe> list(List<QueryFilter> filters, int pagina, int porPagina) {
    	WhereStringAndValues keyValue = QueryFilter.buildWhereWithValues(filters);
		return Equipe.find(keyValue.where + " order by description", keyValue.values).fetch(pagina, porPagina);		
    }
    
    public static Long total(List<QueryFilter> filters) {
    	WhereStringAndValues keyValue = QueryFilter.buildWhereWithValues(filters);
		return Equipe.count(keyValue.where, keyValue.values);
    }
}
