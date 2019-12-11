package models.general;

import java.util.HashMap;
import java.util.Map;

public class DeviceStatus {

	Map<String, String> deviceAuthenticationStatuses = new HashMap<String, String>();
	
	public DeviceStatus(){
		deviceAuthenticationStatuses.put("active", "Dispositivo Autenticado");
		deviceAuthenticationStatuses.put("missing_user_association", "Dispositivo sem usuário associado");
		deviceAuthenticationStatuses.put("registered", "O dispositivo foi registrado. Verifique mais tarde se ele foi autorizado");
		deviceAuthenticationStatuses.put("inactive", "Dispositivo inativo");
		deviceAuthenticationStatuses.put("not_registered", "Dispositivo não autorizado a acessar o sistema");
		deviceAuthenticationStatuses.put("not_allowed_to_upload", "Dispositivo não autorizado a enviar dados para o sistema");
		deviceAuthenticationStatuses.put("not_allowed_to_download", "Dispositivo não autorizado a receber dados do sistema");
	}

	public Map<String, String> authenticationStatuses() {
		return deviceAuthenticationStatuses;
	}
	
	public static String authenticationStatusDesc(String status) {
		DeviceStatus deviceStatus = new DeviceStatus();
		return deviceStatus.authenticationStatuses().get(status);
	}	
	
}

