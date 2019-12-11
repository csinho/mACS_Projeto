package util;

public class Logger {
	public static void info(String mensagem, Object... args) {
		info(null, mensagem, args);
	}
	public static void info(Throwable e, String mensagem, Object... args) {		
		play.Logger.info(e, prepararMensagem(mensagem), args);
	}	
	public static void error(String mensagem, Object... args) {
		error(null, mensagem, args);
	}
	public static void error(Throwable e, String mensagem, Object... args) {		
		play.Logger.error(e, prepararMensagem(mensagem), args);
	}
	
	static String prepararMensagem(String mensagem) {	
		String nomeDoMetodo = null;
		
		StackTraceElement[] stack = Thread.currentThread().getStackTrace(); 
		
		//pega o método que chamou
		for (int i = 0; i < stack.length; i++) {
			StackTraceElement ste = stack[i];
						
            if (ste.getClassName().equals(Logger.class.getName())) {
            	StackTraceElement proximoSTE = stack[i + 1];
            	nomeDoMetodo = proximoSTE.getClassName() + "." + proximoSTE.getMethodName();
            	
            	if ( !proximoSTE.getClassName().equals(Logger.class.getName()))
            		break;
            }
        }
		
		if( nomeDoMetodo != null )
			mensagem = nomeDoMetodo + " - " + mensagem;
		
		return mensagem;
	}
}
