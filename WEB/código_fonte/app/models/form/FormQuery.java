package models.form;

import java.util.Date;
import java.util.List;

import util.Query.QueryFilter;

public class FormQuery {
	private List<QueryFilter> filters;
	private Integer page;
	private Float formVersion; 	
	private String formType;
	private Date formCreatedAtFrom;
	private Date formCreatedAtTo;
	private Date formSyncedAtFrom;
	private Date formSyncedAtTo;
	private Long unityId;
	private Long cityId;
	private Long deviceUserId;
	private boolean listOldRevisions;
	private boolean includeFormData;
	
	public FormQuery(){
		this.includeFormData = false;
	}
	
	public Long getUnityId() {
		return unityId;
	}
	public void setUnityId(Long unityId) {
		this.unityId = unityId;
	}
	public Long getCityId() {
		return cityId;
	}
	public void setCityId(Long cityId) {
		this.cityId = cityId;
	}
	public Long getDeviceUserId() {
		return deviceUserId;
	}
	public void setDeviceUserId(Long deviceUserId) {
		this.deviceUserId = deviceUserId;
	}
	public List<QueryFilter> getFilters() {
		return filters;
	}
	public void setFilters(List<QueryFilter> filters) {
		this.filters = filters;
	}
	
	public void addFilter(QueryFilter filter){
		this.filters.add(filter);
	}
	
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Float getFormVersion() {
		return formVersion;
	}
	public void setFormVersion(Float formVersion) {
		this.formVersion = formVersion;
	}
	public String getFormType() {
		return formType;
	}
	public void setFormType(String formType) {
		this.formType = formType;
	}
	public Date getFormCreatedAtFrom() {
		return formCreatedAtFrom;
	}
	public void setFormCreatedAtFrom(Date formCreatedAtFrom) {
		this.formCreatedAtFrom = formCreatedAtFrom;
	}
	public Date getFormCreatedAtTo() {
		return formCreatedAtTo;
	}
	public void setFormCreatedAtTo(Date formCreatedAtTo) {
		this.formCreatedAtTo = formCreatedAtTo;
	}
	public Date getFormSyncedAtFrom() {
		return formSyncedAtFrom;
	}
	public void setFormSyncedAtFrom(Date formSyncedAtFrom) {
		this.formSyncedAtFrom = formSyncedAtFrom;
	}
	public Date getFormSyncedAtTo() {
		return formSyncedAtTo;
	}
	public void setFormSyncedAtTo(Date formSyncedAtTo) {
		this.formSyncedAtTo = formSyncedAtTo;
	}
	public boolean isListOldRevisions() {
		return listOldRevisions;
	}
	public void setListOldRevisions(boolean listOldRevisions) {
		this.listOldRevisions = listOldRevisions;
	}
	public boolean getIncludeFormData() {
		return includeFormData;
	}
	public void setIncludeFormData(boolean withFormData) {
		this.includeFormData = withFormData;
	}
	
	
}
