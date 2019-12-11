package util;

import flexjson.JSONSerializer;

public class SerializeUtil {
  
  public static JSONSerializer serializerGeral(){
    
    return new JSONSerializer()
    .exclude("*.class","*.entityId","*.persistent")
    .include("*");
    
  }
  
  public static JSONSerializer serializerExcludeGeral(){
    
    return new JSONSerializer()
    .exclude("*.class","*.entityId","*.persistent");
    
  }
  
}
