package util.Query;

import java.util.List;


public class SubQuery {
	public String entity;
	public String column;
	public List<QueryFilter> filters;
	
	public SubQuery(String entity,String column,List<QueryFilter> filters){
		this.entity = entity;
		this.column = column;
		this.filters = filters;
	}
}
