package models.export.xml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import models.form.Form;
import models.form.FormData;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class FormDataXML extends FormData implements Comparable<FormDataXML> {
	
	@Transient
	private FormFieldTagXML formFieldTagXML;
	
	@Transient
	private String parentSlugDomiciliar;
	
	@Transient
	private String parentSlugFamilia;
	
	@Transient
	private String parentSlugIndividuo;
	
	@Transient
	private Form formOrigem;
	
	public Form getFormOrigem() {
		return formOrigem;
	}
	
	public void setFormOrigem(Form formOrigem) {
		this.formOrigem = formOrigem;
	}
	
	public String getParentSlugFamilia() {
		return parentSlugFamilia;
	}

	public void setParentSlugFamilia(String parentSlugFamilia) {
		this.parentSlugFamilia = parentSlugFamilia;
	}

	public String getParentSlugIndividuo() {
		return parentSlugIndividuo;
	}

	public void setParentSlugIndividuo(String parentSlugIndividuo) {
		this.parentSlugIndividuo = parentSlugIndividuo;
	}

	public String getParentSlugDomiciliar() {
		return parentSlugDomiciliar;
	}
	
	public void setParentSlugDomiciliar(String parentSlugDomiciliar) {
		this.parentSlugDomiciliar = parentSlugDomiciliar;
	}
	
	public FormFieldTagXML getFormFieldTagXML() {
		return formFieldTagXML;
	}
	
	public void setFormFieldTagXML(FormFieldTagXML formFieldTagXML) {
		this.formFieldTagXML = formFieldTagXML;
	}
	
	@Override
	public int compareTo(FormDataXML o) {
		
		if (o.getForm().getType().equalsIgnoreCase("atividadeColetiva")){
			if (id < o.id){
				return -1;
			}
		
			if (id > o.id){
				return 1;
			}
			return 0;
		}
		
		if (getParentSlugDomiciliar().equalsIgnoreCase(o.getParentSlugDomiciliar())){
			if (getParentSlugFamilia().equals(o.getParentSlugFamilia())){
				
				if (getParentSlugIndividuo().equals(o.getParentSlugIndividuo())){
					
					if (formFieldTagXML.getOrderLoad() < o.formFieldTagXML.getOrderLoad()){
						return -1;
					}
				
					if (formFieldTagXML.getOrderLoad() > o.formFieldTagXML.getOrderLoad()){
						return 1;
					}
					return 0;
				}
				
				return getParentSlugIndividuo().compareTo(o.getParentSlugIndividuo());
			}
			return getParentSlugFamilia().compareTo(o.getParentSlugFamilia());
		}
		
		return getParentSlugDomiciliar().compareTo(o.getParentSlugDomiciliar()); 
	}
}
