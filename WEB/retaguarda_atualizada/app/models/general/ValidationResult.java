package models.general;

public class ValidationResult {

	public String message;
	public boolean isValid;
	
	public ValidationResult() {
		this.setValid(true);
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
