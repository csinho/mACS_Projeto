package models.export.xml;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import models.domain.Domain;
import models.domain.DomainData;
import models.form.Form;
import models.form.FormTemplate;
import models.form.UserForm;
import models.general.SystemInfo;
import models.general.UserData;
import play.Play;
import play.db.jpa.JPA;
import util.DirectoryOperations;
import util.EnumDataInstalationXML;
import util.EnumForms;
import util.EnumUrlDirectory;
import util.XML;
import util.zip.ZipUtils;


public class XmlEsus {
	private Element elementSlug = null;
	private Document documentXML = null;
	private Element elementForm = null;
	private String namespacePrefix = null;
	private String namespaceUrl = null;
	private String nameTagXml = null;
	private Element elementSender = null;
	private Element elementSource = null;
	
	public void init(){
		elementSlug = new Element("tagTemp");
		documentXML = new Document(elementSlug);
		documentXML.setRootElement(elementSlug);
		elementForm = new Element("TagFormTemp");
		elementSender = new Element(EnumDataInstalationXML.SENDER.getDescription());
		elementSource = new Element(EnumDataInstalationXML.SOURCE.getDescription());
	}
		
	public File generateDocumentXml(List<Form> listForm){
		init();
		if (listForm != null && !listForm.isEmpty()){
			Form formType = listForm.get(0);
			String idFileZip = UUID.randomUUID().toString().substring(0, 12);
			idFileZip = idFileZip.replace("-", "");
			String nameFolderZip = String.valueOf(idFileZip +"_" + formType.getType());
			String path = EnumUrlDirectory.XML.getUrl() + File.separator + nameFolderZip;
			File directoryXML = new File(EnumUrlDirectory.XML.getUrl());
			File directoryXmlTemp = new File(path);
			File source = new File(path);
			File destination = new File(EnumUrlDirectory.ZIP.getUrl() + File.separator + nameFolderZip + ".zip");
			
			if (!directoryXML.exists()){
				directoryXML.mkdirs();
			}
			
			if (!directoryXmlTemp.exists()){
				directoryXmlTemp.mkdir();
			}
			
			int fileNumber = 1;
			if (formType.getType().equalsIgnoreCase(EnumForms.VISITA_DOMICILIAR.getType())){
				createDocumentXMLVisit(listForm, fileNumber, path);
			}else{
				for (Form form : listForm){
					createDocumentXML(form, fileNumber, path);
					fileNumber ++;
				}
			}
			
			DirectoryOperations directoryOperations = new DirectoryOperations();
			if (source.exists() && !directoryOperations.isEmpty(source)){
				try {
					ZipUtils.compress(source, destination);
				} catch (IOException e) {
					e.printStackTrace();
				}
				directoryOperations.delete(source);
			}
	        return destination;
		}else{
			return null;
		}
	}
	
	private Document createDocumentXMLVisit(List<Form> listFormXML, int fileNumber, String path){
		List<FormDataXML> listFormDataXML = new ArrayList<FormDataXML>();
		
		for (Form form : listFormXML){
			
			FormTemplate formTemplate = FormTemplate.find(" slug = ? ", form.getType()).first();
			StringBuilder whereForm = new StringBuilder("slug = '" + form.getParentSlug() + "' and createdat = '" + form.getParentCreatedAt() + "' ");
			StringBuilder whereFormData = new StringBuilder("fd.id = " + form.id + " ");
			
			String queryDomicilio = "select distinct on (slug) familia.* \n"
						+	"from form familia \n"
						+	"inner join ( \n"
						+		"select distinct on (slug) 	parentslug, slug, parentcreatedat \n"
						+		"from form \n"
						+		"where " + whereForm + " \n"
						+	") formIndividuo \n"
						+	"on formIndividuo.parentslug = familia.slug and familia.createdat = formIndividuo.parentcreatedat  \n"
						+	"limit 1";
			
			Form formDomicilioFamilia = (Form) JPA.em().createNativeQuery(queryDomicilio, Form.class).getSingleResult();
			
			String queryIndividuoVisita = "select * from ( \n"
								  +	"select distinct on (slug) *"
								  + "from (\n"
								  +	"select fd.*, fftx.orderload \n"
								  +	"from formdata fd \n"
								  +	"inner join ( \n"
								  +		"select distinct on(slug) *  \n"
								  +		"from form \n"
								  +		"where " + whereForm + " \n"
								  +	") formIndividuo \n"
								  +	"on formIndividuo.slug = fd.parentslug \n"
								  + "inner join formfieldtagxml fftx \n"
								  + "on fd.slug = fftx.formfield \n"
								  + "and fd.type = fftx.formtemplatetype \n"
								  + "inner join formtemplate ft \n"
								  + "on ft.id = fftx.idformtemplate \n"
								  +	"where ft.id = " + formTemplate.id + ""
								  + "union \n"
								  + "select fd.*, fftx.orderload \n"
								  + "from formdata fd \n"
								  + "inner join formfieldtagxml fftx \n" 
								  + "on fd.slug = fftx.formfield \n"
								  + "and fd.type = fftx.formtemplatetype \n"
								  + "inner join formtemplate ft \n"
								  + "on ft.id = fftx.idformtemplate \n"
								  + "where ft.id = " + formTemplate.id + " \n"
								  + "and " + whereFormData + "\n"
								  + ") fd \n"
								  +") fd \n"
								  + "order by fd.orderload";
			
			List<FormDataXML> listFormDataXMLTemp = JPA.em().createNativeQuery(queryIndividuoVisita, FormDataXML.class).getResultList();
			
			for (FormDataXML field : listFormDataXMLTemp){
				StringBuilder where = new StringBuilder("formfield = '" + field.getSlug() + "' and formtemplatetype = '" + field.getType() + "' and idformtemplate = '" +  formTemplate.getId() + "'");
				String query = "select * from formfieldtagxml ff where " + where ;
				
				FormFieldTagXML t = (FormFieldTagXML) JPA.em().createNativeQuery(query, FormFieldTagXML.class).getSingleResult();
				
				field.setFormFieldTagXML(t);
				field.setParentSlugDomiciliar(formDomicilioFamilia.getParentSlug());
				field.setParentSlugFamilia(formDomicilioFamilia.getSlug());
				field.setParentSlugIndividuo(form.getParentSlug());
				field.setFormOrigem(form);
			}
			listFormDataXMLTemp = validFieldFormDataXML(listFormDataXMLTemp);
			
			listFormDataXML.addAll(listFormDataXMLTemp);
		}
		Collections.sort(listFormDataXML);
		
		int sizeListFormData = listFormDataXML.size();
		String currentHome = null;
		String previousHome = null;
		String currentIndividual = null;
		String previousIndividual = null;
		int controllerListForm = 0;
		Element elementNew = null;
		String id_data_serializad = null;
		int number = fileNumber;
		
		for (FormDataXML listFormData : listFormDataXML){
			controllerListForm ++;
			currentIndividual = listFormData.getParentSlugIndividuo();
			
			if (currentHome != null){
				currentHome = listFormData.getParentSlugDomiciliar();
			}
			
			if (currentHome != null){
				if (previousHome != null && !previousHome.equalsIgnoreCase(currentHome)){
					elementForm.addContent(elementNew);
					elementNew = null;
					
					elementForm.addContent(new Element("tpCdsOrigem").setText("3"));
					elementForm.addContent(new Element("uuid").setText(id_data_serializad));
					elementForm.addContent(new Element("uuidFichaOriginadora").setText(id_data_serializad));
					
					documentXML.getRootElement().addContent(elementForm);
					documentXML.getRootElement().addContent(elementSender);
					documentXML.getRootElement().addContent(elementSource);
					
					versionDataEsus();
					
					XML xml = new XML();
					String pathTemp = path + File.separator + nameTagXml + "_" + String.valueOf(number) + ".esus.xml";
					number ++;
					xml.saveXML(documentXML, pathTemp);
					currentHome = null;
					init();
				}
			}
			
			if (currentHome == null){
				currentHome = listFormData.getParentSlugDomiciliar();
				
				UserForm userForm = UserForm.find(" formslug = ? ", listFormData.getParentSlug()).first();
				UserData userData = UserData.find(" id_user = ? ", userForm.getDeviceUser().getId()).first();
				
				if (!validField(userData)){
					return null;
				}
				
				id_data_serializad = userData.getUnidadeSaude().getCnes().toString() + "-" + UUID.randomUUID().toString();
				createDataDefaultXML(listFormData.getFormOrigem(), userData, id_data_serializad);
				
				if (nameTagXml == null || nameTagXml.isEmpty()){
					return null;
				}
				elementForm.setName(nameTagXml);
				elementForm.setNamespace(Namespace.getNamespace(namespacePrefix, namespaceUrl));
				
				generalData(userData, listFormData.getFormOrigem().getCreatedAt(), listFormData.getFormOrigem().getType());
				
				elementNew = new Element(listFormData.getFormFieldTagXML().getGroupXML().getName());
			}
			
			if (previousIndividual != null){
				if (previousHome.equalsIgnoreCase(currentHome) && !previousIndividual.equals(currentIndividual)){
					elementForm.addContent(elementNew);
					elementNew = new Element(listFormData.getFormFieldTagXML().getGroupXML().getName());
				}
			}
			
			elementNew.addContent(new Element(listFormData.getFormFieldTagXML().getTagXML()).setText(listFormData.getValue()));
			
			if (sizeListFormData == controllerListForm){
				elementForm.addContent(elementNew);
				
				elementForm.addContent(new Element("tpCdsOrigem").setText("3"));
				elementForm.addContent(new Element("uuid").setText(id_data_serializad));
				elementForm.addContent(new Element("uuidFichaOriginadora").setText(id_data_serializad));
				
				documentXML.getRootElement().addContent(elementForm);
				documentXML.getRootElement().addContent(elementSender);
				documentXML.getRootElement().addContent(elementSource);
				
				versionDataEsus();

				
				XML xml = new XML();
				String pathTemp = path + File.separator	 + nameTagXml + "_" + String.valueOf(number) + ".esus.xml";
				number++;
				xml.saveXML(documentXML , pathTemp);
			}
			
			previousHome = currentHome;
			previousIndividual = currentIndividual;
		}
		return documentXML;
	}
	
	private Document createDocumentXML(Form formXML, int fileNumber, String path){
		elementSlug = new Element("tagTemp");
		documentXML = new Document(elementSlug);
		documentXML.setRootElement(elementSlug);
		elementForm = new Element("TagFormTemp");
		elementSender = new Element(EnumDataInstalationXML.SENDER.getDescription());
		elementSource = new Element(EnumDataInstalationXML.SOURCE.getDescription());
		
		UserForm userForm = UserForm.find(" id_form = ? ", formXML.getId()).first();
		UserData userData = UserData.find(" id_user = ? ", userForm.getDeviceUser().getId()).first();
		/*
		if (!validField(userData)){
			return null;
		}*/
		
		String id_data_serializad = userData.getUnidadeSaude().getCnes().toString() + "-" + UUID.randomUUID().toString();
		createDataDefaultXML(formXML, userData, id_data_serializad);
		
		if (nameTagXml == null || nameTagXml.isEmpty()){
			return null;
		}

		elementForm.setName(nameTagXml);
		elementForm.setNamespace(Namespace.getNamespace(namespacePrefix, namespaceUrl));
		
		if (!formXML.getType().equalsIgnoreCase("atividadeColetiva")){
			generalData(userData, formXML.getCreatedAt(), formXML.getType());
		}
		
		List<FormDataXML> listFormDataProfessional = null;
		List<FormDataXML> listFormData = returnFormData(formXML);
		int sizeListFormData = listFormData.size();
		
		if (formXML.getType().equalsIgnoreCase("atividadeColetiva")){
			elementForm.addContent(new Element("tpCdsOrigem").setText("3"));
			listFormDataProfessional = listProfessionals(listFormData);
		}
		
		if (listFormData == null || listFormData.isEmpty()){
			return null;
		}
		
		String group = null;
		int controllerListForm = 0;
		Element elementNew = null;
		int numberAnimals = 0;
		boolean latestPosition = false;
		String pet = null;
		Domain domain = null;
		boolean havePets = false;
		int DescriptionOtherCondition = 0;
		boolean professionalControl = true;
		String slugPrevious = null;
		
		for (FormDataXML formDataReturn : listFormData){
			String currentGroup = formDataReturn.getFormFieldTagXML().getGroupXML().getName();
			controllerListForm++;
			
			if (slugPrevious == null){
				slugPrevious = formDataReturn.getSlug();
			}
			
			if (formXML.getType().equalsIgnoreCase("cadastroIndividual")){
				if (formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("descricaoOutraCondicao")){
					DescriptionOtherCondition ++;
					formDataReturn.getFormFieldTagXML().setTagXML(formDataReturn.getFormFieldTagXML().getTagXML() + String.valueOf(DescriptionOtherCondition));
				}
			}
			
			if (pet != null && !formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("animaisNoDomicilio")){
				elementForm.addContent(new Element("quantosAnimaisNoDomicilio").setText(String.valueOf(numberAnimals)));
				pet = null;
			}
			
			if (formDataReturn.getDataType().equalsIgnoreCase("associatedFormType")){
				associatedFormType(formDataReturn, formXML);				
				continue;
			}
			
			if (formXML.getType().equalsIgnoreCase("atividadeColetiva")){
				if (formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("responsavelCnesUnidade")){
					elementForm.addContent(new Element("responsavelCns").setText(userData.getCns()));
				}
				
				if (formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("cns") || formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("codigoCbo2002")){
					if (professionalControl){
						professionalControl = false;
						
						if (listFormDataProfessional != null){
							int tamanho = listFormDataProfessional.size();
							int count = 0;
							Element e = null;
							boolean abrirTag = false;
							
							for (FormDataXML value : listFormDataProfessional){
														
								count ++;
								if (count == 1){
									e = new Element(value.getFormFieldTagXML().getGroupXML().getName());
									e.addContent(new Element("cns").setText(userData.getCns()));
									e.addContent(new Element("codigoCbo2002").setText(userData.getCns()));
									elementForm.addContent(e);
									abrirTag = true;
								}
								if (abrirTag){
									e = new Element(value.getFormFieldTagXML().getGroupXML().getName());
									abrirTag = false;
								}
								e.addContent(new Element(value.getFormFieldTagXML().getTagXML()).setText(value.getValue()));
								if (value.getFormFieldTagXML().getLatestPosition() || tamanho == count){
									elementForm.addContent(e);
									abrirTag = true;
								}
								
								
							}
							
						}
					}
					continue;
				}
			}
			
			
			if (group == null){
				if (!formDataReturn.getFormFieldTagXML().getGroupXML().getName().equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
					elementNew = new Element(currentGroup);
					group = currentGroup;
				}else{
					elementForm.addContent(new Element(formDataReturn.getFormFieldTagXML().getTagXML()).setText(formDataReturn.getValue()));
					continue;
				}	
			}else{
				if (!group.equalsIgnoreCase(currentGroup)){					
					if (!formDataReturn.getFormFieldTagXML().getGroupXML().getName().equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
						if (!group.equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
							elementForm.addContent(elementNew);
						}
						elementNew = new Element(currentGroup);
					}else{
						elementForm.addContent(elementNew);
						
					}
					latestPosition = false;
					group = currentGroup;
				}
			}
			
			if (!formDataReturn.getFormFieldTagXML().getGroupXML().getName().equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
				elementNew.addContent(new Element(formDataReturn.getFormFieldTagXML().getTagXML()).setText(formDataReturn.getValue()));
			}
					
			if ((latestPosition && !slugPrevious.equalsIgnoreCase(formDataReturn.getSlug())) || sizeListFormData == controllerListForm){
				group = currentGroup;			
				if (!group.equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
					elementForm.addContent(elementNew);
				}	
			}
			
			if (formDataReturn.getFormFieldTagXML().getGroupXML().getName().equalsIgnoreCase(EnumGroupDefaultXML.VALUE_DEFAULT.getDescription())){
				
				if (formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("stAnimaisNoDomicilio")){
					if (formDataReturn.getValue().equalsIgnoreCase("true")){
						havePets = true;
						elementForm.addContent(new Element(formDataReturn.getFormFieldTagXML().getTagXML()).setText(formDataReturn.getValue()));
						continue;
					}
				}
				
				if (havePets && formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("animaisNoDomicilio")){
					if (pet == null){
						pet = formDataReturn.getFormFieldTagXML().getTagXML();
						domain = Domain.find(" slug = 'animais' ").first();
					}
					numberAnimals += Integer.parseInt(formDataReturn.getValue());
					DomainData domainData = DomainData.find(" domaincode = ? and slug = ? ", domain.id, formDataReturn.getFormFieldTagXML().getFormField()).first();
					formDataReturn.setValue(domainData.getValue());
					elementForm.addContent(new Element(formDataReturn.getFormFieldTagXML().getTagXML()).setText(formDataReturn.getValue()));
					continue;
				}
				
				if (!havePets && formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("animaisNoDomicilio")){
					continue;
				}
				elementForm.addContent(new Element(formDataReturn.getFormFieldTagXML().getTagXML()).setText(formDataReturn.getValue()));
				
				if (pet != null && (!formDataReturn.getFormFieldTagXML().getTagXML().equalsIgnoreCase("animaisNoDomicilio") || sizeListFormData == controllerListForm)){
					elementForm.addContent(new Element("quantosAnimaisNoDomicilio").setText(String.valueOf(numberAnimals)));
					pet = null;
				}
			}
			slugPrevious = formDataReturn.getSlug();
			latestPosition = formDataReturn.getFormFieldTagXML().getLatestPosition();
			
		}
		
		if (!formXML.getType().equalsIgnoreCase("atividadeColetiva")){
			elementForm.addContent(new Element("tpCdsOrigem").setText("3"));
		}
		elementForm.addContent(new Element("uuid").setText(id_data_serializad));
		elementForm.addContent(new Element("uuidFichaOriginadora").setText(id_data_serializad));
		
		documentXML.getRootElement().addContent(elementForm);
		documentXML.getRootElement().addContent(elementSender);
		documentXML.getRootElement().addContent(elementSource);
		
		versionDataEsus();
		XML xml = new XML();
		String caminho = path + "/";
		xml.saveXML(documentXML, caminho  + nameTagXml + "_" + fileNumber +".esus.xml" );
        
		return documentXML;
	}
	
	private List<FormDataXML> listProfessionals(List<FormDataXML> listFormData){
		List<FormDataXML> listFormDataProfessionals = new ArrayList<FormDataXML>();

		for (FormDataXML profissional : listFormData){
			if (profissional.getFormFieldTagXML().getTagXML().equals("codigoCbo2002") || profissional.getFormFieldTagXML().getTagXML().equals("cns")){
				listFormDataProfessionals.add(profissional);
			}
		}
		Collections.sort(listFormDataProfessionals);
		return listFormDataProfessionals;
	}
	
	private void associatedFormType(FormDataXML formDataReturn, Form form){
		List<Form> listAssociatedForm = returnAssociatedForm(form);
		
		for(Form formFamily : listAssociatedForm){
			List<FormDataXML> listFormDataFamily = returnAssociatedFormDataXMLFamily(formFamily, form.getType());
			Element elementNew = null;
			
			int tamanho = listFormDataFamily.size();
			int controller = 0;
			
			for(FormDataXML formDataXML : listFormDataFamily){
				controller ++;
				if (controller == 1){
					elementNew = new Element(formDataXML.getFormFieldTagXML().getGroupXML().getName());
				}
				elementNew.addContent(new Element(formDataXML.getFormFieldTagXML().getTagXML()).setText(formDataXML.getValue()));
				if (tamanho == controller){
					elementForm.addContent(elementNew);
				}
			}
		}
	}
	
	private boolean validField(UserData userData){
		// validar cpf ou cnpj
		
		if (userData == null){
			return false;
		}
		
		if (userData.getMunicipio() == null){
			return false;
		}
		
		if (userData.getMunicipio().getCodigo() == null){
			return false;
		}
		
		if (userData.getUnidadeSaude() == null || userData.getUnidadeSaude().getCnes() == null){
			return false;
		}
		
		if (userData.getUnidadeSaude() == null || userData.getUnidadeSaude().getCnpj() == null || userData.getUnidadeSaude().getCnpj().isEmpty()){
			return false;
		}
		
		if (userData.getCns().isEmpty()){
			return false;
		/*}else{
			ValidarCns validarCns = new ValidarCns();
			if (!validarCns.validaCns(userData.getCns())){
				return false;
			}*/
		}
		
		return true;
	}
	
	private void createDataDefaultXML(Form formXML, UserData userData, String id_data_serializad){
		FormTemplate formTemplate = FormTemplate.find(" slug = ? ", formXML.getType()).first();
		List<HeaderXML> listHeaderXML = HeaderXML.find("idformTemplate = ?", formTemplate.id).fetch();
		
		for(HeaderXML header : listHeaderXML){
			if (header.getTagDefault()){
				elementSlug.setName(header.getNamespaceXml().getName());
				elementSlug.setNamespace(Namespace.getNamespace(header.getNamespaceXml().getPrefix(), header.getNamespaceXml().getUrl()));
			}else{
				elementSlug.addNamespaceDeclaration(Namespace.getNamespace(header.getNamespaceXml().getPrefix(), header.getNamespaceXml().getUrl()));
				if (header.getNamespaceXml().getName().equalsIgnoreCase(formXML.getType())){
					namespacePrefix = header.getNamespaceXml().getPrefix();
					namespaceUrl = header.getNamespaceXml().getUrl();
					nameTagXml = header.getNamespaceXml().getNameTagXML();
				}
			}
			
			if (header.getNamespaceXml().getName().equalsIgnoreCase(EnumDataInstalationXML.DATA_INSTALATION.getDescription())){
				
				elementSender.setNamespace(Namespace.getNamespace(header.getNamespaceXml().getPrefix(), header.getNamespaceXml().getUrl()));
				elementSource.setNamespace(Namespace.getNamespace(header.getNamespaceXml().getPrefix(), header.getNamespaceXml().getUrl()));
				
				SystemInfo systemInfo = SystemInfo.findById(1L);
				String against_key = systemInfo.getIdSystem().replace(" ", "") + "-" + systemInfo.getVersion();
				
				elementSender.addContent(new Element(EnumDataInstalationXML.AGAINST_KEY.getDescription()).setText(against_key));
				elementSource.addContent(new Element(EnumDataInstalationXML.AGAINST_KEY.getDescription()).setText(against_key));
				
				String uuid_instalation = systemInfo.getIdSystem();
				elementSender.addContent(new Element(EnumDataInstalationXML.UUID_INSTALATION.getDescription()).setText(uuid_instalation));
				elementSource.addContent(new Element(EnumDataInstalationXML.UUID_INSTALATION.getDescription()).setText(uuid_instalation));
				
				if (userData.getUnidadeSaude() != null && userData.getUnidadeSaude().getCnpj() != null && !userData.getUnidadeSaude().getCnpj().isEmpty()){
					elementSender.addContent(new Element(EnumDataInstalationXML.CPF_CNPJ.getDescription()).setText(userData.getUnidadeSaude().getCnpj()));
					elementSource.addContent(new Element(EnumDataInstalationXML.CPF_CNPJ.getDescription()).setText(userData.getUnidadeSaude().getCnpj()));
				}
				
				if (userData.getUnidadeSaude() != null && !userData.getUnidadeSaude().getNomeFantasia().isEmpty()){
					elementSender.addContent(new Element(EnumDataInstalationXML.NAME_SOCIAL_REASON.getDescription()).setText(userData.getUnidadeSaude().getNomeFantasia()));
					elementSource.addContent(new Element(EnumDataInstalationXML.NAME_SOCIAL_REASON.getDescription()).setText(userData.getUnidadeSaude().getNomeFantasia()));
				}
				
				if (userData.getUnidadeSaude().getTelefone() != null && !userData.getUnidadeSaude().getTelefone().isEmpty()){
					elementSender.addContent(new Element(EnumDataInstalationXML.PHONE.getDescription()).setText(userData.getUnidadeSaude().getTelefone()));
					elementSource.addContent(new Element(EnumDataInstalationXML.PHONE.getDescription()).setText(userData.getUnidadeSaude().getTelefone()));
				}
				
				if (userData.getUnidadeSaude().getEmail() != null && !userData.getUnidadeSaude().getEmail().isEmpty()){
					elementSender.addContent(new Element(EnumDataInstalationXML.EMAIL.getDescription()).setText(userData.getUnidadeSaude().getEmail()));
					elementSource.addContent(new Element(EnumDataInstalationXML.EMAIL.getDescription()).setText(userData.getUnidadeSaude().getEmail()));
				}
			}
		}
		
		elementSlug.addContent(new Element(EnumDataTransport.UUID_DATA_SERIALIZED.getDescription()).setText(id_data_serializad));
		elementSlug.addContent(new Element(EnumDataTransport.TYPE_DATA_SERIALIZED.getDescription()).setText(typeDataSerialized(formXML.getType()).toString()));
		elementSlug.addContent(new Element(EnumDataTransport.CODE_IBGE.getDescription()).setText(userData.getMunicipio().getCodigo().toString()));
		elementSlug.addContent(new Element(EnumDataTransport.CNES_DATA_SERIALIZED.getDescription()).setText(userData.getUnidadeSaude().getCnes().toString()));
	}
	
	private void generalData(UserData userData, Date dateService, String formType){
		Element elementGeral = new Element("dadosGerais");
		if (formType.equalsIgnoreCase("visita")){
			elementGeral.setName("headerTransport");
		}else{
			
		}
		
		if (!userData.getCns().isEmpty()){
			elementGeral.addContent(new Element("cnsProfissional").setText(userData.getCns()));
		}
		elementGeral.addContent(new Element("cnesUnidadeSaude").setText(userData.getUnidadeSaude().getCnes().toString()));
		
		if (!userData.getMicroarea().isEmpty()){
			elementGeral.addContent(new Element("microarea").setText(userData.getMicroarea()));
		}
		
		if (dateService != null){
			elementGeral.addContent(new Element("dataAtendimento").setText(String.valueOf(dateService.getTime())));
		}
		elementGeral.addContent(new Element("codigoIbgeMunicipio").setText(userData.getMunicipio().getCodigo().toString()));
		elementForm.addContent(elementGeral);
	}
	
	private void versionDataEsus(){
		List<VersionDataEsus> listVersion = VersionDataEsus.findAll();
		if (listVersion != null && !listVersion.isEmpty()){
			for (VersionDataEsus version : listVersion){
				if (version.getListaAttribute() != null && !version.getListaAttribute().isEmpty()){
					Element elementVersion = new Element(version.getTag());
					for (AttributeXml attribute: version.getListaAttribute()){
						elementVersion.setAttribute(attribute.getAttribute(), attribute.getValue());
					}
					elementSlug.addContent(elementVersion);
				}
			}
		}
	}
	
	private Long typeDataSerialized(String type){
		TypeDataSerialized typeDataSerializad = TypeDataSerialized.find(" type = ? ", type).first();
		return typeDataSerializad.id;
	}
	
	private String buildQuery(Form form, FormTemplate formTemplate){
		
		StringBuilder whereFormData = new StringBuilder("id_form = " + form.id + " ");
		String query;
		
		if (form.getType().equalsIgnoreCase("cadastroDomiciliar")){
			query = "select fd.* \n" 
					+"from formdata fd \n"
					+"inner join formfieldtagxml fftx \n"
					+ "on fd.slug = fftx.formfield \n"
					+ "and fd.type = fftx.formtemplatetype \n"
					+ "inner join formtemplate ft \n"
					+ "on ft.id = fftx.idformtemplate \n"
					+ "where ft.id = " + formTemplate.getId() + " \n"
					+ "and fd." + whereFormData + "\n"
					+ "order by orderload";

			return query;
		}
		
		if (form.getType().equalsIgnoreCase("cadastroIndividual")){
			StringBuilder parentslug = new StringBuilder("id_form = " + form.id + " and slug = 'chefeDaFamilia' ");
			StringBuilder slug = new StringBuilder("id_form = " + form.id + " ");
			
			query =	"select fd.id, fd.createdat, fd.datatype, fd.description, fd.formversion, fd.parententity, fd.parentslug, fd.slug as slug, fd.status, fd.type, fd.synchronizedat, fd.value, fd.valuedesc, fd.id_form, fd.targetreferencecreatedat \n"
								   + "from ( \n"
								   +	"select fdsc.id, fdsc.createdat, fdsc.datatype, fdsc.description, fdsc.formversion, fdsc.parententity, fdsc.parentslug, fdsc.slug, fdsc.status, fdsc.type, fdsc.synchronizedat, fdsc.value, fdsc.valuedesc, fdsc.id_form, fdsc.targetreferencecreatedat \n"
								   +	"from formdata fdsc \n"
								   +	"where " + slug + "\n" 
								   +	"union \n" 
								   +	"select fds.id + 1000000, fds.createdat, fds.datatype, fds.description, fds.formversion, fds.parententity, fds.parentslug, fds.slug || 'chefe', fds.status, fds.type, fds.synchronizedat, fds.value, fds.valuedesc, fds.id_form, fds.targetreferencecreatedat \n"
								   +	"from form f \n"
								   +	"inner join \n"
								   +	"( \n"
								   +		"select fdss.value, fdss.targetreferencecreatedat \n"
								   +		"from formdata fdss \n"
								   +		"where " + parentslug + " \n" 
								   +	") fdr \n"
								   +	"on fdr.value = f.slug and fdr.targetreferencecreatedat = f.createdat "
								   +	"inner join formdata fds \n"
								   +	"on fds.parentslug = f.slug \n"
								   +	"where fds.slug = 'dataNascimento' \n"
								   +	"or fds.slug = 'numeroCartaoSUS' \n"
								   +	") fd \n"
								   + "inner join formfieldtagxml fftx \n"
								   + "on fd.slug = fftx.formfield \n"
								   + "and fd.type = fftx.formtemplatetype \n"
								   + "inner join formtemplate ft \n"
								   + "on ft.id = fftx.idformtemplate \n"
								   + "where ft.id = " + formTemplate.getId() + " \n"
								   + "order by orderload";
			return query;
		}
		
		if (form.getType().equalsIgnoreCase("visita")){
			query = "select * \n"
							+ 	"from formdata fd \n"
							+	"inner join formfieldtagxml fx \n" 
							+	"on fx.formfield = fd.slug \n" 
							+	"and fx.formtemplatetype = fd.type \n"
							+	"where idformtemplate = " + formTemplate.getId() + " \n"
							+	"and " +  whereFormData + " \n"
							+	"order by fx.idformtemplate asc, fx.orderload asc";
			return query;

		}
		
		if (form.getType().equalsIgnoreCase("atividadeColetiva")){
			query = "select fd.* \n"
					+"from formdata fd \n"
					+"inner join formfieldtagxml fftx \n"
					+"on fd.slug = fftx.formfield \n"
					+"and fd.type = fftx.formtemplatetype \n"
					+"inner join formtemplate ft \n"
					+"on ft.id = fftx.idformtemplate \n"
					+"where ft.id = " + formTemplate.getId() + " \n"
					+"and fd." + whereFormData + "\n"
					+"order by orderload";
			return query;

		}
		
		return null;
		
	}
	
	private List<FormDataXML> returnFormData(Form form){
		FormTemplate formTemplate = formTemplate(form.getType());
		
		String queryResult = buildQuery(form, formTemplate);
		
		//String q = "select distinct on (slug, parentslug)* from formdata where "  + whereForm.toString();
		List<FormDataXML> listFormDataXML = JPA.em().createNativeQuery(queryResult, FormDataXML.class).getResultList();
		
		listFormDataXML = fillFormFieldTagXML(listFormDataXML, form.getType());
		
		listFormDataXML = validFieldFormDataXML(listFormDataXML);
		return listFormDataXML;
	}
	
	private List<FormDataXML> validFieldFormDataXML(List<FormDataXML> listFormDataTemp){
		List<FormDataXML> listFormDataXML = new ArrayList<FormDataXML>();
		
		for (FormDataXML data : listFormDataTemp){
			
			if (data.getFormFieldTagXML() != null  && data.getValue() != null  && !data.getValue().isEmpty() && !data.getValue().equalsIgnoreCase("null")){
				if (data.getDataType().equalsIgnoreCase("date") || data.getDataType().equalsIgnoreCase("month")){
					
					SimpleDateFormat format = null;
					if (data.getSlug().equalsIgnoreCase("residenteDesde")){
						format = new SimpleDateFormat("yyyy-MM");
					}else{
						format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					}
					
					try {
						Date d = format.parse(data.getValue());
						data.setValue(String.valueOf(d.getTime()));
					} catch (ParseException e) {
						e.printStackTrace();
					} 
				}
				
				if (data.getDataType().equalsIgnoreCase("time")){
					 SimpleDateFormat format = new SimpleDateFormat("HH:mm");
					 String hora = data.getValue().substring(11, 16);
					try {
						Date time = format.parse(hora);
						data.setValue(String.valueOf(time.getTime()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				listFormDataXML.add(data);
			}
		}
		return listFormDataXML;
	}
	
	private List<Form> returnAssociatedForm(Form form){
		return Form.find(" parentslug = ? and parentcreatedat = ? ", form.getSlug(), form.getCreatedAt()).fetch();
	}
	
	private List<FormDataXML> returnAssociatedFormDataXMLFamily(Form form, String type){
		FormTemplate formTemplate = formTemplate(type);
		
		StringBuilder parentslugForm = new StringBuilder("parentslug = '" + form.getSlug() + "' and parentcreatedat = '" + form.getCreatedAt() + "' ");
		StringBuilder parentslugFormData = new StringBuilder("id_form = " + form.id);
		StringBuilder whereSlug = new StringBuilder("slug = '" + form.getSlug() + "' and f.createdat = '" + form.getCreatedAt() + "' ");
		
		String query = "select fd.id, fd.createdat, fd.datatype, fd.description, fd.formversion, fd.parententity, fd.parentslug, fd.slug as slug, fd.status, fd.type, fd.synchronizedat, fd.value, fd.valuedesc, fd.id_form, fd.targetreferencecreatedat\n"
						+"from ("
						+"select * \n"
						+"from ( \n"
						+"select * \n"
						+"from ( \n"
						+"select * \n"
						+"from ( \n"
						+"select fd.*, fftx.orderload \n"
						+"from formdata fd \n"
						+"inner join formfieldtagxml fftx \n"
						+"on fd.slug = fftx.formfield \n"
						+"and fd.type = fftx.formtemplatetype \n" 
						+"inner join formtemplate ft \n"
						+"on ft.id = fftx.idformtemplate \n"
						+"where ft.id = " + formTemplate.getId() + "\n"
						+"and fd." + parentslugFormData + "\n"
						+")familia \n"
						+"union \n"
						+"select fd.*, fftx.orderload \n"
						+"from ( \n"
						+"select value \n"
						+"from formdata fd \n"
						+"where fd." + parentslugFormData + "\n"
						+"and slug = 'chefeDaFamilia' \n"
						+")chefe \n"
						+"inner join formdata fd \n"
						+"on fd.parentslug = chefe.value \n"
						+"inner join formfieldtagxml fftx \n"
						+"on fd.slug = fftx.formfield \n"
						+"and fd.type = fftx.formtemplatetype \n" 
						+"inner join formtemplate ft \n"
						+"on ft.id = fftx.idformtemplate \n"
						+"where ft.id = " + formTemplate.getId() + "\n"
						+") fd \n"
						+"where slug <> 'chefeDaFamilia' \n"
						+"union \n"
						+"select fd.*, fftx.orderload \n"
						+"from ( \n"
						+"select cast(fd.id as bigint), createdat, cast(datatype as character varying(255)), cast(description as character varying(255)), cast(formversion as real), cast(parententity as character varying(255)), cast(parentslug as character varying(255)), cast(slug as character varying(255)), cast(status as character varying(255)), cast(type as character varying(255)), to_timestamp(cast(synchronizedat as text), 'yyyy-MM-dd HH24:mm:ss') as synchronizedat, cast(value as text ), cast(valuedesc as text), cast(id_form as bigint), cast(cast(form as character varying(255)) as bytea),cast(dtype as character varying(255)), parentcreatedat \n" 
						+"from ( \n"
						+"select 1 as id, individuo.createdat, 'number' as datatype, 'QuantidadedeIndividuos' as description, individuo.formversion, 'form' as parententity, 'form' as parentslug, 'quantidadeIndividuos' as slug, 'status' as status, 'familia' as type, '2016-01-01' as synchronizedat, individuo.value, 'descricao' as valuedesc, individuo.id_form, '' as formfieldtagxml, '' as form, 'FormData' as dtype, individuo.parentcreatedat \n"
						+"from( \n"
						+"select id_form.value, f.id as id_form, f.parentcreatedat, f.createdat, f.formversion \n"
						+"from (\n"
						+"select count(*) as value  \n"
						+"from ( \n"
						+ "select distinct on (slug) *\n"
						+ "from form \n"
						+"where " + parentslugForm + "\n"
						+")abc \n"
						+")as id_form \n"
						+"inner join form f\n"
						+"on f." + whereSlug + "\n"
						+")as individuo \n"
						+")as fd \n"
						+") as fd \n"
						+"inner join formfieldtagxml fftx \n" 
						+"on fd.slug = fftx.formfield \n" 
						+"and fd.type = fftx.formtemplatetype \n" 
						+"inner join formtemplate ft  \n"
						+"on ft.id = fftx.idformtemplate  \n"
						+"where ft.id = " + formTemplate.id + "\n"
						+") resul \n"
						+"order by resul.orderload \n"
						+") as fd";
		
		List<FormDataXML> listFormDataXML = JPA.em().createNativeQuery(query, FormDataXML.class).getResultList();
		
		listFormDataXML = fillFormFieldTagXML(listFormDataXML, type);
		
		listFormDataXML = validFieldFormDataXML(listFormDataXML);
		return listFormDataXML;
	}
	
	private List<FormDataXML> fillFormFieldTagXML(List<FormDataXML> listFormDataXML, String type){
		FormTemplate formTemplate = formTemplate(type);
		
		for (FormDataXML formData : listFormDataXML){
			StringBuilder where = new StringBuilder("formfield = '" + formData.getSlug() + "' and formtemplatetype = '" + formData.getType() + "' and idformtemplate = '" +  formTemplate.getId() + "'");
			String query = "select * from formfieldtagxml ff where " + where ;
			
			FormFieldTagXML formFieldTagXML = (FormFieldTagXML) JPA.em().createNativeQuery(query, FormFieldTagXML.class).getSingleResult();
			formData.setFormFieldTagXML(formFieldTagXML);
			
		}
		return listFormDataXML;
	}
	
	private FormTemplate formTemplate(String type){
		return FormTemplate.find(" slug = ? ", type).first();
	}
	
}