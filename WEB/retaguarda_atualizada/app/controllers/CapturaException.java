package controllers;


import models.general.CommunicationObject;
import play.db.jpa.JPA;
import play.mvc.Catch;
import play.mvc.Controller;
import util.BusinessRuleException;
import util.Logger;


public class CapturaException extends Controller {

    @Catch
    public static void tratar(Throwable e) {
        if (e != null) {
        	JPA.setRollbackOnly();
        	String mensagem = (e.getMessage() != null) 
        			? e.getMessage() 
        			: "Desculpe. Aconteceu um erro inesperado, favor entrar em contato com o administrador do sistema.";
        	
        	if(e instanceof BusinessRuleException) {
        		response.status = ( (BusinessRuleException) e).getStatus();
        	} else { 
    			response.status =  500;
    			Logger.error(e, "Exception: %s", e);
        	}
            renderJSON(CommunicationObject.error(mensagem));
        }
    }
}
