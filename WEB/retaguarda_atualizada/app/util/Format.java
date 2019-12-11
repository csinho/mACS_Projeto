package util;

public class Format {
  
  public static String formatarCnpjCpf( String valor, String mascara ) {
    
    String dado = "";      
    // remove caracteres nao numericos
    for ( int i = 0; i < valor.length(); i++ )  {
        char c = valor.charAt(i);
        if ( Character.isDigit( c ) ) { dado += c; }
    }

    int indMascara = mascara.length();
    int indCampo = dado.length();

    for ( ; indCampo > 0 && indMascara > 0; ) {
        if ( mascara.charAt( --indMascara ) == '#' ) { indCampo--; }
    }

    String saida = "";
    for ( ; indMascara < mascara.length(); indMascara++ ) {    
        saida += ( ( mascara.charAt( indMascara ) == '#' ) ? dado.charAt( indCampo++ ) : mascara.charAt( indMascara ) );
    }    
    return saida;
  }
  
}
