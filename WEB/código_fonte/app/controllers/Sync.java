package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.Query;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import flexjson.transformer.DateTransformer;
import models.domain.Domain;
import models.domain.DomainData;
import models.domain.Estado;
import models.domain.Event;
import models.domain.Municipio;
import models.domain.PackedDomainData;
import models.form.Form;
import models.form.FormData;
import models.form.FormFieldType;
import models.form.FormMeta;
import models.form.FormSearchable;
import models.form.FormSet;
import models.form.FormSummaryData;
import models.form.FormTemplate;
import models.form.SyncUploadData;
import models.form.UserForm;
import models.form.UserFormDataPacked;
import models.form.UserFormSearchablePacked;
import models.form.UserFormsPacked;
import models.general.CommunicationObject;
import models.general.Device;
import models.general.DeviceStatus;
import models.general.Usuario;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import util.GsonUTCDateAdapter;
import util.SerializeUtil;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.SubQuery;
import util.Query.WhereStringAndValues;


@With({CapturaException.class, AccessControlAllowOriginFilter.class})
public class Sync extends Controller{			
	
	/**
	 * Return the server time.
	 * Useful for using the server time instead of the less reliable/secure
	 * app time.
	 */
	public static void time(){
		renderText(new Date().getTime());
	}	
	
	
	public static void upload() throws IOException {
		SyncUploadData syncUploadData = new GsonBuilder()
				.registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.create().fromJson(new InputStreamReader(request.body), SyncUploadData.class);	
		
		CommunicationObject commObj = new CommunicationObject();
		Integer totalForms = syncUploadData.getFormsSets().size();
		if(deviceCanSync(syncUploadData.getDeviceId())){			
			Integer insertedForms = 0;
			for (FormSet formSync : syncUploadData.getFormsSets()) {				
				try {
					Form form = formSync.getForm();
					List<QueryFilter> filters = new ArrayList<QueryFilter>();	
					filters.add(new QueryFilter("slug", form.getSlug(), OperatorType.EQUAL));
					filters.add(new QueryFilter("createdAt", form.getCreatedAt(), OperatorType.GREATER_OR_EQUAL));
					
					Integer existingForms = Form.search(filters).size();
				    if(existingForms == 0) {																		
						form.setSynchronizedAt(new Date());						
						form.id = null;					
						form.save();						
						saveAdditionalFormInfo(formSync, syncUploadData.getDeviceId(), syncUploadData.getDeviceUserId());						
						insertedForms++;
				    }
				} 
				catch (Exception e) {					
					//silence is gold 
					//in the future we can save a log of the forms not saved and the reason
				}				
			}
			Integer formErrosCounter = totalForms -insertedForms;
			Hashtable statistics = new Hashtable();
			statistics.put("errorItems",formErrosCounter);
			statistics.put("successItems",insertedForms);
			commObj.setData(statistics);
			
			if( totalForms == insertedForms){
				response.status = Http.StatusCode.ACCEPTED;//all the forms were inserted successfully
			}
			else if(insertedForms == 0){
				response.status = Http.StatusCode.NOT_MODIFIED;//no form was inserted. We dont know the reason
				commObj.setMsg("Houve um erro ao enviar os formulários. Tente novamente");
			}
			else{
				response.status = Http.StatusCode.PARTIAL_INFO;//partial insertion				
				commObj.setMsg("Houve um erro ao enviar " + formErrosCounter + " dos formulários "+ totalForms+". Tente novamente para completar o envio");
			}			
		}
		else{			
			commObj.setStatus("not_authorized");
			commObj.setMsg("Dispositivo não autorizado a enviar dados");
			response.status = Http.StatusCode.UNAUTHORIZED;
		}
		renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));		
	}	
	
	/**
	 * Saves additional form info, like formData, formSearchable and user form
	 * @param formSync
	 * @param deviceId
	 * @param deviceUserId
	 */
	private static void saveAdditionalFormInfo(FormSet formSync, String deviceId, String deviceUserId){		
		UserForm userForm = new UserForm();
		userForm.setForm(formSync.getForm());
		userForm.setDeviceUser((Usuario) Usuario.findById(Long.parseLong(deviceUserId)));
		userForm.setDevice(Device.byDeviceId(deviceId));
		userForm.setFormSlug(formSync.getForm().getSlug());
		userForm.save();
		
		for (FormMeta formMeta : formSync.getFormMetas()) {	
			formMeta.setCreatedAt(formSync.getForm().getCreatedAt());
			formMeta.setForm(formSync.getForm());
			formMeta.setSynchronizedAt(new Date());
			formMeta.save();							
		}
		for (FormSearchable formSearchable : formSync.getFormSearchables()) {	
			formSearchable.setCreatedAt(formSync.getForm().getCreatedAt());
			formSearchable.setForm(formSync.getForm());
			formSearchable.setSynchronizedAt(new Date());
			formSearchable.save();							
		}
		for (FormData formData : formSync.getFormData()) {
			formData.setCreatedAt(formSync.getForm().getCreatedAt());
			formData.setForm(formSync.getForm());
			formData.setSynchronizedAt(new Date());
			formData.save();
			
			//updates the field type table, creating if does not existe
			updateFieldType(formData);
		}
		
		insertSummaryData(formSync.getFormSummaryDatas(),formSync.getForm());		
	}

	/**
	 * Gets the domains data that are used to populate options in the app
	 * the domain data includes states, cities, professions (etc) and also user specific data, like user city and state
	 */
	public static void getDomainData() throws MalformedURLException, ParseException {		
		
		if(deviceCanSync()){
			response.status = Http.StatusCode.ACCEPTED;
			List<QueryFilter> filters = new ArrayList<QueryFilter>();			
			
			// If we we passed the date as param we can convert it			
			if(!Strings.isNullOrEmpty(params.get("updatedAt"))){
				// We receive the updatedAt as milliseconds, so we parse it
				Date lastSyncDate = new Date(Long.parseLong(params.get("updatedAt")));
				filters.add(new QueryFilter("updatedAt", lastSyncDate, ">"));
			}
			
			//if the domain code is specified, we filter by this domain
			if(!Strings.isNullOrEmpty(params.get("domaincode"))) {			
				filters.add(new QueryFilter("domaincode", params.get("domaincode"), OperatorType.EQUAL));			
			}
			
			ArrayList packed = (ArrayList) Domain.packedData(filters);

			//here we get user specifique data (based in the current user/device syncing)
			packed.addAll(getUserPackedData());
			
			
			renderText(SerializeUtil.serializerExcludeGeral()				    
				    .include("emptyExistingItems", "entitySlug", "items")
				    .exclude("items.createdAt", "items.domain", "items.id"/*,"items.value"*/)
				    .serialize(packed));			
			
		}
		else{
			response.status = Http.StatusCode.UNAUTHORIZED;	
			CommunicationObject commObj = new CommunicationObject();
			Device device = getSyncingDevice();
			commObj.setData(device);
			commObj.setMsg(DeviceStatus.authenticationStatusDesc("not_allowed_to_download"));
			commObj.setStatus((device.getStatus()));
			renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));	
		}		
	}
	
	/**
	 * Get an event
	 * @param page
	 */
	public static void getEvents() {
		try {
			Long userId = Long.parseLong(params.get("userId"));
			Usuario user = (Usuario) Usuario.findById(userId);
			if(user != null){
				renderJSON(SerializeUtil.serializerGeral()
						.transform(new DateTransformer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "lista.date")
						.serialize(Event.get(user.getUserData(), null)));
			} 
			else {
				error("Usuário não cadastrado");
			}
		} catch(NumberFormatException e){
			error("Um 'userId' válido deve ser informado na query");
		} catch (Exception e) {
			error(e);
		}
	}
	
	/**
	 * Get a list with ids of the (logically) excluded events
	 */
	public static void getDeletedEvents(){
		renderJSON(Event.removedList());
	}
	
	/**
	 * Retrieves the image associated with the event
	 * @param img
	 * @throws FileNotFoundException
	 */
	public static void getEventImage(String img) throws FileNotFoundException{
		renderBinary(new FileInputStream(Blob.getStore().getAbsolutePath() + File.separator + img));
	}
	
	/**
	 * Gets the domains data	 *
	 */
	public static List<PackedDomainData> getUserPackedData(){
		
		Usuario currentDeviceUser = getSyncingDevice().getDeviceUser();
		List<PackedDomainData> userPackedDataList = new ArrayList<PackedDomainData>();
		
		/*Gets the user State*/
		PackedDomainData userStatePack = new PackedDomainData();
		Domain userStateDomain = new Domain();
		userStateDomain.setSlug("deviceUserState");
		userStateDomain.setDescription("Estado do usuário");
		userStatePack.setDomain(userStateDomain);
		userStatePack.setEmptyExistingItems(true);
		Estado userState = currentDeviceUser.getUserData().getMunicipio().getEstado();	
		
		//the domain dat aof states are in another table. It contains a value the is a code for the domain item, 
		//so we get the state from this table based in the user state data
		Domain stateDomain = Domain.find(" slug = ?", "unidadefederativa").first();		
		List<DomainData> userStates =  DomainData.find(" domaincode = ? and lower(description) = ?", stateDomain.getId(),userState.getNome().toLowerCase()).fetch();
		
		//the states in the domaindata table are child of domain 'uniddefederativa', 
		//but in this case, the states must not be child of anyone
		for (DomainData state : userStates) {
			state.setParentEntity(null);								
		}
		
		userStatePack.setItems(userStates);
		userPackedDataList.add(userStatePack);
		
		/*Gets the user city*/
		Domain userCityDomain = new Domain();
		userCityDomain.setSlug("deviceUserCity");
		userCityDomain.setDescription("Município do usuário");
		
		PackedDomainData userCityPack = new PackedDomainData();	
		userCityPack.setDomain(userCityDomain);
		userCityPack.setEmptyExistingItems(true);
		//for each user states we get the corresponding cities (foreach state)
		for (DomainData state : userStates) {
			Municipio userCity = currentDeviceUser.getUserData().getMunicipio();
			//we get the cities that are child of the current state and has the same name of the user state
			//there is no more than one city with a name in a given state
			List<DomainData> userCities =  DomainData.find(" parentSlug = ? and lower(description) = ?", state.getSlug(), userCity.getNome().toLowerCase()).fetch();
			for (DomainData city : userCities) {
				city.setParentEntity("deviceUserState");							
			}
			userCityPack.addItems(userCities);										
		}
		userPackedDataList.add(userCityPack);		
		return userPackedDataList;
	}
	
	/**
	 * Get and pack the user forms according the last sync date and the current device	 * 
	 */
	public static void getPackedUserForms() throws MalformedURLException, ParseException {
		//Device device = getSyncingDevice("327ccadb3182d1df");	
		//if(deviceCanSync("327ccadb3182d1df")){
		Device device = getSyncingDevice();	
		if(deviceCanSync()){
			response.status = Http.StatusCode.ACCEPTED;
			ArrayList packedData = new ArrayList<>();			
			List<Form> userForms = new ArrayList<>();
			Date lastSyncDate = null;
			
			//if lastSyncDate is not null so it is not day zero and we must retrive only forms sent by other devices
			//this cover the scenery when the same user is using mutiple devices and needs to retrive forms sent by other devices he/she owns
			if(!Strings.isNullOrEmpty(params.get("updatedAt"))){
				// If we we passed the date as param we can convert it	
				lastSyncDate = new Date(Long.parseLong(params.get("updatedAt")));
				userForms = getUserFormsFromOtherDevices(device.getDeviceUser(), device, lastSyncDate);
			}
			else{//if lastSyncDate is null, is day zero and we must download all. No date or device filter is required
				userForms = getUserForms(device.getDeviceUser());
			}			
			
			// we need to convert the form json object in string
			// we also get the formData belonging to the form
			for (Form form : userForms) {					
				// we dont need to send the form (scaffold + formData) 
				// because the form will be rebuilt from the template plus the form data that will be populated				
				
				// Here we get the formData of each form				
				List<FormData> formDatas =  FormData.find("form = ?", form).fetch();
				UserFormDataPacked packedFormDatas = new UserFormDataPacked(formDatas, false);
				
				for (FormData formData : formDatas) {	
					//it is not necessary to send the form scaffold
					//because the form will be rebuilt from the template plus the form data that will be populated 
					formData.setForm(null);
					//formData.id = null;
				}
				packedData.add(packedFormDatas);
				
				
				// Here we get the formSearchbles of each form				
				List<FormSearchable> formSearchables =  FormSearchable.find("form = ?", form).fetch();
				UserFormSearchablePacked packedFormSearchables = new UserFormSearchablePacked(formSearchables, false);
				
				for (FormSearchable formSearchable : formSearchables) {	
					//it is not necessary to send the form scaffold
					//because the form will be rebuilt from the template plus the form data that will be populated 
					formSearchable.setForm(null);
					//formSearchable.id = null;
				}
				packedData.add(packedFormSearchables);	
			}	
			
			//the pack will be formed by the form list and the instruction 
			//to do not empty the device form table (the boolean parameter)
			//if is day zero, there is no need to empty device form table.
			//if is not day zero, we return only forms send by other user device, 
			//so we also dont need to empty local form table
			UserFormsPacked packedForms = new UserFormsPacked(userForms, false);
			//packed = new ArrayList<>();
			packedData.add(packedForms);	
			
			renderText(SerializeUtil.serializerExcludeGeral()				    
				    .include("emptyExistingItems", "entitySlug", "items","description")
				    .exclude("items.id")
				    .serialize(packedData));
			
		}
		else{
			response.status = Http.StatusCode.UNAUTHORIZED;	
			CommunicationObject commObj = new CommunicationObject();			
			commObj.setData(device);
			commObj.setMsg(DeviceStatus.authenticationStatusDesc("not_allowed_to_download"));
			commObj.setStatus((device.getStatus()));
			renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));	
		}		
	}
	
	/**
	 * Gets all user forms
	 * @param Usuario
	 */
	public static List<Form> getUserForms(Usuario user){		
		List<Form> forms = new ArrayList<Form>();
		List<QueryFilter> formFilters = new ArrayList<QueryFilter>();
		final Long deviceUserId = user.id;
		
		formFilters.add(new QueryFilter("id", OperatorType.IN, 
				new SubQuery("userform", "id_form",
						new ArrayList<QueryFilter>(){{add(new QueryFilter("id_deviceuser",OperatorType.EQUAL,deviceUserId));}}
				)
			)
		);
			
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(formFilters);
		Query query = JPA.em().createNativeQuery("select distinct on(slug)  form.* from form where " + wv.where + " order by slug, createdAt desc", Form.class);		
		
		for (int i = 1;i <= wv.values.length;i++){
			query.setParameter(i, wv.values[i-1]);
		}
		
		forms = query.getResultList();	
		return forms;
	}
	
	/**
	 * Gets the user forms sent by others device after the current device last sync
	 * @param Usuario
	 * @param Device
	 * @param Date
	 */
	public static List<Form> getUserFormsFromOtherDevices(Usuario user,Device device, Date lastSyncDate){
		
		List<Form> forms = new ArrayList<Form>();
		List<QueryFilter> formFilters = new ArrayList<QueryFilter>();
		final Long deviceUserId = user.id;
		final Long syncingDeviceId = device.getId();
		
		formFilters.add(new QueryFilter("synchronizedAt", lastSyncDate, OperatorType.GREATER));
		
		formFilters.add(new QueryFilter("id", OperatorType.IN, 
				new SubQuery("userform", "id_form",
						new ArrayList<QueryFilter>(){{
							add(new QueryFilter("id_deviceuser",OperatorType.EQUAL,deviceUserId));							
							add(new QueryFilter("id_device",OperatorType.DIFERENT,syncingDeviceId));							
						}}
					)
			)
		);
			
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(formFilters);
		Query query  = JPA.em().createNativeQuery(
			"select distinct on (slug) * from (select  form.* from form where " + wv.where 
			+ " order by synchronizedAt desc, slug) tb1 order by slug, createdAt desc",
			Form.class
		);	
		
		for (int i = 1;i <= wv.values.length;i++){
			query.setParameter(i, wv.values[i-1]);
		}
		
		forms = query.getResultList();	
		return forms;
	}
	
	
	/**
	 * return all form templates that will be used to search forms
	 * @throws ParseException 
	 */
	public static void getTemplates() {	
		if(deviceCanSync()){
			List<QueryFilter> filters = new ArrayList<QueryFilter>();		
			// Last syncronization made by the app
			if(!Strings.isNullOrEmpty(params.get("createdAt"))) {	
				//As the mobile delete all templates and resinsert them, if there is anyone new, we must return all
				//Date lastSyncDate = new Date(Long.parseLong(params.get("createdAt")));
				//filters.add(new QueryFilter("createdAt", lastSyncDate, ">"));			
			}		
			renderJSON(FormTemplate.getWithFilters(filters));
		}
		else{
			response.status = Http.StatusCode.UNAUTHORIZED;	
			Device device = getSyncingDevice();
			CommunicationObject commObj = new CommunicationObject();
			commObj.setData(device);
			commObj.setMsg(DeviceStatus.authenticationStatusDesc("not_allowed_to_download"));
			commObj.setStatus((device.getStatus()));
			renderText(SerializeUtil.serializerExcludeGeral().serialize(commObj));	
		}	
	}
	
	/**
	 * return the form template associated to a formData. Its used in the updateFieldType method	 * 
	 */
	private static FormTemplate getFormDataTemplate(FormData formData){
		List<QueryFilter> templateFilters = new ArrayList<QueryFilter>();
		templateFilters.add(new QueryFilter("slug", formData.getType(), OperatorType.EQUAL));	
		templateFilters.add(new QueryFilter("formVersion", formData.getFormVersion(), OperatorType.EQUAL));
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(templateFilters);
		System.out.println(wv.where);
		System.out.println(Arrays.toString(wv.values));
		FormTemplate existingTempalte = FormTemplate.find(wv.where, wv.values).first();	
		return existingTempalte;		
	}
	
	/**
	 * Insert a registry for a summary data if it does not exist. 
	 * The summary data is used when listing forms as primary and secondary descriptions
	 */
	private static void insertSummaryData(List<FormSummaryData> formSummaryDatas,Form form){
		for (FormSummaryData formSummaryData : formSummaryDatas) {
			if(formSummaryData != null){				
				formSummaryData.setForm(form);
				formSummaryData.setCreatedAt(form.getCreatedAt());					
				formSummaryData.save();							
			}									
		}
	}
	
	/**
	 * Creates a distinct field type if it does not exist  
	 * The objective is to have a collection of distinct field types filled in forms. 
	 * These field type will be used as options to filter the forms 
	 * and is reasonable only to show options that has been filled at least in one form
	 */
	private static void updateFieldType(FormData formData){	
		if(formData.getSlug() != null){
			if(!Strings.isNullOrEmpty(formData.getDataType()) && !formData.getDataType().equalsIgnoreCase("undefined")){
				FormFieldType formFieldType = new FormFieldType();
				formFieldType.setDataType(formData.getDataType());
				formFieldType.setDescription(formData.getDesc());
				formFieldType.setFormVersion(formData.getFormVersion());
				formFieldType.setSlug(formData.getSlug());
				formFieldType.setFormTemplate(getFormDataTemplate(formData));			
				
				List<QueryFilter> filters = new ArrayList<QueryFilter>();
				filters.add(new QueryFilter("slug", formFieldType.getSlug(), OperatorType.EQUAL));	
				filters.add(new QueryFilter("formVersion", formFieldType.getFormVersion(), OperatorType.EQUAL));
				filters.add(new QueryFilter("formTemplate", formFieldType.getFormTemplate(), OperatorType.EQUAL));
				
				WhereStringAndValues keyValue = QueryFilter.buildWhereWithValues(filters);
				System.out.println(keyValue.where);
				System.out.println(Arrays.toString(keyValue.values));
				FormFieldType existingFieldType = FormFieldType.find(keyValue.where, keyValue.values).first();							
				
				if(existingFieldType == null){
					formFieldType.save();	
				}	
			}				
		}
	}
	
	/**
	 * Check if the device can sync.
	 *  It can check  the device by the deviceId passed
	 *  @param String deviceId
	 *  @return boolean
	 */
	private static boolean deviceCanSync(String deviceId){
		boolean canSync = false;
		Device device = getSyncingDevice(deviceId);	
		if(device != null){
			canSync = device.isAuthorized();
		}
		return canSync;
	}
	
	/**
	 * Check if the current requesting device can sync, getting the device id from the request bofy (using getSyncingDevice)
	 *  @return boolean
	 */
	private static boolean deviceCanSync(){
		return deviceCanSync(null);
	}
	
	/**
	 * Get the device id or from the deviceId string passed
	 *  @return Device
	 */
	private static Device getSyncingDevice(String deviceId){
		Device device = null;
		
		if(deviceId == null && !Strings.isNullOrEmpty(params.get("uuid"))){	
			deviceId = params.get("uuid");			
		}
		if(deviceId != null){
			device = Device.byDeviceId(deviceId);
		}
		return device;
	}
	
	/**
	 * Get the device id from the current request body
	 *  @return Device
	 */
	private static Device getSyncingDevice(){
		return getSyncingDevice(null);
	}
}

