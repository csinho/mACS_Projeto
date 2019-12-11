package models.general;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.db.jpa.Model;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity
public class Device extends Model {
	private String imei;
	private String status = "not_registered";
	
	@Column(nullable = false, name="description")
	private String desc = "";
	
	
	@JoinColumn(name = "id_deviceuser", nullable = true)
	@OneToOne
	private Usuario deviceUser;	

	public Usuario getDeviceUser() {
		return deviceUser;
	}

	public void setDeviceUser(Usuario deviceUser) {
		this.deviceUser = deviceUser;
	}

	public boolean isActive() {
		return this.status == "active";
	}
	
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

    public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
    
	/**
	 * Listagem dos dispositivos podendo aplicar filtros
	 * @param filters
	 * @param pagina
	 * @param quantidade
	 * @return
	 */
    public static List<Device> list(List<QueryFilter> filters, Integer page, Integer amount){
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<Device> dispositivos = Device.find(wv.where + " order by imei", wv.values).fetch(page, amount);		
		return dispositivos;		
    }
    
    public static Long total(List<QueryFilter> filters) {
    	WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		return Device.count(wv.where, wv.values);
    }
    
    public Device saveDevice() {
    	if(this.getId() != null){
    		 return ((Device) this.merge()).save();
    	}
    	else{    
    		if(this.getDeviceUser() == null){
    			this.setStatus("missing_agent_association");
    		}    		 		
    		return this.save();
    	}       
    }
    
    /**
     * Validate a device
     * @param device
     * @return
     */
    public ValidationResult validate(Device device){
    	ValidationResult result = new ValidationResult();
    	result.setValid(true);
        
        // Validações
        if (device.getImei() == null) {
        	result.setMessage("Campo IMEI é obrigatório!");
        	result.setValid(false);
        }

        if (device.getId() == null) {
            // Testando se já tem dispositivo cadastrado com o mesmo IMEI
        	Device dispCadastrado = (Device) Device.find("imei = ?", device.getImei()).first();
            if (dispCadastrado != null) {
                result.setMessage("Já existe dispositivo cadastrado com este IMEI!");
                result.setValid(false);
            }
        }        
        return result;       
    }
        
    
    public static Device byDeviceId(String deviceId){
    	Device device = (Device) Device.find("imei = ?", deviceId.trim()).first();
        if(device ==  null) device = new Device();
        if(device.getStatus() == "active" && device.getDeviceUser() == null){
        	device.setStatus("missing_user_association");        	
        }
        return device;
    }
    
        
//    public static CommunicationObject register(String deviceId, String deviceDesc, String identifier){
//    	CommunicationObject obj = new CommunicationObject();
//        Device device = (Device) Device.find("imei = ?", deviceId.trim()).first();
//        
//        DeviceUser deviceUser = (DeviceUser) DeviceUser.find("cartaoSus = ?", identifier).first();
//        if(deviceUser == null){
//        	//set objeRetorno data
//            obj.setMsg("O dispositivo não foi registrado. Verifique se o número do cartão SUS está correto e cadastrado no sistema");
//            obj.setStatus("inactive"); 
//            return obj;
//        }
//        else if(device == null){
//        	//save device
//        	device = new Device();
//        	device.setImei(deviceId);
//        	device.setDesc(deviceDesc);
//        	device.setStatus("inactive");
//        	device.save();
//            
//            //set objeRetorno data
//            obj.setMsg("Dispositivo registrado. Solicite a ativação ao gestor e verifique se está autorizado em seguida");
//            obj.setStatus("inactive");
//        }
//        else if(device.getStatus() == "active"){
//        	//set objeRetorno data
//            obj.setMsg("Dispositivo já registrado e ativo");
//            obj.setStatus("active");
//        }
//        else{
//        	//set objeRetorno data
//            obj.setMsg("Dispositivo já registrado. Solicite a ativação ao gestor e verifique se está autorizado em seguida");
//            obj.setStatus("inactive");
//        }
//        
//        return obj;
//    }
    
	public static boolean isAuthorized(String uuid){
		Device device = Device.byDeviceId(uuid);		
		return device.getStatus().equalsIgnoreCase("active");				
	}
	
	public boolean isAuthorized(){				
		return this.getStatus().equalsIgnoreCase("active");				
	}
}
