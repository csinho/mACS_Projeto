package models.general;

public class CommunicationObject {
    private boolean success = true;
    private String status;
    private Object data;
    private String msg;
    private Long total;
    private String token;
    
    public String getToken() {
		return token;
	}
    
    public void setToken(String token) {
		this.token = token;
	}
    
    public CommunicationObject() {
    	this.status = "not_registered";
    }
    
    public static CommunicationObject sucesso(Object data, String msg) {
    	CommunicationObject objeto = new CommunicationObject();
        objeto.setMsg(msg);
        objeto.setData(data);        
        return objeto;
    }
    
    public static CommunicationObject sucesso(Object data,Long total, String mensagem) {
    	CommunicationObject objeto = CommunicationObject.sucesso(data, mensagem);
        objeto.setTotal(total);
        
        return objeto;
    }
    
    public static CommunicationObject error(String msg) {
    	CommunicationObject objeto = new CommunicationObject();
        objeto.setSuccess(false);
        objeto.setMsg(msg);        
        return objeto;
    }
    
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
}
