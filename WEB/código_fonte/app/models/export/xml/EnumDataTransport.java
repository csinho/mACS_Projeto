package models.export.xml;

public enum EnumDataTransport {
	UUID_DATA_SERIALIZED("uuidDadoSerializado"),
	TYPE_DATA_SERIALIZED("tipoDadoSerializado"),
	CODE_IBGE("codIbge"),
	CNES_DATA_SERIALIZED("cnesDadoSerializado"),
	INE_DATA_SERIALIZED("ineDadoSerializado"),
	NUMERIC_LOT("numLote");
	
	private final String description;
	
	private EnumDataTransport(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
