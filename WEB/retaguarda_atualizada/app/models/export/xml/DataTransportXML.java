package models.export.xml;

import javax.persistence.Entity;
import javax.persistence.Transient;

//@Entity(name = "dataTransportxml")
public class DataTransportXML {
	
	//faltando concatenar CNES 
	
	private String uuidDataSerialized;
	private Long typeDataSerialized;
	//Codigo cnes da unidade
	private String cnesDataSerialized;
	//Código IBGE da unidade
	private String codeIBGE;
	//Código INE da equipe que gerou a ficha.
	//Não obrigatório
	private String ineDataSerialized;
	//Número do lote para controle interno dos arquivos enviados.
	//Não obrigatório
	private Long numericLot;
	//Identifica a instalação que enviou os dados.
	//Identifica a instalação que cadastrou/digitou.
	@Transient
	private InstalationDataXML instalationDataXml;
	//Identifica a versão do e-SUS AB.
	@Transient
	private VersionDataEsus version;
		

	public String getUuidDataSerialized() {
		return uuidDataSerialized;
	}

	public void setUuidDataSerialized(String uuidDataSerialized) {
		this.uuidDataSerialized = uuidDataSerialized;
	}
	
	public Long getTypeDataSerialized() {
		return typeDataSerialized;
	}
	
	public void setTypeDataSerialized(Long typeDataSerialized) {
		this.typeDataSerialized = typeDataSerialized;
	}
	
	public String getCnesDataSerialized() {
		return cnesDataSerialized;
	}
	
	public void setCnesDataSerialized(String cnesDataSerialized) {
		this.cnesDataSerialized = cnesDataSerialized;
	}

	public String getCodeIBGE() {
		return codeIBGE;
	}

	public void setCodeIBGE(String codeIBGE) {
		this.codeIBGE = codeIBGE;
	}

	public String getIneDataSerialized() {
		return ineDataSerialized;
	}

	public void setIneDataSerialized(String ineDataSerialized) {
		this.ineDataSerialized = ineDataSerialized;
	}

	public Long getNumericLot() {
		return numericLot;
	}

	public void setNumericLot(Long numericLot) {
		this.numericLot = numericLot;
	}

	public InstalationDataXML getInstalationDataXml() {
		return instalationDataXml;
	}
	
	public void setInstalationDataXml(InstalationDataXML instalationDataXml) {
		this.instalationDataXml = instalationDataXml;
	}
	
	public VersionDataEsus getVersion() {
		return version;
	}

	public void setVersion(VersionDataEsus version) {
		this.version = version;
	}
}
