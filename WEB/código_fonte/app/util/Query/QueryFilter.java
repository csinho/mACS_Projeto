package util.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.dom.ThisExpression;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.mysql.jdbc.Util;
import com.sun.org.apache.xpath.internal.operations.And;
import com.sun.webkit.ThemeClient;
import com.sun.xml.internal.bind.v2.TODO;
import groovy.lang.Buildable;
import play.db.jpa.JPA;
import util.Query.*;
import util.Query.DBs.DBFactory;
import util.Query.DBs.IDB;


public class QueryFilter {
	
	private String column;
	private String dataType;
	private Object value;
	private String operator;
	private static final String defaultOrGroup = "default";	
	private String orGroup;
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String coluna) {
		this.column = coluna;
	}
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Object getValue(){		
		return buildFilterValue();
	}
	public void setValue(Object valor) {
		this.value = valor;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operador) {
		this.operator = operador;
	}
	
	public void setOperator(OperatorType operator) {
		this.operator = Operator.getOperator(operator);
	}
	
	public String getOrGroup() {
		return Strings.isNullOrEmpty(this.orGroup)? this.defaultOrGroup: this.orGroup;
	}
	public void setOrGroup(String orGroup) {
		this.orGroup = orGroup;
	}
	
	public QueryFilter() {
		
	}
	
	public QueryFilter(String column, String operator)
	{
		this.column = column;		
		this.operator = operator;			
	}
	
	public QueryFilter(String column, String value, String operator)
	{
		this(column, operator);		
		this.value = value;			
	}
	
	public QueryFilter(String column, Object value, String operator) {
		this(column, operator);		
		this.value = value;	
	}
	
	public QueryFilter(String column, Object value, OperatorType operatorType) {	
		this(column, Operator.getOperator(operatorType));		
		this.value = value;	
	}
	
	public QueryFilter(String column, OperatorType operatorType, Object value) {	
		this(column, Operator.getOperator(operatorType));		
		this.value = value;	
	}
	
	public QueryFilter(String column, Object value, String operator, String orGroup) {	
		this(column,value,operator);
		setOrGroup(orGroup);		
	}
	
	public QueryFilter(String column, Object value, OperatorType operatorType, String orGroup) {	
		this(column,value,Operator.getOperator(operatorType));
		setOrGroup(orGroup);		
	}
	
	public QueryFilter(String column, String value, String operator, String orGroup) {		
		this(column,value,operator);
		setOrGroup(orGroup);
	}
	
	private String buildFilter()
	{
		String operator = Operator.getDBOperator(getOperator());
		OperatorType opType = Operator.getOperatorType(operator);
		String placeHolder = buildRightCast("?");
		String column = buildLeftCast(this.getColumn());
		
		if(opType == OperatorType.CONTAIN) {
			return  " lower(" + column + ") " + operator + " " + placeHolder;
		} 		
		else {
			return column + " " + operator + " " + placeHolder + " ";
		}		
	}
	
	private Object buildFilterValue(){
		DataType dataType = Operator.getDataType(this.dataType);
		OperatorType operator = Operator.getOperatorType(this.operator);
		//at this moment, when building dinamic queries and specifying dataTypes, we only support INTEGER
		//queries made not specifying explicitly the datatype can use any type of data, including float, double etc..
		//TODO: find a better way to solve this
		if(dataType == DataType.NUMBER){
			 if (this.value instanceof Double) {
				 this.value = ((Double)this.value).longValue();
				 // this.value = ((Double)this.value).intValue();
			 }
		}
		
		if(this.value instanceof String || operator == operator.CONTAIN) {	
			this.value = this.value.toString();
			String operatorStr = Operator.getDBPlaceHolder(this.getOperator());			
			this.value = operatorStr.replaceFirst("\\?",Matcher.quoteReplacement(value.toString()));
		} 
		else if (this.value instanceof SubQuery) {
			this.value =  buildSubQuery((SubQuery)this.value);
		}	
		return this.value;
	}
	
	private String buildRightCast(String operandPlaceHolder){
		OperatorType operator = Operator.getOperatorType(this.operator);
		if(this.dataType == null || operator == operator.CONTAIN){
			return operandPlaceHolder;
		}		
		else{
			//when added new db support, get the the db name from a config file 
			IDB db =  DBFactory.getDb("postgres");
			DataType dataType = Operator.getDataType(this.dataType);
			return db.castOperand(dataType, operandPlaceHolder,operator);
		}			
	}
	
	private String buildLeftCast(String operand){
		OperatorType operator = Operator.getOperatorType(this.operator);
		if(this.dataType == null){
			return operand;
		}
		else {
			//when added new db support, get the the db name from a config file 
			IDB db =  DBFactory.getDb("postgres");
			DataType dataType = Operator.getDataType(this.dataType);
			if(operator == operator.CONTAIN){
				return db.castOperand(DataType.TEXT, operand,operator);
			}
			else{
				return db.castOperand(dataType, operand,operator);
			}
		}		
	}
		
	public static WhereStringAndValues keyValueInstance() {
		return new WhereStringAndValues();
	}
	
	public static WhereStringAndValues buildWhereWithValues(List<QueryFilter> filters)
    {
		WhereStringAndValues whereAndValues = keyValueInstance();
    	List<Object> where = new ArrayList<Object>();
    	List<Object> values = new ArrayList<Object>();
    	
    	where.add("1 = ?");
		values.add(1);		
		whereAndValues.where = Joiner.on(" and ").join(where);

		if(filters.size() > 0)
		{
			Boolean openedOrGroup = false;
			String lastOrGroup = QueryFilter.defaultOrGroup;
			for (QueryFilter filter : filters) {
				String filterBuilt = filter.buildFilter();
				where.add(filterBuilt);
				Object value = filter.getValue();
				
				//if the condition value is of the type WhereValues it indicates that it is a subquery
				//then we create the subquery sql string, add the subquery parameters to the list of the main query parameters
				if (value instanceof WhereStringAndValues) {
					WhereStringAndValues wv = ((WhereStringAndValues)value);
					whereAndValues.where += " and " + filter.getColumn() + " " + filter.getOperator() + " " +wv.where;
					for (Object object : wv.values) {
						values.add(object);
					}
				}
				else{
					//if its a like comparasion, we must convert to string and lower case
					if(Operator.getOperatorType(filter.operator) == OperatorType.CONTAIN){
						value = value.toString().toLowerCase();
					}	
					values.add(value);
					
					//set where conditions prameters
					WhereParameters wp = new WhereParameters();
					wp.openedOrGroup = openedOrGroup;//open or group determines if we are inside a sql or group
					wp.filter = filter; //the filter to be added
					wp.lastOrGroup = lastOrGroup; //the last orgroup used to determine 
					wp.whereItem = where; //the where already built					
					wp.whereAndValues = whereAndValues; //the current where string and values object
					
					//get the condition added result
					ConditionResult cResult = QueryFilter.addCondition(wp);				
					whereAndValues.where = cResult.where;//the where with the new condittion added			
					openedOrGroup = cResult.openedOrGroup;//the definition if we are inside an sql or group
				}
			}			
		}			
		
		whereAndValues.where = Strings.isNullOrEmpty(whereAndValues.where)? "": whereAndValues.where;		
		whereAndValues.values = values.toArray();		
		return whereAndValues;
    }
	
	private static ConditionResult addCondition(WhereParameters wp){
		ConditionResult cResult = new ConditionResult();
		if(wp.filter.getOrGroup() != wp.lastOrGroup)
		{					
			if(wp.openedOrGroup)
			{
				wp.whereAndValues.where = Joiner.on(" and ").join(wp.whereItem);
				wp.whereAndValues.where += ")";
			}
			else{						
				wp.whereAndValues.where = Joiner.on(" and ( ").join(wp.whereItem);
			}
			
			cResult.openedOrGroup = !wp.openedOrGroup;									
		}
		else
		{
			wp.whereAndValues.where = Joiner.on(" and ").join(wp.whereItem);				
		}
		cResult.where = wp.whereAndValues.where;
		return cResult;
	}
	
	public static String buidWhereString(List<QueryFilter> filters){
		WhereStringAndValues keyvalue = buildWhereWithValues(filters);
		String where = keyvalue.where;
		for(int i=0; i< keyvalue.values.length; i++){
			String valueStr =  keyvalue.values[i].toString();			
			where = where.replaceFirst("\\?",Matcher.quoteReplacement(valueStr) );
		}       
		return where;
	}		
	
	private WhereStringAndValues buildSubQuery(SubQuery subquery){
		WhereStringAndValues subConditions = QueryFilter.buildWhereWithValues(subquery.filters);
		subConditions.where = "(select "+subquery.column+" from "+subquery.entity+" where " +subConditions.where + " )";
		return subConditions;
	}

	
	
}