package models.form;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

/**
 * Form types
 *
 */
@Entity(name="formfieldtype")
public class FormFieldType extends Model{

	public static final String TEXT = "text";
	public static final String NUMBER = "number";
	
	private String description;
	private String dataType;
	private String slug;
	private Float formVersion;
	@ManyToOne
	private FormTemplate formTemplate;

	
	public Float getFormVersion() {
		return formVersion;
	}
	public void setFormVersion(Float formVersion) {
		this.formVersion = formVersion;
	}
	/**
	 * @return the formTemplate
	 */
	public FormTemplate getFormTemplate() {
		return formTemplate;
	}
	/**
	 * @param formTemplate the formTemplate to set
	 */
	public void setFormTemplate(FormTemplate formTemplate) {
		this.formTemplate = formTemplate;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}
	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the slug
	 */
	public String getSlug() {
		return slug;
	}
	/**
	 * @param slug the slug to set
	 */
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	public static List<FormFieldType> findTemplateFields(Long idFormTemplate, Float formVersion) {
		
		/*List<QueryFilter> filters = new ArrayList<QueryFilter>();
		filters.add(new QueryFilter("formtemplate_id", idFormTemplate, OperatorType.EQUAL));	
		filters.add(new QueryFilter("formVersion", formVersion, OperatorType.EQUAL));
		filters.add(new QueryFilter("datatype", "('number','text','date','boolean','month')", OperatorType.IN));		
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<FormFieldType> formFieldTypes = FormFieldType.find(wv.where, wv.values).fetch();*/
		
		Query query  = JPA.em().createNativeQuery("select distinct on (slug) * from formFieldType where formtemplate_id = ? and formVersion = ? and datatype in ('number','text','date','boolean','month') ", FormFieldType.class);
		query.setParameter(1, idFormTemplate);
		query.setParameter(2, formVersion);
		List<FormFieldType> formFieldTypes = query.getResultList();
		
		return formFieldTypes;
	}
}
