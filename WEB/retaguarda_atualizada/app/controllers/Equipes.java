package controllers;

import java.util.ArrayList;
import java.util.List;

import models.domain.Equipe;
import play.mvc.Controller;
import play.mvc.With;
import util.Query.OperatorType;
import util.Query.QueryFilter;

@With({CapturaException.class, AccessControlAllowOriginFilter.class, Secure.class})
public class Equipes extends Controller{
	
	public static void get(Long id){
		renderJSON(Equipe.findById(id));
	}
	
	public static void list(Long unidade) {
		renderJSON(Equipe.find("unidade_saude_id = ?", unidade).fetch());
	}

	public static void search(){
		String term = params.get("term");
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		filters.add(new QueryFilter("description", term, OperatorType.CONTAIN));
		List<Equipe> equipes = Equipe.list(filters, 0, 10); 
		renderJSON(equipes);
	}
	
	
	public static void searchByUnidade(Long unidade){
		String term = params.get("term");
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		filters.add(new QueryFilter("description", term, OperatorType.CONTAIN));
		filters.add(new QueryFilter("unidade_saude_id", unidade, OperatorType.EQUAL));
		List<Equipe> equipes = Equipe.list(filters, 0, 10); 
		renderJSON(equipes);
	}
}
