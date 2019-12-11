package controllers;

import java.util.List;

import models.general.Usuario;
import play.mvc.Controller;
import play.mvc.With;
import util.SerializeUtil;

@With({CapturaException.class, AccessControlAllowOriginFilter.class, Secure.class})
public class DeviceUsers extends Controller{	
	/**
	 * Searches for the device users
	 * @param term Part of the name to be searched
	 */
	public static void search(){
		String term = params.get("term");
		List<Usuario> users = Usuario.searchDeviceUsers(term);
		renderJSON(SerializeUtil.serializerExcludeGeral().include("id", "nome").exclude("*").serialize(users));
	}
	
}
