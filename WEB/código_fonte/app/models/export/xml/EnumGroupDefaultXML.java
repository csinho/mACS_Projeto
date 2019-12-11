package models.export.xml;

public enum EnumGroupDefaultXML {
	VALUE_DEFAULT("Default");
	
	private final String valueDefault;
	
	private EnumGroupDefaultXML(String valueDefault) {
		this.valueDefault = valueDefault;
	}
	
	public String getDescription() {
		return valueDefault;
	}
}
