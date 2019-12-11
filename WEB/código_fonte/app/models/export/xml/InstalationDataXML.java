package models.export.xml;

public class InstalationDataXML {
	//Identifica o software que gerou o dado (pec/cds, cdsOff ou software de terceiros).
	//Regras: Seguir o padrão <Nome do software do município> - Versão 2015
	//Observações: Campo serve para a identificar a instalação que gerou / cadastrou / enviou os dados.
	private String againstKey;
	//É um identificador da instalação do software que gerou o dado. Seja ele o e-SUS ou software de terceiro. Cada e-SUS possui um UUID.
	//Observações: Campo serve para a identificar a instalação que gerou / cadastrou / enviou os dados.
	private String uuidInstalation;
	//CPF do responsável ou CNPJ da empresa responsável.
	private String cpfOrCnpj;
	//Nome do responsável ou razão social da empresa responsável.
	private String nameOrSocialReason;
	//Telefone da pessoa ou empresa responsável.
	//Não obrigatório.
	private String phone;
	//Email da pessoa ou empresa responsável.
	//Não obrigatório.
	private String email;
	
	public String getAgainstKey() {
		return againstKey;
	}
	
	public void setAgainstKey(String againstKey) {
		this.againstKey = againstKey;
	}
	
	public String getUuidInstalation() {
		return uuidInstalation;
	}
	
	public void setUuidInstalation(String uuidInstalation) {
		this.uuidInstalation = uuidInstalation;
	}
	
	public String getCpfOrCnpj() {
		return cpfOrCnpj;
	}
	
	public void setCpfOrCnpj(String cpfOrCnpj) {
		this.cpfOrCnpj = cpfOrCnpj;
	}
	
	public String getNameOrSocialReason() {
		return nameOrSocialReason;
	}
	
	public void setNameOrSocialReason(String nameOrSocialReason) {
		this.nameOrSocialReason = nameOrSocialReason;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
}
