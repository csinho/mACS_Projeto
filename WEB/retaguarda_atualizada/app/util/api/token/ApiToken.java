package util.api.token;

	

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.general.Usuario;
import play.Play;

/**
 * Created by thiagoaos on 24/05/2016.
 */
public class ApiToken {
  
  private ApiToken() {
    throw new IllegalAccessError("Utility class");
  }
  
  /**
   * Gera um token, no formato JWT, com base nas informações passadas.
   * Este vai ser utilizado para identificar um usuário.
   *
   * @param user objeto usuário contendo as informações utilizadas para gerar o token
   * @return Token no formato como string utilizando o padrão JWT
   */  
  public static String generate(Usuario user) {
    return generate(user, null);
  }
  
  /**
   * Assinatura detalhada para gerar um token.
   * Aceita um horário de criação do token
   * 
   * @param tokenTime dia e horário que o token vai ser gerado
   */
  public static String generate(Usuario user, DateTime tokenTime) {
    if (tokenTime == null) {
      tokenTime = new DateTime();
    }
	
    JwtBuilder builder = Jwts.builder().setIssuedAt(tokenTime.toDate())
        .setSubject(user.getId().toString()).claim("name", user.getNome())
        .setIssuer(Play.configuration.getProperty("application.prettyName"))
        .signWith(SignatureAlgorithm.HS512, Play.configuration.getProperty("application.apisecret"));

    int timeToExpiration =
        Integer.parseInt(Play.configuration.getProperty("application.timeToExpiration"));

    builder.setExpiration(tokenTime.plusMinutes(timeToExpiration).toDate());

    return builder.compact();
  }
  
  public static void validateToken(String token)throws ExpiredJwtException{
	  Jwts.parser().setSigningKey(Play.configuration.getProperty("application.apisecret")).parseClaimsJws(token);
  }
  
  public static String updateToken(String token, Integer duracaoApiTokenDia)throws Exception{
	  Long id = Long.parseLong(Jwts.parser().setSigningKey(Play.configuration.getProperty("application.apisecret")).parseClaimsJws(token).getBody().getSubject());
	  
	  DateTime tokenTime = new DateTime();
	  
	  Usuario usuario = Usuario.find(" id = ? ", id).first();
	  
	  if (usuario != null){
		  JwtBuilder builder = Jwts.builder().setIssuedAt(tokenTime.toDate())
			        .setSubject(usuario.id.toString()).claim("name", usuario.getNome())
			        .setIssuer(Play.configuration.getProperty("application.prettyName"))
			        .signWith(SignatureAlgorithm.HS512, Play.configuration.getProperty("application.apisecret"));
		  
		  Calendar c = Calendar.getInstance();
		  c.add(Calendar.DAY_OF_YEAR, duracaoApiTokenDia);
			
		  Date dataExpiracao = new Date(c.getTimeInMillis());
		  
		  builder.setExpiration(dataExpiracao);
		  
		  return builder.compact();  
	  }
	  throw new Exception("Acesso negado.");
  }
	  
	 
	  
  
}
