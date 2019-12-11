package util.Query.DBs;

import java.util.HashMap;

import util.Query.DataType;
import util.Query.OperatorType;

public interface IDB {	
	
	public String castOperand(DataType dataType, String dataString, OperatorType opType);
}
