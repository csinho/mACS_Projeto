package models.general;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.mail.EmailException;

import notifiers.Mails;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import util.Constants;
import util.Query.QueryFilter;
import util.Query.WhereStringAndValues;

@Entity(name="usuario")
public class Usuario extends Model {

    private static final String Perfil = null;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 64)
    private byte[] senha;

    @Column(length = 20)
    private String token;

    @Column(length = 100)
    private String email;

    @Column(name = "primeiro_acesso")
    private boolean primeiroAcesso = true;

    private String perfis;
    
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinColumn(name = "user_data")
    private UserData userData;
    
	@Transient
    private String pass;
	
	@Column(name = "api_token", length = 512)
	private String apiToken;
	
	@Column(name = "renovacao_api_token_automatico")
	private Boolean renovarApiTokenAutomatico;
	
	@Column(name = "duracao_api_token_dia")
	private Integer duracaoApiTokenDia;
	
	@Transient
	private Date DataExpiracaoToken;
	
	@Transient
	private static Long totalRegistroLista;
	
	@Transient
	private List<Device> listaDispositivos;
	
	public List<Device> getListaDispositivos() {
		return listaDispositivos;
	}
	
	public void setListaDispositivos(List<Device> listaDispositivos) {
		this.listaDispositivos = listaDispositivos;
	}
	
	public Integer getDuracaoApiTokenDia() {
		return duracaoApiTokenDia;
	}
	
	public void setDuracaoApiTokenDia(Integer duracaoApiTokenDia) {
		this.duracaoApiTokenDia = duracaoApiTokenDia;
	}
	
	public Date getDataExpiracaoToken() {
		return DataExpiracaoToken;
	}
	
	public void setDataExpiracaoToken(Date dataExpiracaoToken) {
		DataExpiracaoToken = dataExpiracaoToken;
	}
	
	public Boolean getRenovarApiTokenAutomatico(){
		return renovarApiTokenAutomatico;
	}
	
	public void setRenovarApiTokenAutomatico(boolean renovarApiTokenAutomatico) {
		this.renovarApiTokenAutomatico = renovarApiTokenAutomatico;
	}
	
    public boolean isPrimeiroAcesso() {
        return primeiroAcesso;
    }

    public void setPrimeiroAcesso(boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public byte[] getSenha() {
        return senha;
    }

    public void setSenha(byte[] senha) {
        this.senha = senha;
    }

	public String getPerfis() {
        return perfis;
    }

    public void setPerfis(String perfis) {
        this.perfis = perfis;
    }
    
    public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
	
	public String getApiToken() {
		return apiToken;
	}
	
	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}
	
    public static byte[] getSha512(String value) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String gerarToken() {
        return RandomStringUtils.randomAlphanumeric(20);
    }

    public static String gerarSenha(Integer tamanho) {
        return RandomStringUtils.randomAlphanumeric(tamanho);
    }

    public boolean atualizarSenha() {
        Usuario usuario = Usuario.findById(this.id);

        if (!usuario.getToken().equals(this.getToken()))
            return false;

        usuario.setSenha(Usuario.getSha512(this.getPass()));
        usuario.setToken(null);
        usuario.setPrimeiroAcesso(false);
        usuario.save();

        return true;

    }

    public static CommunicationObject enviarRecuperacaoSenha(String login) throws MalformedURLException, EmailException {
        Usuario usuario = Usuario.find("login = ?", login).first();
        CommunicationObject commObj = new CommunicationObject();
        commObj.setMsg("Foi enviado um email com as informações para criação de uma nova senha.");

        if (usuario == null) {
        	commObj.setMsg("Usuário inválido.");
        	commObj.setSuccess(false);
        }

        if (!commObj.isSuccess())
            return commObj;

        try {

            String token = Usuario.gerarToken();

            usuario.setToken(token);
            usuario.save();

            Mails.comprovante(usuario);

        } catch (Exception e) {
        	commObj.setMsg("Erro ao recuperar senha.");
        	commObj.setSuccess(false);
        }

        return commObj;

    }

    /*
     * Listagem dos usuarios
     */
    public static PaginacaoRetorno listar(int pagina, int porPagina, String nome, Usuario usuarioLogado) {
        PaginacaoRetorno pgnRtn = new PaginacaoRetorno();
        String where = null;
        
        if (usuarioLogado.getPerfis().equals(Constants.PERFIL_GESTOR_UNIDADE)){
        	where = "where u.perfis = '" + Constants.PERFIL_USUARIO + "'" +"\r\n";
        	where += "and ud.id_unidadesaude = " + usuarioLogado.getUserData().getUnidadeSaude().getId() + "\r\n";
        	where += (!nome.isEmpty() ? " and upper(u.nome) like upper('"+nome+"')" + "\r\n": "");
        	where += "order by nome";
        	
        	pgnRtn.setLista(listarUsuarioPorPagina(pagina, porPagina, where));
        	pgnRtn.setTotal(totalRegistroLista);
        	totalRegistroLista = 0l;
        	return pgnRtn;
        }
        
        if (usuarioLogado.getPerfis().equals(Constants.PERFIL_GESTOR_MUNICIPIO)){
        	where = "where u.perfis = '" + Constants.PERFIL_GESTOR_UNIDADE + "'" +"\r\n";
        	where += "and ud.id_municipio = " + usuarioLogado.getUserData().getMunicipio().getId() + "\r\n";
        	where += (!nome.isEmpty() ? " and upper(u.nome) like upper('"+nome+"')" + "\r\n": "");
        	where += "order by nome";
        	
        	pgnRtn.setLista(listarUsuarioPorPagina(pagina, porPagina, where));
        	pgnRtn.setTotal(totalRegistroLista);
        	totalRegistroLista = 0l;
        	return pgnRtn;
        }
        
        if (usuarioLogado.getPerfis().equals(Constants.PERFIL_GESTOR_ESTADO)){
        	where = "where u.perfis = '" + Constants.PERFIL_GESTOR_MUNICIPIO + "'" +"\r\n";
        	where += (!nome.isEmpty() ? " and upper(u.nome) like upper('"+nome+"')" + "\r\n": "");
        	where += "order by nome";
        	
        	pgnRtn.setLista(listarUsuarioPorPagina(pagina, porPagina, where));
        	pgnRtn.setTotal(totalRegistroLista);
        	totalRegistroLista = 0l;
        	return pgnRtn;
        }
        
        if (usuarioLogado.getPerfis().equals(Constants.PERFIL_ADMINISTRADOR)){
        	where = (!nome.isEmpty() ? " upper(nome) like upper('"+nome+"')": "");
        	pgnRtn.setLista(Usuario.find(where + " order by nome").fetch(pagina, porPagina));
        	pgnRtn.setTotal(Usuario.count(where));
        	return pgnRtn;
        }
        
        return pgnRtn;
    }

    public static List<Usuario> list(List<QueryFilter> filters, int pagina, int porPagina) {
    	WhereStringAndValues keyValue = QueryFilter.buildWhereWithValues(filters);
		return Usuario.find(keyValue.where + " order by id", keyValue.values).fetch(pagina, porPagina);
    }
    
    public static Long total(List<QueryFilter> filters) {
    	WhereStringAndValues keyValue = QueryFilter.buildWhereWithValues(filters);
		return Usuario.count(keyValue.where, keyValue.values);
    }
    
    /**
     * Return the list of users that are also device users
     * @return a list of device users
     */
    public static List<Usuario> listDeviceUsers() {
    	//TODO: Use column "canUseDevice" instead of type check
    	return Usuario.find("userData.tipo.canUseDevice = true order by nome").fetch();
    }
    
    /**
     * Search for the name of a device user
     * TODO: Search also for the cns	
     * @param term a part of the name of the user
     * @return the list of device user found
     */
    public static List<Usuario> searchDeviceUsers(String term) {
    	return Usuario.find("upper(nome) like upper(?) and userData.tipo.canUseDevice = true order by nome", "%"+term+"%").fetch();
    }
    
    
    public Usuario salvar() {
        return ((Usuario) this.merge()).save();
    }

    public static CommunicationObject salvarForm(Usuario usu) throws MalformedURLException, EmailException {
    	CommunicationObject commObj = new CommunicationObject();

        // Validações
        if (usu.getNome() == null) {
        	commObj.setMsg("Campo Nome é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
        }

        if (usu.getId() == null && usu.getEmail() == null) {
        	commObj.setMsg("Campo Email é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
        }

        if (usu.getId() == null && usu.getLogin() == null) {
        	commObj.setMsg("Campo Login é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
        }

        if (usu.getPerfis() == null) {
        	commObj.setMsg("Campo Perfils é obrigatório!");
        	commObj.setSuccess(false);
            return commObj;
        }

        if (usu.getId() == null) {
            // Testando se já tem usuario cadastrado com este Login
            Usuario validaLogin = (Usuario) Usuario.find("login = ?", usu.getLogin()).first();
            if (validaLogin != null) {
            	commObj.setMsg("Já existe usuário cadastrado com este Login!");
            	commObj.setSuccess(false);
                return commObj;
            }

            // Testando se já tem usuario cadastrado com este email
            Usuario validaEmail = (Usuario) Usuario.find("email = ?", usu.getEmail()).first();
            if (validaEmail != null) {
            	commObj.setMsg("Já existe usuário cadastrado com este email!");
            	commObj.setSuccess(false);
                return commObj;
            }
        }

        if (usu.getId() == null) {
            usu.setToken(usu.gerarToken());
            usu.setPass(usu.gerarSenha(8));
            usu.setSenha(usu.getSha512(usu.getPass()));
        }

        if (usu.getId() != null) {
            Usuario usuarioBanco = Usuario.findById(usu.getId());

            usu.setLogin(usuarioBanco.getLogin());
            usu.setEmail(usu.getEmail());
            usu.setSenha(usuarioBanco.getSenha());
            usu.setToken(usuarioBanco.getToken());
        }

        try {
        	UserData userData = usu.getUserData();
        	if(userData != null){
        		userData.setUser(usu);
        	}
        	
            usu.salvar();

        } catch (Exception e) {
        	commObj.setMsg("Erro ao tentar salvar o usuário.");
        	commObj.setSuccess(false);
            return commObj;
        }

        if (usu.getId() == null) {
            Mails.usuarioNovo(usu);
            commObj.setMsg("O usuário " + usu.getNome()
                    + " foi cadastrado com sucesso. Um email de confirmação foi enviado ao mesmo.");
        } else
        	commObj.setMsg("O usuário " + usu.getNome() + " foi atualizado com sucesso.");

        return commObj;

    }    

    public static CommunicationObject deletarForm(Long id) {
    	CommunicationObject obj = new CommunicationObject();

        Usuario usu = (Usuario) Usuario.find("id = ?", id).first();

        try {
            obj.setMsg("Registro excluído com sucesso");
            usu.deletar(id);

        } catch (Exception e) {
            obj.setMsg("Erro ao tentar excluir o registro, tente novamente!");
            obj.setSuccess(false);
        }

        return obj;
    }

    public static void deletar(Long id) {
        ((Usuario) Usuario.findById(id)).delete();
    }

    public static String getPerfil() {
        return Perfil;
    }

    public boolean salvarNovaSenha() {
        Usuario usuario = Usuario.findById(this.id);

        usuario.setSenha(Usuario.getSha512(this.getPass()));
        usuario.save();

        return true;

    }
    
    private static List<Usuario> listarUsuarioPorPagina(int pagina, int porPagina, String where){
    	
    	String queryLista = "select u.*\r\n" + 
		    			"from usuario u\r\n" + 
		    			"inner join userdata ud\r\n" + 
		    			"on ud.id_user = u.id\r\n" + 
		    			where;

    	Query query = JPA.em().createNativeQuery(queryLista, Usuario.class);
    	totalRegistroLista = (long) JPA.em().createNativeQuery(queryLista, Usuario.class).getResultList().size();
    	
    	Integer primeiroItem = (pagina - 1)*(porPagina);
		query.setMaxResults(porPagina);
		query.setFirstResult(primeiroItem);
    	return query.getResultList();
    }
    
    public static Usuario verificarApiToken(String token){
    	return Usuario.find(" api_token = ? ", token).first();
    }
    
    /**
     * Busca os dispositivos associados aos usuários logados
     * @param usuario
     * @return lista de dispositivos associaos ao usuário.
     */
    public static List<Device> dispositivosUsuario(Usuario usuario){
    	if (usuario != null && usuario.getPerfis().equalsIgnoreCase("usuario")){
    		return Device.find(" id_deviceuser = ? ", usuario.getId()).fetch();
    	}
    	return new ArrayList<>();
    }
}
