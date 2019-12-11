package controllers;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.dialect.function.VarArgsSQLFunction;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import models.form.Form;
import models.form.FormQuery;
import models.form.FormSet;
import models.general.PaginacaoRetorno;
import models.general.Usuario;
import play.mvc.Controller;
import util.SerializeUtil;
import util.api.token.ApiToken;

public class APIService extends Controller{
	
	public static void search() {
		String token = params.get("token");
		
		if (Strings.isNullOrEmpty(token)){
			response.status = 422;
			renderText("E necessário informar um token");
		}
		
		Usuario usuario = Usuario.verificarApiToken(token);
		if (usuario == null){
			response.status = 403;
			renderText("Token inválido.");
		}
				
		try {
			ApiToken.validateToken(token);
		}catch(ExpiredJwtException e){
			response.status = 401;
			renderText("Tempo do token expirado.");
		}catch (SignatureException e){
			response.status = 403;
			renderText("Assinatura do token inválida");
		}catch (Exception e){
			response.status = 403;
			renderText("Token inválido.");
		}
		
		String mensagemErro = "Erro ao tentar efetuar a verificação, favor tentar novamente daqui a alguns minutos. Caso o erro persista favor entrar em contato com o suporte.";
				
		if (usuario != null){
			
			String newToken = null;
			if (usuario.getRenovarApiTokenAutomatico()){
				if (usuario.getDuracaoApiTokenDia() == null){
					usuario.setDuracaoApiTokenDia(0);
				}

				try {
					newToken = ApiToken.updateToken(usuario.getApiToken(), usuario.getDuracaoApiTokenDia());
					usuario.setApiToken(newToken);
					usuario.salvar();
				} catch (Exception e) {
					response.status = 401;
					renderText(mensagemErro);
				}
			}
			response.setHeader("newToken", usuario.getApiToken());
			
			try{	
				FormQuery formQuery = null;
				try {
					formQuery = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(params.get("formQuery"), FormQuery.class);
				} catch (Exception e) {
					// silence is gold
				}
				
				PaginacaoRetorno pg = new PaginacaoRetorno();
				
				if (formQuery != null && !Strings.isNullOrEmpty(formQuery.getFormType()) && formQuery.getFormVersion() != null && formQuery.getPage() != null){
					List<Form> forms = Form.findForms(formQuery,10, usuario);
					
					//if the param tell us to include form data, we return a collection of formSet, the include formData, formMetas ...
					if(formQuery.getIncludeFormData()){
						List<FormSet> formsSet = new ArrayList<FormSet>();
						for (Form form : forms) {
							FormSet formSet = form.populateFormSet(form);
							formsSet.add(formSet);
						}
						pg.setLista(formsSet);
					}
					else{//if no, only a collection of forms
						pg.setLista(forms);
					}					
					pg.setTotal(Form.count(formQuery, usuario));
					renderText(SerializeUtil.serializerExcludeGeral().exclude("lista.deviceUser", "lista.formData.form", "lista.formMetas.form").include("*").serialize(pg));					
				}else{
					response.status = 401;
					String json = "\r\n \r\n {\r\n" + 
									"	\"filters\": <array of objects> - List of filters to be applied.\r\n "+
									"	\"formType\": <required, string> - * Form type. For example: \"cadastroDomiciliar\" \r\n" + 
									"	\"formCreatedAtFrom\": <timestamp> - Initial timestamp limit of the form creation. For example: 1475535940000,\r\n" + 
									"	\"formCreatedAtTo\": <timestamp> - End timestamp limit of the form creation. For example: 1475535940000,\r\n" + 
									"	\"formSyncedAtFrom\": <timestamp> - Initial timestamp limit of the form sync. For example: 1475535940000,\r\n" + 
									"	\"formSyncedAtTo\": <timestamp> - End timestamp limit of the form sync. For example: 1475535940000,\r\n" + 
									"	\"listOldRevisions\": <true|false>  - If the old revisions of each form must be returned.\r\n" +
									"	\"cityId\": <integer> - City Id. For example: 938\r\n" +
									"	\"deviceUserId\": <integer> - Device user Id. For example: 2535\r\n" +
									"	\"unityId\": <integer>  - HealthUnit Id. For example: 11002\r\n" +
									"	\"formVersion\": <required, float>  - * Form template version. For example: 1.0 \r\n" +
									"	\"page\": <required, integer>  - * Page to be retrived. For example: 2  \r\n" +
									"	\"includeFormData\": <true|false> (default=false)  - If the service must return the formData " +
									
									"\r\n}";
					
					String dataTypes = "";
					for (util.Query.DataType dt : util.Query.DataType.values()) {
						dataTypes += " " + dt;
					}
					renderText("ERROR: the service expect an object as parameter as follows: " 
					+json + 
					" \r\n \r\n \r\n Important:" +
					" \r\n \r\n 1 - The attributes with star(*) are required\r\n" +
					" \r\n 2 - The service returns 10 items per page\r\n" +
					" \r\n 3 - The possible datatypes are" + dataTypes + "\r\n" +
					" \r\n 4 - Each filter is composed of a pair of objects, The first determines the field and the second the field data type, the operator and the value to be searched. Example: \r\n "+
					"filters: [ { \"column\":\"slug\", \"operator\":\"EQUAL\", \"value\": \"bairro\" }, { \"dataType\":\"text\",  \"operator\":\"CONTAIN\", \"value\": \"a\" }]"
					);
				}
			}catch (Exception e){
				error(mensagemErro);
			}
		}else{
			error(mensagemErro);
		}
	}
	
}
