package controllers;

import play.mvc.Controller;
import play.mvc.With;
import util.RequiresProfile;


@With({AccessControlAllowOriginFilter.class, Secure.class, CapturaException.class})
public class QRCode extends Controller{
	public static void index(){		
		render();
	}
}
