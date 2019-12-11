package util;

import java.util.ArrayList;
import java.util.List;

public class Perfil {
  private String descricao;
  private String codigo;
  
  public String getDescricao() {
    return descricao;
  }
  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }
  public String getCodigo() {
    return codigo;
  }
  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }
  
  public static List<Perfil> listarPerfis (String perfilUsuarioLogado) {
 
    List<Perfil> perfils = new ArrayList<Perfil>();
    Perfil p = null;
    
    if (perfilUsuarioLogado.equals(Constants.PERFIL_GESTOR_UNIDADE)){
    	p = new Perfil();
	    p.setCodigo(Constants.PERFIL_USUARIO);
	    p.setDescricao("Usuário");
	    perfils.add(p);
	    return perfils;
    }
    
    if (perfilUsuarioLogado.equals(Constants.PERFIL_GESTOR_MUNICIPIO)){
    	p = new Perfil();
	    p.setCodigo(Constants.PERFIL_GESTOR_UNIDADE);
	    p.setDescricao("Gestor da Unidade");
	    perfils.add(p);
	    return perfils;
    }
    
    if (perfilUsuarioLogado.equals(Constants.PERFIL_GESTOR_ESTADO)){
    	p = new Perfil();
	    p.setCodigo(Constants.PERFIL_GESTOR_MUNICIPIO);
	    p.setDescricao("Gestor do Município");
	    perfils.add(p);
	    return perfils;
    }
    
    if (perfilUsuarioLogado.equals(Constants.PERFIL_ADMINISTRADOR)){
    	p = new Perfil();
        p.setCodigo(Constants.PERFIL_ADMINISTRADOR);
        p.setDescricao("Administrador");
        perfils.add(p);

        p = new Perfil();
        p.setCodigo(Constants.PERFIL_USUARIO);
        p.setDescricao("Usuário");
        perfils.add(p);
        
        p = new Perfil();
        p.setCodigo(Constants.PERFIL_GESTOR_ESTADO);
        p.setDescricao("Gestor do Estado");
        perfils.add(p);
        
        p = new Perfil();
        p.setCodigo(Constants.PERFIL_GESTOR_MUNICIPIO);
        p.setDescricao("Gestor do Município");
        perfils.add(p);
        
        p = new Perfil();
        p.setCodigo(Constants.PERFIL_GESTOR_UNIDADE);
        p.setDescricao("Gestor da Unidade");
        perfils.add(p);
        return perfils;
    }
    
    
    
    return perfils; 
  }
  
  
}
