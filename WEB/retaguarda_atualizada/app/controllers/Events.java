package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.Query;

import com.google.gson.GsonBuilder;

import flexjson.transformer.DateTransformer;
import models.domain.Event;
import models.general.PaginacaoRetorno;
import models.general.UserData;
import models.general.Usuario;

import play.db.jpa.Blob;
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.Controller;
import play.mvc.With;
import util.SerializeUtil;
/**
 * control events
 * @author joaoraf
 *
 */
@With({CapturaException.class, AccessControlAllowOriginFilter.class, Secure.class})
public class Events extends Controller{
	/**
	 * page index
	 */
	public static void index() {
		render();
	}
	
	/**
	 * list all events by page
	 * @param page
	 */
	public static void list(Integer page) {
		if(page == null)
			page = 0;
		PaginacaoRetorno pr = new PaginacaoRetorno();
		List<Event> events = Event.find("(deleted = false or deleted is null) order by date desc").fetch(page, 10);
		Long total = Event.count("deleted", false);
		pr.setLista(events);
		pr.setTotal(total);
		renderJSON(SerializeUtil.serializerGeral().transform(new DateTransformer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "lista.date").serialize(pr));
	}
	
	/**
	 * Get an event
	 * @param page
	 */
	public static void get(Integer page) {
		// We should use the logged user. But how this will work for mobile?
		Usuario user = Secure.usuarioLogado();
		PaginacaoRetorno pr = Event.get(user.getUserData(), page);
		renderJSON(SerializeUtil.serializerGeral().transform(new DateTransformer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "lista.date").serialize(pr));
	}
	
	/**
	 * Get a list with ids of the (logically) excluded events
	 */
	public static void getDeleted(){
		renderJSON(Event.removedList());
	}
	
	/**
	 * Remove (logically) the event
	 * @param id - The id of the event to be excluded
	 */
	public static void delete(Long id){
		renderJSON(Event.remove(id));
	}
	
	/**
	 * Save (or update) an event
	 */
	public static void save(){
		// Reconstruct the event from json
		Event event = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create().fromJson(new InputStreamReader(request.body), Event.class);
		renderText(SerializeUtil.serializerGeral().serialize(Event.salvarForm(event)));
	}
	
	/**
	 * Retrieves the image associated with the event
	 * @param img
	 * @throws FileNotFoundException
	 */
	public static void getImage(String img) throws FileNotFoundException{
		renderBinary(new FileInputStream(Blob.getStore().getAbsolutePath() + File.separator + img));
	}
}
