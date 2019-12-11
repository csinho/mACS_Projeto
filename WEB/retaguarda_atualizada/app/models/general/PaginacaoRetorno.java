package models.general;

import java.util.List;

public class PaginacaoRetorno {
	
  private Object lista;

  private Long total;
  
  public Object getLista() {
    return lista;
  }

  public void setLista(Object lista) {
    this.lista = lista;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }
 
}
