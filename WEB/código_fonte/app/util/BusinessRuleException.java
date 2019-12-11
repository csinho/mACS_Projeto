package util;

public class BusinessRuleException extends Exception {
	
	private Integer status = 400;
	
	public BusinessRuleException(String mensagem) {
		super(mensagem);
	}
	
	public BusinessRuleException(String mensagem, Integer status) {
		super(mensagem);
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}	
	
}