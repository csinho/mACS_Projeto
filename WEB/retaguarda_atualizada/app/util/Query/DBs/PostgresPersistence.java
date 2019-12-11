package util.Query.DBs;

import java.util.HashMap;

import util.Query.*;

public class PostgresPersistence implements IDB {	
	
	private HashMap<DataType, String> dataTypeCast(){		
		HashMap castTypes = new HashMap<String,String>();
		castTypes.put(DataType.NUMBER, "bigint" );
		castTypes.put(DataType.DATE, "date" );
		castTypes.put(DataType.TEXT, "varchar" );
        castTypes.put(DataType.TEL, "varchar" );
		castTypes.put(DataType.MONTH, "date" );
		castTypes.put(DataType.BOOLEAN, "boolean" );
		
		return castTypes;
	}

	@Override
	public String castOperand(DataType dataType, String operand, OperatorType opType) {	
		if(this.dataTypeCast().get(dataType) != null){
			operand = " cast(" + operand + " as " + this.dataTypeCast().get(dataType) + ")";
		}		
		return operand;		
	}
	
}
