package controllers;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.results.Ok;

public class AccessControlAllowOriginFilter extends Controller {
    @After
    public static void headers() {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
    }
    
    public static void options() {
        ok();
    }
}
