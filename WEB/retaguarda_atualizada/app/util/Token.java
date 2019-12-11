package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Token {
    
    public static boolean isValid(String dispositivoId, String token) throws NoSuchAlgorithmException {
        StringBuffer sb = new StringBuffer();
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        md.update(("macs" + dispositivoId + "mobile").getBytes());
        
        byte[] digest = md.digest();
        
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return (sb.toString().equals(token));
    }
    
}
