package controllers;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.mail.EmailException;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import flexjson.transformer.DateTransformer;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import models.general.UserType;
import models.general.Usuario;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import util.Constants;
import util.Perfil;
import util.RequiresProfile;
import util.SerializeUtil;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.SubQuery;
import util.Query.WhereStringAndValues;
import util.api.token.ApiToken;

@With({ Secure.class, CapturaException.class })
public class Usuarios extends Controller {

	@RequiresProfile({ Constants.PERFIL_ADMINISTRADOR, Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_GESTOR_ESTADO })
	public static void index() {
		render();
	}

	
	public static void exibir(Long id) {
		render();
	}

	@RequiresProfile({ Constants.PERFIL_ADMINISTRADOR, Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_GESTOR_ESTADO })
	public static void salvar() throws MalformedURLException, EmailException {
		Usuario usu = new GsonBuilder().create().fromJson(new InputStreamReader(request.body), Usuario.class);

		renderText(SerializeUtil.serializerGeral().serialize(Usuario.salvarForm(usu)));
	}

	@RequiresProfile({ Constants.PERFIL_ADMINISTRADOR })
	public static void buscarCombo() {
		renderText(SerializeUtil.serializerGeral().serialize(Usuario.find("order by nome").fetch()));
	}

	public static void listar() {
		int pagina = Integer.parseInt(params.get("pagina"));
		int porPagina = Integer.parseInt(params.get("porPagina"));
		String name = (params.get("nome").isEmpty()) ? "" : params.get("nome") + "%";
		renderText(SerializeUtil.serializerExcludeGeral().exclude("lista.senha", "lista.token", "lista.pass")
				.include("*").serialize(Usuario.listar(pagina, porPagina, name, Secure.usuarioLogado())));
	}
	
	@RequiresProfile({ Constants.PERFIL_ADMINISTRADOR, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_USUARIO })
	public static void search() {			
		
		List<QueryFilter> filters = new ArrayList<QueryFilter>();		
		String term = params.get("term");	
		if(!Strings.isNullOrEmpty(term)){
			filters.add(new QueryFilter("nome", term, OperatorType.CONTAIN));
		}
		
		String unityId = params.get("unityId");	
		if(!Strings.isNullOrEmpty(unityId)){
			List<QueryFilter> subfilters = new ArrayList<QueryFilter>();
			subfilters.add(new QueryFilter("id_unidadesaude",Long.parseLong(unityId), OperatorType.EQUAL));
			filters.add(new QueryFilter("id", new SubQuery("userdata", "user.id",subfilters), OperatorType.IN));
		}		
		
		WhereStringAndValues wValues = QueryFilter.buildWhereWithValues(filters);		
		List<Usuario> usuarios = Usuario.find(wValues.where, wValues.values).fetch();		
		renderText(SerializeUtil.serializerExcludeGeral().exclude("lista.senha", "lista.token", "lista.pass")
				.include("*").serialize(usuarios));
	}

	@RequiresProfile(Constants.PERFIL_ADMINISTRADOR)
	public static void deletar(Long id) {
		renderText(SerializeUtil.serializerGeral().serialize(Usuario.deletarForm(id)));
	}
	
	public static void buscarComboPerfil() {
		renderText(SerializeUtil.serializerGeral().serialize(Perfil.listarPerfis(Secure.usuarioLogado().getPerfis())));
	}

	public static void listUserTypes() {
		renderJSON(UserType.all().fetch());
	}
	
	public static void currentUser(){		
		renderText(SerializeUtil.serializerExcludeGeral().exclude("senha", "pass","token")
				.include("*").serialize(Secure.usuarioLogado()));
	}
	
	public static void usuarioLogadoAtualizado(){
		Usuario usuario = Usuario.find(" id = ? ", Secure.usuarioLogado().id).first();
		
		usuario.setListaDispositivos(Usuario.dispositivosUsuario(usuario));
		
		if (usuario.getApiToken() != null){
			try {
				ApiToken.validateToken(usuario.getApiToken());
				usuario.setDataExpiracaoToken(Jwts.parser().setSigningKey(Play.configuration.getProperty("application.apisecret")).parseClaimsJws(usuario.getApiToken()).getBody().getExpiration());
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		renderText(SerializeUtil.serializerExcludeGeral().exclude("senha", "pass","token")
				.include("*").transform(new DateTransformer("dd/MM/yyyy HH:mm:ss"), "dataExpiracaoToken").serialize(usuario));
	}
	
	public static void atualizarToken(){
		Usuario usuarioTela = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(new InputStreamReader(request.body), Usuario.class);

		Usuario usuarioBanco = Usuario.verificarApiToken(usuarioTela.getApiToken());
		if (usuarioBanco == null){
			response.status = 403;
			renderText("Token inválido.");
		}
		
		try {
			ApiToken.validateToken(usuarioTela.getApiToken());
		}catch(ExpiredJwtException e){
			response.status = 401;
			renderText("Tempo do token expirado.");
		}catch (Exception e){
			response.status = 403;
			renderText("Token inválido.");
		}
		
		try {
			String novoToken = 	ApiToken.updateToken(usuarioTela.getApiToken(), usuarioTela.getDuracaoApiTokenDia());
			usuarioBanco.setApiToken(novoToken);
			usuarioBanco.setRenovarApiTokenAutomatico(usuarioTela.getRenovarApiTokenAutomatico());
			usuarioBanco.setDuracaoApiTokenDia(usuarioTela.getDuracaoApiTokenDia());
			usuarioBanco.salvar();
			usuarioBanco.setDataExpiracaoToken(Jwts.parser().setSigningKey(Play.configuration.getProperty("application.apisecret")).parseClaimsJws(usuarioBanco.getApiToken()).getBody().getExpiration());
			renderText(SerializeUtil.serializerExcludeGeral().exclude("senha", "pass","token")
					.include("*").transform(new DateTransformer("dd/MM/yyyy HH:mm:ss"), "dataExpiracaoToken").serialize(usuarioBanco));
		} catch (Exception e) {
			error("Erro ao atualizar o token.");
		}
	}
	
	public static void removerApiToken(){
		Usuario usuarioTela = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(new InputStreamReader(request.body), Usuario.class);
		
		Usuario usuarioBanco = Usuario.find(" id = ? ", usuarioTela.getId()).first();
		
		if (usuarioBanco != null){
			usuarioBanco.setApiToken(null);
			usuarioBanco.setRenovarApiTokenAutomatico(false);
			usuarioBanco.setDuracaoApiTokenDia(null);
			usuarioBanco.salvar();
			renderText(SerializeUtil.serializerExcludeGeral().exclude("senha", "pass","token")
					.include("*").transform(new DateTransformer("dd/MM/yyyy HH:mm:ss"), "dataExpiracaoToken").serialize(usuarioBanco));
		}
	}
}
