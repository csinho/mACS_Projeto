package models.general;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.Model;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="appdata")
public class AppData extends Model{
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date versionDate;
	
	protected String version;
	
	protected String versionDesc;
	
	protected Boolean isValid;

	public Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersionNumber(String version) {
		this.version = version;
	}

	public String getVersionDesc() {
		return versionDesc;
	}

	public void setVersionDesc(String versionDesc) {
		this.versionDesc = versionDesc;
	}

	public Boolean getIsValid() {
		return isValid != null? isValid : false;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	
	/**
	 * Listagem dos dispositivos podendo aplicar filtros
	 * @param filters
	 * @param pagina
	 * @param quantidade
	 * @return
	 */
    public static List<AppData> list(List<QueryFilter> filters, Integer page, Integer amount){
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<AppData> apps = AppData.find(wv.where + " order by version desc", wv.values).fetch(page, amount);		
		return apps;		
    }
    
    public static Long total(List<QueryFilter> filters) {
    	WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		return AppData.count(wv.where, wv.values);
    }
    
    public AppData saveApp() {
    	if(this.getId() != null){
    		 return ((AppData) this.merge()).save();
    	}
    	else{
    		return this.save();
    	}       
    }
    
    /**
     * Validate a device
     * @param device
     * @return
     */
    public ValidationResult validate(AppData appData){
    	ValidationResult result = new ValidationResult();
    	result.setValid(true);
        
        // Validações
        if (appData.getVersion() == null) {
        	result.setMessage("Campo Versão é obrigatório!");
        	result.setValid(false);
        }

        if (appData.getId() == null) {
            // Testando se já tem dispositivo cadastrado com o mesma versão
        	AppData app = (AppData) AppData.find("version = ?", appData.getVersion()).first();
            if (app != null) {
                result.setMessage("Já existe uma App cadastrada com este número de versão!");
                result.setValid(false);
            }
        }        
        return result;       
    }
        
    
    public static AppData byAppVersion(String version){
    	AppData appData = (AppData) Device.find("version = ?", version.trim()).first();
        if(appData ==  null) appData = new AppData();        
        return appData;
    }
   
}
