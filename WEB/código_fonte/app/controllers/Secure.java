package controllers;

import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Arrays;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.jamonapi.jmx.Log4jDeltaMXBeanImp;

import io.jsonwebtoken.SignatureException;
import models.general.CommunicationObject;
import models.general.Usuario;
import play.Play;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import util.RequiresProfile;
import util.SerializeUtil;
import util.api.token.ApiToken;;

@With({AccessControlAllowOriginFilter.class})
public class Secure extends Controller {

    // interceptors
    @Before(unless = { "form", "logar", "deslogar" })
    static void verificaAcesso() throws Throwable {
        if (!autenticado()) {
            flash.put("url", "GET".equals(request.method) ? request.url : Play.ctxPath + "/"); 
                                                                                                                                                                                                                                                                                         
            Secure.form();
        }

        RequiresProfile profile = getActionAnnotation(RequiresProfile.class);
        if (profile != null) {
            verificaPerfils(profile);
        }
        profile = getControllerInheritedAnnotation(RequiresProfile.class);
        if (profile != null) {
            verificaPerfils(profile);
        }
    }

    // actions
    public static void usuarioLogadoJSON() {
    	renderText(usuarioLogado());
    }

    public static void form() throws ParseException {
        if (autenticado()) {
            redirecionarParaUrlOriginal();
        } else {
            render();
        }
    }

    public static void logar() {
        Usuario login = new GsonBuilder().create().fromJson(new InputStreamReader(request.body), Usuario.class);

        Usuario loginBanco = Usuario.find("login = ? ", login.getLogin()).first();

        CommunicationObject commObj = new CommunicationObject();

        if (loginBanco != null && Arrays.equals(loginBanco.getSenha(), Usuario.getSha512(login.getPass()))) {
            if (loginBanco.isPrimeiroAcesso())
                renderText(SerializeUtil.serializerExcludeGeral().include("token", "primeiroAcesso").exclude("*")
                        .serialize(loginBanco));
  			
            
            if (Strings.isNullOrEmpty(loginBanco.getApiToken())){
            	loginBanco.setApiToken(ApiToken.generate(loginBanco));
            	loginBanco.setRenovarApiTokenAutomatico(false);
            	loginBanco.setDuracaoApiTokenDia(0);
            	loginBanco.salvar();
            }else{
            	try {
            		ApiToken.validateToken(loginBanco.getApiToken());
            	}catch (Exception e){
            		loginBanco.setApiToken(ApiToken.generate(loginBanco));
            		loginBanco.setRenovarApiTokenAutomatico(false);
            		loginBanco.setDuracaoApiTokenDia(0);
            		loginBanco.salvar();
            	}
            }
            
            commObj.setToken(loginBanco.getApiToken());
            
            guardarUsuario(loginBanco);
            commObj.setMsg("Usuário Logado!");
        } else {
        	commObj.setSuccess(false);
        	commObj.setMsg("Dados de acesso inválidos");
        }
        
        renderJSON(commObj);
    }

    public static void deslogar() throws ParseException {
        limparSessao();
        form();
    }

    // no actions

    static void verificaPerfils(RequiresProfile perfils) throws Throwable {
        boolean temPerfil = false;

        for (String perfil : perfils.value()) {
            temPerfil = temPerfil(perfil);
            if (temPerfil)
                break;
        }

        if (!temPerfil)
            forbidden("Para ter acesso a página solicitada é necessário ter um dos perfils a seguir: "
                    + Arrays.toString(perfils.value()));
    }

    static boolean temPerfil(String perfil) {
        boolean retorno = false;
        Usuario login = usuarioLogado();
        
        if (login != null) {
            retorno = login.getPerfis() != null && login.getPerfis().contains(perfil);
        }
        
        return retorno;
    }

    public static Usuario usuarioLogado() {
        return (Usuario) Cache.get(session.getId() + "-login");
    }

    public static String getPerfil() {
        Usuario login = (Usuario) Cache.get(session.getId() + "-login");
        return login.getPerfis();
    }

    protected static void limparSessao() {
        session.remove("user");
        Cache.delete(session.getId() + "-login");
    }

    private static void guardarUsuario(Usuario login) {
        login.setSenha(null);
        session.put("user", login.getNome());
        Cache.set(session.getId() + "-login", login);
    }

    static boolean autenticado() {
        Usuario login = usuarioLogado();
        return (session.get("user") != null && login != null);
    }

    static void redirecionarParaUrlOriginal() throws ParseException {
        String url = flash.get("url");
        
        if (url != null) {
            redirect(url);
        } else {
            Home.index();
        }
    }

}
