package models.form;

import org.joda.time.DateTime;

import controllers.DeviceUsers;
import models.general.Usuario;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public class FormSet {
	
	public FormSet(){
		this.formMetas = new ArrayList<FormMeta>();
		this.formData = new ArrayList<FormData>();
		this.formSummaryDatas = new ArrayList<FormSummaryData>();
	}
	
	private Form form;
	
	private List<FormMeta> formMetas;
	
	private List<FormData> formData;
	
	private List<FormSearchable> formSearchables;
	
	private List<FormSummaryData> formSummaryDatas;
	
	private DateTime synchronizedAt;	
	
	private Usuario deviceUser;	

	public DateTime getSynchronizedAt() {
		return synchronizedAt;
	}

	public void setSynchronizedAt(DateTime synchronizedAt) {
		this.synchronizedAt = synchronizedAt;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public List<FormMeta> getFormMetas() {
		return formMetas;
	}

	public void setFormMetas(List<FormMeta> metas) {
		this.formMetas = metas;
	}
	
	public void addMeta(FormMeta item) {
		this.formMetas.add(item);
	}

	public List<FormData> getFormData() {
		return formData;
	}

	public void setFormData(List<FormData> formData) {
		this.formData = formData;
	}
	
	public List<FormSearchable> getFormSearchables() {
		return formSearchables;
	}

	public void setFormSearchables(List<FormSearchable> formSearchables) {
		this.formSearchables = formSearchables;
	}
	
	public void addFormData(FormData formDataItem) {
		this.formData.add(formDataItem);
	}

	public List<FormSummaryData> getFormSummaryDatas() {
		return formSummaryDatas;
	}

	public void setFormSummaryDatas(List<FormSummaryData> formSummaryDatas) {
		this.formSummaryDatas = formSummaryDatas;
	}
	
	public void addFormSummary(FormSummaryData formSummaryDatas) {
		this.formSummaryDatas.add(formSummaryDatas);
	}

	public Usuario getDeviceUser() {
		return deviceUser;
	}

	public void setDeviceUser(Usuario deviceUser) {
		this.deviceUser = deviceUser;
	}

	

	
}
