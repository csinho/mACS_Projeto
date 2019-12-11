package controllers;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import models.domain.UnidadeSaude;
import models.general.PaginacaoRetorno;
import models.general.Usuario;
import play.mvc.Controller;
import play.mvc.With;
import util.SerializeUtil;
import util.Query.OperatorType;
import util.Query.QueryFilter;

/**
 * control events
 * @author joaoraf
 *
 */
@With({CapturaException.class, AccessControlAllowOriginFilter.class})
public class UnidadesSaude extends Controller{
	public static void index() {
		render();
	}
	public static void list(Integer page) {
		if(page == null)
			page = 0;
		PaginacaoRetorno pr = new PaginacaoRetorno();
		List<UnidadeSaude> events = UnidadeSaude.find("order by nomeFantasia").fetch(page, 10);
		Long total = UnidadeSaude.count();
		pr.setLista(events);
		pr.setTotal(total);
		renderJSON(pr);
	}
	
	public static void get(Long id){
		renderJSON(UnidadeSaude.findById(id));
	}
	
	
	public static void search(){
		String term = params.get("term");
		
		//int porPagina = Integer.parseInt(params.get("porPagina"));		
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		filters.add(new QueryFilter("nomeFantasia", term, OperatorType.CONTAIN));
	
		List<UnidadeSaude> unidades = UnidadeSaude.list(filters, 0, 10); 
		
		renderJSON(unidades);
	}
	
	public static void searchByUnidade(){
		PaginacaoRetorno pr = new PaginacaoRetorno();
		
		
		String term = params.get("term");
		String paginaStr = params.get("pagina");
		int pagina =0;
		if(paginaStr!="" && paginaStr!=null){
			 pagina = Integer.parseInt(params.get("pagina"));	
		}
		
				
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		filters.add(new QueryFilter("nomeFantasia", term, OperatorType.CONTAIN));
		if(term=="" || term ==null ){
			list(pagina);
		}
		List<UnidadeSaude> unidades = UnidadeSaude.list(filters, pagina, 10); 
		Long total =  UnidadeSaude.total(filters);
		pr.setLista(unidades);
		pr.setTotal(total);
		renderJSON(pr);
	}
	public static void salvar() {
		
		UnidadeSaude ug = new GsonBuilder().setDateFormat("ddMMyyyy").create()
			.fromJson(new InputStreamReader(request.body), UnidadeSaude.class);
		UnidadeSaude aux = UnidadeSaude.findById(ug.getId());
		aux.setCnpj(ug.getCnpj());
		aux.save();
		ok();
	}
	
	public static void searchByMunicipio(Long municipioId){
		
		String term = params.get("term");
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		if(!Strings.isNullOrEmpty(term)){
			filters.add(new QueryFilter("nomeFantasia", term, OperatorType.CONTAIN));
		}
		
		filters.add(new QueryFilter("municipio_id", municipioId, OperatorType.EQUAL));
		List<UnidadeSaude> unidades = UnidadeSaude.list(filters, 0, 10); 
		
		renderJSON(unidades);
	}
}
