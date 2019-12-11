package notifiers;
 
import java.net.MalformedURLException;

import org.apache.commons.mail.EmailException;

import models.general.Usuario;
import play.mvc.Mailer;
 
public class Mails extends Mailer {
 
   public static void comprovante(Usuario usuario) throws MalformedURLException, EmailException {
      setSubject("Redefinir senha");
      addRecipient(usuario.getEmail());
      setFrom("Nao Responda <naoresponda.macs@sesab.ba.gov.br>");
      String url = play.mvc.Http.Request.current().host;
      send(usuario,url);
   }
   
   public static void usuarioNovo (Usuario usuario) throws MalformedURLException, EmailException {
     setSubject("Acesso ao Sistema de Retaguarda - Mapa da Saúde");
     addRecipient(usuario.getEmail());
     setFrom("Nao Responda <naoresponda.macs@sesab.ba.gov.br>");
     String url = play.mvc.Http.Request.current().host;
     send(usuario,url);
  }
 
}