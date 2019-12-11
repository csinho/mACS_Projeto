package models.export.xml;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.form.FormTemplate;
import play.db.jpa.Model;

@Entity(name = "formfieldtagxml")
public class FormFieldTagXML extends Model{
	@Column(name = "formfield", nullable = false)
	private String formField;
	@Column(name = "tagxml", nullable = false)
	private String tagXML;
	@Column(name = "latestposition", nullable = false)
	private boolean latestPosition;
	@Column(name = "mandatoryexport", nullable = false)
	private boolean mandatoryExport;
	@ManyToOne
	@JoinColumn(name = "idformtemplate", nullable = false)
	private FormTemplate formTemplate;
	@ManyToOne
	@JoinColumn(name = "idgroupxml", nullable = false)
	private GroupXML groupXML;
	@Column(name = "formtemplatetype", nullable = false)
	private String formTemplateType;
	@Transient
	private List<FormDataXML> listFormData;
	@Column(name = "orderLoad", nullable = false)
	private int orderLoad;
	
	public List<FormDataXML> getListFormData() {
		return listFormData;
	}
	
	public void setListFormData(List<FormDataXML> listFormData) {
		this.listFormData = listFormData;
	}
	
	public String getFormField() {
		return formField;
	}
	
	public void setFormField(String formField) {
		this.formField = formField;
	}
	
	public String getTagXML() {
		return tagXML;
	}
	
	public void setTagXML(String tagXML) {
		this.tagXML = tagXML;
	}
	
	public boolean getLatestPosition(){
		return this.latestPosition;
	}
	
	public void setLatestPosition(boolean latestPosition) {
		this.latestPosition = latestPosition;
	}
	
	public GroupXML getGroupXML() {
		return groupXML;
	}
	
	public void setGroupXML(GroupXML groupXML) {
		this.groupXML = groupXML;
	}
	
	public boolean getMandatoryExport() {
		return mandatoryExport;
	}
	
	public void setMandatoryExport(boolean mandatoryExport) {
		this.mandatoryExport = mandatoryExport;
	}
	
	public FormTemplate getFormTemplate() {
		return formTemplate;
	}
	
	public void setFormTemplate(FormTemplate formTemplate) {
		this.formTemplate = formTemplate;
	}

	public String getFormTemplateType() {
		return formTemplateType;
	}

	public void setFormTemplateType(String formTemplateType) {
		this.formTemplateType = formTemplateType;
	}
	
	public int getOrderLoad() {
		return orderLoad;
	}
	
	public void setOrderLoad(int orderLoad) {
		this.orderLoad = orderLoad;
	}
}