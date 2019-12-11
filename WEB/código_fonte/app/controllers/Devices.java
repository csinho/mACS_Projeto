package controllers;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

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

@With({CapturaException.class, AccessControlAllowOriginFilter.class})
public class Devices extends Controller{
	
	public static void index(){		
		render();
	}

	@RequiresProfile(Constants.PERFIL_ADMINISTRADOR)
	public static void list() {
		int pagina = Integer.parseInt(params.get("pagina"));
		int porPagina = Integer.parseInt(params.get("porPagina"));		
		
		List<QueryFilter> filters = new ArrayList<QueryFilter>();		
		if(!Strings.isNullOrEmpty(params.get("imei"))) {			
			filters.add(new QueryFilter("imei", params.get("imei"), OperatorType.CONTAIN));			
		}
		
		CommunicationObject objetoRetorno = CommunicationObject.sucesso(Device.list(filters, pagina, porPagina), Device.total(filters), "");		
		renderText(SerializeUtil.serializerExcludeGeral().serialize(objetoRetorno));	
	}	
	
	
	@RequiresProfile({ Constants.PERFIL_ADMINISTRADOR })
	public static void save() throws MalformedURLException {
		Device device = new GsonBuilder().create().fromJson(new InputStreamReader(request.body), Device.class);
		ValidationResult validationResult = device.validate(device);
		CommunicationObject objetoRetorno = new CommunicationObject();
		
		if(validationResult.isValid()){
			
			device.saveDevice();
			if(device.getId() != null){
				response.status = Http.StatusCode.CREATED;
				response.setHeader("Location", "/dispositivos/" + device.getId());
				renderText(SerializeUtil.serializerGeral().serialize(device));
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

	@RequiresProfile(Constants.PERFIL_ADMINISTRADOR)
	public static void delete(Long id) {
		try {
        	JPA.em().remove(Device.findById(id));
        	ok();//return http status 200 when the action is delete is it was executed with success
        } 
        catch (Exception e) {
        	notFound();
        }
	}

	public static void authorized(String uuid) {
		
		CommunicationObject commObj = isAuthorized(uuid);
		if(commObj.isSuccess()){
			response.status = Http.StatusCode.ACCEPTED;
		}	
		
		else{
			response.status = Http.StatusCode.UNAUTHORIZED;		
		}						
		renderText(SerializeUtil.serializerExcludeGeral().exclude("data.active", "success","total").serialize(commObj));		
	}
	
	
	
	public static CommunicationObject isAuthorized(String uuid) {
		
		Device device = Device.byDeviceId(uuid);		
		CommunicationObject commObj = new CommunicationObject();
		commObj.setData(device);
		commObj.setMsg(DeviceStatus.authenticationStatusDesc(device.getStatus()));
		commObj.setStatus((device.getStatus()));
		
		if(device.isAuthorized()){
			commObj.setSuccess(true);
		}
		else{
			commObj.setSuccess(false);	
		}						
		return commObj;		
	}
	
//	public static void register() {
//		String identifier = params.get("identifier");
//		String deviceId = params.get("deviceId");
//		String deviceDesc = params.get("deviceDesc");
//		renderText(SerializeUtil.serializerGeral().serialize(Device.register(deviceId, deviceDesc, identifier)));
//	}
}
