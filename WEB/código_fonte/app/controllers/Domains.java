package controllers;

import java.util.List;

import models.domain.Domain;
import play.mvc.Controller;
import play.mvc.With;

/**
 * The domain data needed by the app.
 * @author jairocalmon
 */
@With({AccessControlAllowOriginFilter.class})
public class Domains extends Controller{
	/**
	 * Returns the domain (external data) for the application
	 * @param page
	 */
	public static void list() {
		List<Domain> domain = Domain.all().fetch();
		renderJSON(domain);
	}	
}

