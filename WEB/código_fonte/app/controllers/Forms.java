package controllers;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import models.domain.Domain;
import models.domain.DomainData;
import models.export.xml.FileDownloads;
import models.export.xml.XmlEsus;
import models.form.Form;
import models.form.FormExport;
import models.form.FormFieldType;
import models.form.FormQuery;
import models.form.FormSet;
import models.form.FormTemplate;
import models.general.PaginacaoRetorno;
import models.general.Usuario;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import util.Constants;
import util.EnumUrlDirectory;
import util.Pair;
import util.RequiresProfile;
import util.SerializeUtil;
import util.Query.Operator;
import util.Query.OperatorType;
import util.Query.QueryFilter;


/**
 * Controller de fomulairos
 * 
 * @author joaoraf
 *s
 */
@With({CapturaException.class, AccessControlAllowOriginFilter.class, Secure.class})
public class Forms extends Controller {

	/**
	 * REnder all forms template
	 * @throws ParseException 
	 */	
	public static void getFormTemplates() {		
		List<QueryFilter> filters = new ArrayList<QueryFilter>();	
		renderJSON(FormTemplate.getWithFilters(filters));
	}
	
	/**
	 * Get form versions
	 * @param filters
	 */
	public static void getFormRevisions(String formSlug, Long currentFormId) {	
		
		List<Form> forms = Form.getFormRevisions(formSlug, currentFormId);		
		renderText(SerializeUtil.serializerExcludeGeral().include()
				.exclude("form.value","value").serialize(forms));
	}

	/**
	 * Render the formTemplate fields 
	 * @param idFormTemplate
	 */	
	public static void getFields(Long idFormTemplate) {
		FormTemplate formTemplate = FormTemplate.findById(idFormTemplate);
		List<FormFieldType> fields = FormFieldType.findTemplateFields(idFormTemplate, formTemplate.getFormVersion());
		for (FormFieldType field : fields) {
			field.setFormTemplate(null);
		}
		renderText(SerializeUtil.serializerExcludeGeral().include("id", "description").serialize(fields));
	}

	/**
	 * Render the formset by a form id	 
	 * @param idForm
	 */	
	public static void getFormSet(Long idForm) {		
		Form form = Form.findById(idForm);
		FormSet formSet = Form.populateFormSet(form);		
		renderText(SerializeUtil.serializerExcludeGeral().include("id", "desc","formData.childForms","formData","formMetas","deviceUser")
				.exclude("deviceUser.login","deviceUser.token","formData.childForms.value","form.value","formData.form","formMetas.form","synchronizedAt").serialize(formSet));
	}

	/**
	 * Render the formset by a formslug and a date (a composed pk)
	 * @param idForm
	 */	
	public static void getFormSetByFormSlugAndDate() {
			
		if(!Strings.isNullOrEmpty(params.get("formCreatedAt")) && !Strings.isNullOrEmpty(params.get("formSlug"))){
			// We receive the updatedAt as milliseconds, so we parse it
			Date formCreatedAt = new Date(Long.parseLong(params.get("formCreatedAt")));	
			String formSlug = params.get("formSlug");
			
			Form form = Form.find(" slug = ? and createdAt = ?",formSlug, formCreatedAt).first();
			if(form != null){
				FormSet formSet = Form.populateFormSet(form);		
				renderText(SerializeUtil.serializerExcludeGeral().include("id", "desc","formData.childForms","formData","formMetas","deviceUser")
						.exclude("deviceUser.login","deviceUser.token","formData.childForms.value","form.value","formData.form","formMetas.form","synchronizedAt").serialize(formSet));
			}			
		}
	}
	

	/**
	 * Page form
	 */	
	public static void index() {
		render();
	}

    /**
	 * Search forms by FormQuery
	 */	
	public static void search() {
		FormQuery formQuery = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(params.get("formQuery"), FormQuery.class);
		PaginacaoRetorno pg = new PaginacaoRetorno();
		Usuario usuario = Secure.usuarioLogado();		
		List<Form> forms = Form.findForms(formQuery,10, usuario);
		pg.setLista(forms);
		pg.setTotal(Form.count(formQuery, usuario));
		renderText(SerializeUtil.serializerGeral().serialize(pg));
	}
	
	
	
	/**
	 * Exports the forms defined by the FormExport id list
	 */
	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void exportSelected() {
		GsonBuilder builder = new GsonBuilder(); 
		FormExport formExport = builder.create().fromJson(new InputStreamReader(request.body), FormExport.class);
		List<Long> formsId = formExport.getFormsId();	
		Query query = JPA.em().createNativeQuery("select * from form where id in :idList", Form.class);
        query.setParameter("idList", formsId);			
		List<Form> forms = query.getResultList();
        
		XmlEsus xml = new XmlEsus();
		File destino = xml.generateDocumentXml(forms);
		String url = EnumUrlDirectory.ROUTE_DOWNLOADS.getUrl() + "?name=" + destino.getName();
		
		if (destino.exists()) {
			renderText(url);
		} else {
			renderText("false");
		}
	}
	
	
    /**
	 * Downloads a file identified by a string id
	 * @param idForm
	 */	
	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void downloads(String name) {
		String destino = EnumUrlDirectory.ZIP.getUrl() +  File.separator + name;
		
		FileDownloads downloads = new FileDownloads();
		File fileTmp = new File(destino);
		InputStream file = downloads.prepararArquivo(fileTmp);
		fileTmp.delete();
		
		if (file != null){
			response.setContentTypeIfNotSet("application/zip");
			renderBinary(file, name);
		}
		error();
	}
    
	/**
	 * Exports, as download, all the forms matching the criteria defined by formQuery InputStreamReader
	 */
	@RequiresProfile({Constants.PERFIL_GESTOR_UNIDADE, Constants.PERFIL_GESTOR_ESTADO, Constants.PERFIL_GESTOR_MUNICIPIO, Constants.PERFIL_ADMINISTRADOR})
	public static void exportAll() {
		FormQuery formQuery = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(new InputStreamReader(request.body), FormQuery.class);
		Usuario usuario = Secure.usuarioLogado();
		List<Form> forms = Form.findForms(formQuery, Integer.MAX_VALUE, usuario);	
		
		XmlEsus xml = new XmlEsus();
		File destino = xml.generateDocumentXml(forms);
		String url = EnumUrlDirectory.ROUTE_DOWNLOADS.getUrl() + "?name=" + destino.getName();
		
		if (destino.exists()) {
			renderText(url);
		} else {
			renderText("false");
		}
	}
	
	/**
	 * Returns the options of an specified domain.
	 */	
	public static void getDomainOptions(String slug){
		List<DomainData> domainOptions = new ArrayList<DomainData>();
		List<Domain> domains = Domain.find(" slug = ?", slug).fetch();
		if(domains.size() > 0){
			domainOptions = DomainData.find(" domaincode = ?", domains.get(0).id).fetch();
		}		
		renderText(SerializeUtil.serializerExcludeGeral().serialize(domainOptions));		
	}
	
	/**
	 * Returns the operators available to a dataType
	 */	
	public static void getOperators(String dataType) {	
		if(dataType.equals("domaindata")){
			List<Pair> operList = new ArrayList<Pair>();			
			operList.add(new Pair<>(OperatorType.EQUAL, Operator.operatorName().get(OperatorType.EQUAL)));
			renderText(SerializeUtil.serializerExcludeGeral().serialize(operList));
		}
		else{
			renderText(SerializeUtil.serializerExcludeGeral().serialize(Operator.getDataTypeOperators(dataType)));
		}		
	}
}
