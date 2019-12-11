package util;

public enum EnumDataInstalationXML {
	DATA_INSTALATION("dadoInstalacao"),
	SENDER("remetente"),
	AGAINST_KEY("contraChave"),
	SOURCE("originadora"),
	UUID_INSTALATION("uuidInstalacao"),
	CPF_CNPJ("cpfOuCnpj"),
	NAME_SOCIAL_REASON("nomeOuRazaoSocial"),
	PHONE("fone"),
	EMAIL("email");
	
	private final String description;
	
	EnumDataInstalationXML(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
