package util.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import util.Pair;


public class Operator {
		
		
	public static DataType getDataType(String key) {
		return Extension.getEnumFromString(DataType.class, key);
    }	
	
	public static String getDBPlaceHolder(String opStr){
		OperatorType opType = Operator.getOperatorType(opStr);
		if(opType != null){
			return Operator.placeHolderMap().get(Operator.operatorMap().get(opType));
		}
		else{			
			return Operator.placeHolderMap().get(opStr);
		}		
	}
	
	public static String getDBOperator(String opStr){
		if(Operator.placeHolderMap().get(opStr) != null){
			return opStr;
		}
		else{
			OperatorType opType = Operator.getOperatorType(opStr);			
			return Operator.operatorMap().get(opType);
		}		
	}
	
	public static OperatorType getOperatorType(String opType) {
		OperatorType operatorType = Extension.getEnumFromString(OperatorType.class, opType);
		if(operatorType == null){
			HashMap<OperatorType, String> map = operatorMap();
			for (Entry<OperatorType, String> entry : map.entrySet()) {
				String opValue = entry.getValue();   
			    if(opValue == opType){
			    	operatorType = entry.getKey();
			    	break;
			    }
			}
		}		
		return operatorType;
    }	
	
	public static List<Pair> getDataTypeOperators(String dtTypeStr){	
		DataType dtType = getDataType(dtTypeStr);
		List<Pair> operList = new ArrayList<Pair>();
		HashMap<DataType,ArrayList<OperatorType>> opDataTypes = DataTypeOperators();
		for (Entry<DataType, ArrayList<OperatorType>> entry : opDataTypes.entrySet()) {
			DataType key = entry.getKey();		   
		    if(key == dtType){		    	
		    	for (OperatorType OperatorType : entry.getValue()) {		    		
		    		operList.add(new Pair<>(OperatorType, operatorName().get(OperatorType)));
		    	}		    	
		    	break;
		    }
		}
		return operList;
	}
	
	public static String getOperator(OperatorType type) {
		String op = null;
		HashMap<OperatorType, String> map = operatorMap();
		for (Entry<OperatorType, String> entry : map.entrySet()) {
			OperatorType key = entry.getKey();		   
		    if(key == type){
		    	op = entry.getValue();
		    	break;
		    }
		}
		return op;
	}
	
	
	private static HashMap<String, String> placeHolderMap(){
		HashMap op = new HashMap<String,String>();
		op.put( "like", "%?%" );
		op.put( "=", "?" );
		op.put( ">", "?" );
		op.put( "<", "?" );
		op.put( ">=", "?" );
		op.put( "<=", "?" );
		op.put( "<>", "?" );
		op.put( "in", "?" );
		return op;
	}
	
	private static HashMap<OperatorType, String> operatorMap(){
		HashMap opType = new HashMap<String,String>();
		opType.put(OperatorType.CONTAIN, "like" );
		opType.put(OperatorType.EQUAL, "=" );
		opType.put( OperatorType.GREATER, ">" );
		opType.put(OperatorType.LESS, "<" );
		opType.put( OperatorType.GREATER_OR_EQUAL, ">=" );
		opType.put(OperatorType.LESS_OR_EQUAL, "<=" );
		opType.put(OperatorType.DIFERENT, "<>" );
		opType.put(OperatorType.IN, "in" );		
		
		return opType;
	}
	
	
	private static HashMap<DataType,ArrayList<OperatorType>> DataTypeOperators(){
		
		HashMap dtTypeOperators = new HashMap<DataType,ArrayList<OperatorType>>();	
		
		dtTypeOperators.put(DataType.NUMBER, new ArrayList<OperatorType>() {
			{
				add(OperatorType.CONTAIN);
				add(OperatorType.EQUAL);
				add(OperatorType.GREATER);
				add(OperatorType.LESS);
				add(OperatorType.GREATER_OR_EQUAL);
				add(OperatorType.LESS_OR_EQUAL);
				add(OperatorType.DIFERENT);
			}
		});		
		dtTypeOperators.put(DataType.DATE, new ArrayList<OperatorType>() {
			{
				add(OperatorType.EQUAL);
				add(OperatorType.GREATER);
				add(OperatorType.LESS);
				add(OperatorType.GREATER_OR_EQUAL);
				add(OperatorType.LESS_OR_EQUAL);
				add(OperatorType.DIFERENT);
			}
		});	
		dtTypeOperators.put(DataType.TEXT, new ArrayList<OperatorType>() {
			{
				add(OperatorType.CONTAIN);
				add(OperatorType.DIFERENT);
				add(OperatorType.EQUAL);
			}
		});	
        dtTypeOperators.put(DataType.TEL, new ArrayList<OperatorType>() {
			{
				add(OperatorType.CONTAIN);
				add(OperatorType.DIFERENT);
				add(OperatorType.EQUAL);
			}
		});	
		dtTypeOperators.put(DataType.MONTH, new ArrayList<OperatorType>() {
			{				
				add(OperatorType.EQUAL);
				add(OperatorType.GREATER);
				add(OperatorType.LESS);
				add(OperatorType.GREATER_OR_EQUAL);
				add(OperatorType.LESS_OR_EQUAL);
				add(OperatorType.DIFERENT);
			}
		});	
		
		dtTypeOperators.put(DataType.BOOLEAN, new ArrayList<OperatorType>() {
			{				
				add(OperatorType.EQUAL);							
			}
		});	
		
		dtTypeOperators.put(DataType.LATLONG, new ArrayList<OperatorType>() {
			{				
				add(OperatorType.EQUAL);
				add(OperatorType.CONTAIN);
				add(OperatorType.DIFERENT);
			}
		});
			
		return dtTypeOperators;
	}	
	
	
	public static HashMap<OperatorType, String> operatorName(){
		HashMap opType = new HashMap<String,String>();
		opType.put(OperatorType.CONTAIN, "Contém" );
		opType.put(OperatorType.EQUAL, "Igual" );
		opType.put( OperatorType.GREATER, "Maior" );
		opType.put(OperatorType.LESS, "Menor" );
		opType.put( OperatorType.GREATER_OR_EQUAL, "Maior ou igual" );
		opType.put(OperatorType.LESS_OR_EQUAL, "Menor ou igual" );
		opType.put(OperatorType.DIFERENT, "Diferente" );
		return opType;
	}	
}
