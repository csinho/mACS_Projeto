package util;

public enum EnumForms {
	CADASTRO_DOMICILIAR("cadastroDomiciliar"),
	CADASTRO_INDIVIDUAL("cadastroIndividual"),
	VISITA_DOMICILIAR("visita"),
	ATIVIDADE_COLETIVA("atividadeColetiva");
	
	private String type;
	
	private EnumForms(String type){
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
