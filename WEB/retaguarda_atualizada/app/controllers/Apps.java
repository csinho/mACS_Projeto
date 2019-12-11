package controllers;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import models.form.SyncUploadData;
import models.general.AppData;
import models.general.CommunicationObject;
import models.general.Device;
import models.general.DeviceStatus;
import models.general.ValidationResult;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import util.Constants;
import util.RequiresProfile;
import util.SerializeUtil;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import java.lang.reflect.Type;

@With({CapturaException.class, AccessControlAllowOriginFilter.class})
public class Apps extends Controller{
	
	public static void index(){		
		render();
	}

	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void list() {
		int pagina = Integer.parseInt(params.get("pagina"));
		int porPagina = Integer.parseInt(params.get("porPagina"));		
		
		List<QueryFilter> filters = new ArrayList<QueryFilter>();		
		if(!Strings.isNullOrEmpty(params.get("versionDesc"))) {			
			filters.add(new QueryFilter("versionDesc", params.get("versionDesc"), OperatorType.CONTAIN));			
		}
		
		CommunicationObject commObj = CommunicationObject.sucesso(AppData.list(filters, pagina, porPagina), AppData.total(filters), "");		
		renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));	
	}	
	
	
	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void save() throws MalformedURLException {
		AppData app = new GsonBuilder().create().fromJson(new InputStreamReader(request.body), AppData.class);		
		ValidationResult validationResult = app.validate(app);
		CommunicationObject objetoRetorno = new CommunicationObject();
		
		if(validationResult.isValid()){	
			app.setVersionDate(new Date());
			app.saveApp();
			if(app.getId() != null){
				response.status = Http.StatusCode.CREATED;
				response.setHeader("Location", "/dispositivos/" + app.getId());
				renderText(SerializeUtil.serializerGeral().serialize(app));
			}
			else{				
				response.status = Http.StatusCode.NOT_FOUND;
				objetoRetorno.setMsg("O dispositivo não pode ser salvo");
				renderText(SerializeUtil.serializerGeral().serialize(objetoRetorno));
			}
		}
		else{
			response.status = Http.StatusCode.NOT_FOUND;		
			objetoRetorno.setMsg(validationResult.getMessage());
			renderText(SerializeUtil.serializerGeral().serialize(objetoRetorno));			
		}
	}

	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void delete(Long id) {
		try {
        	JPA.em().remove(AppData.findById(id));
        	ok();//return http status 200 when the action is delete is it was executed with success
        } 
        catch (Exception e) {
        	notFound();
        }
	}

	
	
	/**
	 * Return the last version of the app
	 * Useful for using the server time instead of the less reliable/secure
	 * app time.
	 */
	public static void versionIsValid(){		
		String appversion = params.get("deviceAppVersion");
		boolean valid = false;
		if(appversion != null){			
			AppData appData = AppData.find(" version = ?",appversion).first();
			if(appData != null && appData.getIsValid() != false){
				valid = true;				
			}					
		}		
		if(valid){			
			ok();
		}
		else{	
			response.status = Http.StatusCode.UNAUTHORIZED;	
			AppData latestAppData = AppData.find(" order by version desc").first();
			CommunicationObject commObj = new CommunicationObject();
			commObj.setData(latestAppData);
			commObj.setSuccess(false);
			commObj.setStatus("app_version_not_valid");				
			commObj.setMsg("A versão do aplicativo instalado ("+appversion+") não é válida. Atualize para a versão mais recente");				
			renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));
		}	
	}
	
}
