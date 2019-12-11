import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.Md5Crypt;

public class Test {

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        StringBuffer sb = new StringBuffer();
        String original = "macsfe2bdf97181aaa43mobile";
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(original.getBytes());
        byte[] digest = md.digest();
        
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        System.out.println ( sb );

    }

}
