package models.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.TypeDef;

import com.sun.istack.internal.Nullable;

import controllers.Secure;
import models.general.Item;
import play.db.jpa.JPA;
import types.JSONObjectUserType;
import util.Constants;
import util.Query.DataType;
import util.Query.Operator;
import util.Query.OperatorType;
import util.Query.QueryFilter;
import util.Query.SubQuery;
import util.Query.WhereStringAndValues;
import models.general.Usuario;



@Entity(name="Form")
@TypeDef(name = "CustomJsonObject", typeClass = JSONObjectUserType.class)
public class Form extends Item {	
		
	@Temporal(TemporalType.TIMESTAMP)
	protected Date synchronizedAt;
	
	protected Date parentCreatedAt;
	
	@OneToMany
	@Nullable
	@NotFound(action = NotFoundAction.IGNORE)	
	@JoinColumn(name = "id_form", nullable = true,insertable = false,updatable = false)	
	protected List<FormSummaryData> formSummaryDatas;
	
	@Transient
	protected FormSummaryData primaryFormSummary;
	
	@Transient
	protected FormSummaryData secondaryFormSummary;

	public Date getSynchronizedAt() {
		return synchronizedAt;
	}	
	

	public Date getParentCreatedAt() {
		return parentCreatedAt;
	}

	public void setParentCreatedAt(Date parentCreatedAt) {
		this.parentCreatedAt = parentCreatedAt;
	}

	public void setSynchronizedAt(Date synchronizedAt) {
		this.synchronizedAt = synchronizedAt;
	}
	
	public List<FormSummaryData> getFormSummaryDatas() {
		return formSummaryDatas;
	}

	public void setSummaryFormDataFields(List<FormSummaryData> formSummaryDatas) {
		this.formSummaryDatas = formSummaryDatas;
	}

	public Form(){
		formSummaryDatas = new ArrayList<FormSummaryData>();		
	}
	
	public FormSummaryData getPrimaryFormSummary() {
		return primaryFormSummary;
	}

	public void setPrimaryFormSummary(FormSummaryData primaryFormSummary) {
		this.primaryFormSummary = primaryFormSummary;
	}

	public FormSummaryData getSecondaryFormSummary() {
		return secondaryFormSummary;
	}

	public void setSecondaryFormSummary(FormSummaryData secondaryFormSummary) {
		this.secondaryFormSummary = secondaryFormSummary;
	}

	public void setFormSummaryDatas(List<FormSummaryData> formSummaryDatas) {
		this.formSummaryDatas = formSummaryDatas;
	}
	
	
	
	
	/**
	 * Find form by formQuery
	 * @param filters
	 * @return List<Forms>
	 */
	public static List<Form> findForms(FormQuery formQuery, int itensPerPage, Usuario usuario) {
		WhereStringAndValues wv = getFormQuery(formQuery, usuario);
		
		Query query = JPA.em().createNativeQuery("select distinct on(slug) * from (select form.* from form  where " + wv.where + " order by createdat desc, synchronizedat) forms", Form.class);
		if(formQuery.isListOldRevisions()){
			query = JPA.em().createNativeQuery("select form.* from form where " + wv.where + " order by createdat desc, synchronizedat desc", Form.class);
		}
		
		Integer offset = (formQuery.getPage()-1)*(itensPerPage);
		query.setMaxResults(itensPerPage);	
		
		query.setFirstResult(offset);
		
		for (int i = 1;i <= wv.values.length;i++){
			query.setParameter(i, wv.values[i-1]);
		}
		
		List<Form> forms = query.getResultList();		
		forms = setSummaryFields(forms);	
		
		return forms;
	}
	
	/**
	 * Get form versions
	 * @param filters
	 * @return List<Forms>
	 */
	public static List<Form> getFormRevisions(String formSlug, Long currentFormId) {	
        //not using currentFormId for now, but if we want 
        //to get all vesions, except the current, it will be necessary	
		Form form = Form.findById(currentFormId);
		List<Form> formRevisions = Form.find(" slug = ? and type = ? order by createdAt desc ", formSlug, form.getType()).fetch();
		return 	formRevisions;	
	}
	
	/**
	 * Set the summaryFields of each form on a given list
	 * @param List<Form>
	 * @return List<Forms>
	 */
	public static List<Form> setSummaryFields(List<Form> forms){
		for (Form form : forms) {
			form.setSummaryFields();
		}		
		return forms;
	}
	
	/**
	 * Set the summaryFields of a form	 	 
	 */
	public void setSummaryFields(){
		
		if(this.formSummaryDatas.size() > 0){
			this.primaryFormSummary = this.formSummaryDatas.get(0);
		}
		if(this.formSummaryDatas.size() > 1){
			this.secondaryFormSummary = this.formSummaryDatas.get(1);
		}		
	}
	
	/**
	 * Search form by QueryFilters
	 * @param QueryFilters filters
	 * @return List<Forms>
	 */
	public static List<Form> search(List<QueryFilter> filters) {
		WhereStringAndValues wv = QueryFilter.buildWhereWithValues(filters);
		List<Form> forms = Form.find(wv.where + " order by createdAt desc", wv.values).fetch();		
		return forms;	
	}
	
	/**
	 * Returns the amount of a formQuery, ignoring pagination
	 * @param formQuery
	 * @return total forms
	 */
	public static Long count(FormQuery formQuery, Usuario usuario) {				
		WhereStringAndValues wv = getFormQuery(formQuery, usuario);		
		Query query = JPA.em().createNativeQuery("select distinct on(slug) * from (select form.* from form  where " + wv.where + " order by createdat desc, synchronizedat) forms", Form.class);
		if(formQuery.isListOldRevisions()){
			query = JPA.em().createNativeQuery("select form.* from form where " + wv.where + " order by createdat desc, synchronizedat desc", Form.class);
		}		
		for (int i = 1;i <= wv.values.length;i++){
			query.setParameter(i, wv.values[i-1]);
		}		
		return  (long)query.getResultList().size();
	}
	
	/**
	 * Builds a QueryFilter based in a formQuery
	 * @param formQuery
	 * @return List<QueryFilter>
	 */
	private static WhereStringAndValues getFormQuery(FormQuery formQuery, Usuario usuario){
		List<QueryFilter> filters = setComparableColumn(formQuery.getFilters());
		List<QueryFilter> formFilters = new ArrayList<QueryFilter>();
		
		if(formQuery.getFormCreatedAtFrom() != null){
			formFilters.add(new QueryFilter("createdAt", formQuery.getFormCreatedAtFrom(),  OperatorType.GREATER_OR_EQUAL));
		}
		if(formQuery.getFormCreatedAtTo() != null){
			formFilters.add(new QueryFilter("createdAt", formQuery.getFormCreatedAtTo(), OperatorType.LESS_OR_EQUAL));
		}
		if(formQuery.getFormSyncedAtFrom() != null){
			formFilters.add(new QueryFilter("synchronizedAt", formQuery.getFormSyncedAtFrom(), OperatorType.GREATER_OR_EQUAL));
		}
		if(formQuery.getFormSyncedAtTo() != null){
			formFilters.add(new QueryFilter("synchronizedAt", formQuery.getFormSyncedAtTo(), OperatorType.LESS_OR_EQUAL));
		}
		
		formFilters.add(new QueryFilter("formVersion", formQuery.getFormVersion(), OperatorType.EQUAL));
		formFilters.add(new QueryFilter("type", formQuery.getFormType(), OperatorType.EQUAL));
		
		for (int i = 0; i < filters.size(); i++) {
			List<QueryFilter> subfilters = new ArrayList<QueryFilter>();
			subfilters.add(filters.get(i));
			//we have pais of filters (like formData.slug = 'x' and formData.value = 'y'), so the step is doubled
			//we get the current, the next and jump to the one after the next
			subfilters.add(filters.get(i+1));
			i++; //here we jumot the one after the next. the i++ in the form makes one jump and this the second
			formFilters.add(new QueryFilter("slug", new SubQuery("formData", "parentslug",subfilters), OperatorType.IN));
		}		
		formFilters = setUserFilters(formQuery, formFilters, usuario);
		return QueryFilter.buildWhereWithValues(formFilters);		
	}
	
	/**
	 * Adds the userFilter to a List<QueryFilter>  based in a formQuery
	 * It consider the profile of the current user and the city/unity/user defined in the formQuery
	 * @param List<QueryFilter>	
	 */
	private static List<QueryFilter> setUserFilters(FormQuery formQuery, List<QueryFilter> formFilters, Usuario usuario){
        
		if(usuario.getPerfil() == Constants.PERFIL_USUARIO){
			formFilters.add(
					new QueryFilter("id", OperatorType.IN, 
							new SubQuery("userform", "id_form",
									new ArrayList<QueryFilter>(){{
										add(new QueryFilter("id_deviceuser", OperatorType.IN, 
												new SubQuery("userdata", "id_user",
														new ArrayList<QueryFilter>(){{
															add(
																	new QueryFilter("id_municipio", 
																			OperatorType.EQUAL, 
																			Secure.usuarioLogado().getUserData().getMunicipio().id
																		)
																);
														}}
												)
										));
									}}
							)
					)
			);
		}
		
		if(usuario.getPerfil() == Constants.PERFIL_GESTOR_MUNICIPIO){						
			formFilters.add(
					new QueryFilter("id", OperatorType.IN, 
							new SubQuery("userform", "id_form",
									new ArrayList<QueryFilter>(){{
										add(new QueryFilter("id_deviceuser", OperatorType.IN, 
												new SubQuery("userdata", "id_user",
														new ArrayList<QueryFilter>(){{
															add(
																	new QueryFilter("id_municipio", 
																			OperatorType.EQUAL, 
																			Secure.usuarioLogado().getUserData().getMunicipio().id
																		)
																);
														}}
												)
										));
									}}
							)
					)
			);
		}
		
		if(formQuery.getDeviceUserId() != null){	
			final Long deviceUserId = formQuery.getDeviceUserId();
			formFilters.add(new QueryFilter("id", OperatorType.IN, 
					new SubQuery("userform", "id_form",
							new ArrayList<QueryFilter>(){{
								add(
										new QueryFilter(
												"id_deviceuser",
												OperatorType.EQUAL,
												deviceUserId
										)
								);
							}}
						)
				)
			);
		}
		
		if(formQuery.getCityId() != null){
			final Long cityId = formQuery.getCityId();
			formFilters.add(
					new QueryFilter("id", OperatorType.IN, 
							new SubQuery("userform", "id_form",
									new ArrayList<QueryFilter>(){{
										add(new QueryFilter("id_deviceuser", OperatorType.IN, 
												new SubQuery("userdata", "id_user",
														new ArrayList<QueryFilter>(){{
															add(
																	new QueryFilter(
																			"id_municipio", 
																			OperatorType.EQUAL, 
																			cityId
																		)
																);
														}}
												)
										));
									}}
							)
					)
			);	
		}

		if(formQuery.getUnityId() != null){
			final Long unityId = formQuery.getUnityId();
			formFilters.add(
					new QueryFilter("id", OperatorType.IN, 
							new SubQuery("userform", "id_form",
									new ArrayList<QueryFilter>(){{
										add(new QueryFilter("id_deviceuser", OperatorType.IN, 
												new SubQuery("userdata", "id_user",
														new ArrayList<QueryFilter>(){{
															add(
																	new QueryFilter("id_unidadesaude", 
																			OperatorType.EQUAL, 
																			unityId
																		)
																);
														}}
												)
										));
									}}
							)
					)
			);
		}

		return formFilters;
	}
	
	/**
	 * Defines the comparable column that will be used to filter the content based in the dataType
	 * defined in the filters
	 * In come cases the compare the filter with the column value and in other cases with the column valueDesc
	 * @param List<QueryFilter>	
	 */
	private static List<QueryFilter> setComparableColumn(List<QueryFilter> filters){
		List<QueryFilter> filtersAdjusted = new ArrayList<QueryFilter>();
		for (QueryFilter filter : filters) {
			if(filter.getColumn() == null){
				List<OperatorType> rawTypeOperators = new ArrayList<OperatorType>() {
					{	
						add(OperatorType.GREATER);
						add(OperatorType.LESS);
						add(OperatorType.GREATER_OR_EQUAL);
						add(OperatorType.LESS_OR_EQUAL);					
					}};		
				//if we are dealing with raw data (date, integer etc) operations, we should not cast it
				if( rawTypeOperators.contains(Operator.getOperatorType(filter.getOperator()))){					
					filter.setColumn("value");
				}
				else if(filter.getDataType().equals("domaindata")){
					filter.setDataType("text");
					filter.setColumn("value");
				}
				else if(Operator.getDataType(filter.getDataType()) == DataType.BOOLEAN )
				{
					filter.setColumn("value");
				}
				else{
					filter.setColumn("valueDesc");
				}
			}
			filtersAdjusted.add(filter);
		}
		return filtersAdjusted;
	}
	
	/**
	 * Returns a formSet based in a form
	 * @param idForm
	 */
	public static FormSet populateFormSet(Form form){		
		FormSet formSet = new FormSet();
		form.setSummaryFields();
		formSet.setForm(form);
		UserForm uForm =  UserForm.find(" form = ?", form).first();
		formSet.setDeviceUser(uForm.getDeviceUser());		
		
		List<FormData> formDatas = FormData.find("id_form = ? order by id", form.id).fetch();
		Integer counter = 0;
		for (FormData formData : formDatas) {			
			if(formData.getDataType().equalsIgnoreCase("associatedFormType")){				
				List<Form> childForms = getFormChildren(form);
				formData.setChildForms(childForms);
			}
			counter++;
		}
		formSet.setFormData(formDatas);
		List<FormMeta> metas = FormMeta.find("id_form = ?", form.id).fetch();
		formSet.setFormMetas(metas);
		return formSet;
	}
	
	/**
	 * Returns a collection of distinct forms (the last version of each one) that are child from the passed form
	 * @param idForm
	 */
	public static List<Form> getFormChildren(Form parentForm){
		Query query = JPA.em().createNativeQuery("select distinct on (slug) * from (select  form.* from form where parentSlug = ? and parentCreatedAt = ? order by createdAt desc, slug) tb1 order by slug, createdAt desc", Form.class);
		query.setParameter(1, parentForm.getSlug());
		query.setParameter(2, parentForm.getCreatedAt());	
		List<Form> childForms = query.getResultList();
		return childForms;
	}


	
}
