package controllers;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Arrays;

import org.apache.commons.mail.EmailException;

import com.google.gson.GsonBuilder;

import models.general.CommunicationObject;
import models.general.Usuario;
import play.mvc.Controller;
import play.mvc.With;
import util.SerializeUtil;

@With(CapturaException.class)
public class Logins extends Controller{

  public static void recuperar(String token) throws ParseException{
    
    Usuario usuario = Usuario.find("token = ?", token).first();
    
    if(usuario == null){
      Secure.form();
    }
    
    String login = usuario.getLogin();
    Long id = usuario.getId();
    render(token, login, id);
    
  }
   
  public static void alterarSenha(){
    
    Usuario usuario = new GsonBuilder().setDateFormat("ddMMyyyy").create().fromJson(
        new InputStreamReader(request.body), Usuario.class);
    
    CommunicationObject commObj = new CommunicationObject();
    
    
    Usuario userBanco = (Usuario)Usuario.find("id = ?", usuario.getId()).first();
    
    if(userBanco != null && Arrays.equals(userBanco.getSenha(), Usuario.getSha512(usuario.getPass()))){
      
    	commObj.setSuccess(false);
    	commObj.setMsg("A senha deve ser diferente da antiga.");
      
    }else if(usuario.atualizarSenha()){
      
    	commObj.setMsg("Senha alterada com sucesso.");
      
    }else{
      
    	commObj.setSuccess(false);
    	commObj.setMsg("Token não encontrado.");
      
    }
    
    renderText(SerializeUtil.serializerGeral().serialize(commObj));
    
  }
  
  public static void enviarRecuperacaoSenha(String usuario) throws MalformedURLException, EmailException{
    
    renderText(SerializeUtil.serializerGeral().serialize(Usuario.enviarRecuperacaoSenha(usuario)));
    
  }
  

  public static void novaSenha() throws ParseException{
    render();
  }

  public static void salvarNovaSenha() throws ParseException{
	    Usuario usuario = new GsonBuilder().create().fromJson(
	        new InputStreamReader(request.body), Usuario.class);
	    
	    CommunicationObject commObj = new CommunicationObject();
	    
	    Usuario userBanco = (Usuario)Usuario.find("id = ?", usuario.getId()).first();
	    
	    if(userBanco != null && Arrays.equals(userBanco.getSenha(), Usuario.getSha512(usuario.getPass()))){
	      
	    	commObj.setSuccess(false);
	    	commObj.setMsg("A senha deve ser diferente da antiga.");
	      
	    }else if(usuario.salvarNovaSenha()){
	      
	    	commObj.setMsg("Senha alterada com sucesso.");
	      
	    }else{
	      
	    	commObj.setSuccess(false);
	    	commObj.setMsg("Token não encontrado.");
	      
	    }
	    
	    renderText(SerializeUtil.serializerGeral().serialize(commObj));
	      
  }  
}
