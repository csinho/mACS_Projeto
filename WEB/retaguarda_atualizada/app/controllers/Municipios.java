package controllers;

import java.util.ArrayList;
import java.util.List;

import models.domain.Municipio;
import models.general.PaginacaoRetorno;
import play.mvc.Controller;
import play.mvc.With;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@With({CapturaException.class, AccessControlAllowOriginFilter.class, Secure.class})
public class Municipios extends Controller {

	public static void index() {
		render();
	}

	public static void list(Integer page) {
		if(page == null) 
			page = 0;
		
		PaginacaoRetorno pr = new PaginacaoRetorno();
		List<Municipio> events = Municipio.find("order by nome").fetch(page, 10);
		Long total = Municipio.count();
		pr.setLista(events);
		pr.setTotal(total);
		renderJSON(pr);
	}
	
	public static void all(){
		renderJSON(Municipio.all().fetch());
	}
	
	public static void search(){
		String term = params.get("term");
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		filters.add(new QueryFilter("nome", term, OperatorType.CONTAIN));
    	WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		renderJSON( Municipio.find(wv.where + "order by nome", wv.values).fetch(0, 10) );
	}
}
