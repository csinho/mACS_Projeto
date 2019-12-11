package util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequerPerfil é utilizado para obrigar um método(action) ou classe(controller) a ter um dos perfils informados
 * 
 * @author thiagoaos
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresProfile {
    String[] value(); 
}
