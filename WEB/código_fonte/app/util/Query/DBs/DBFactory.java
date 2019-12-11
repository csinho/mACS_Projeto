package util.Query.DBs;

import util.Query.DataType;

public class DBFactory {
	
	private DBFactory(){
			
	}
	public static IDB getDb(String dbName) {
		
		switch (dbName){
			case "postgres":
				return new PostgresPersistence();				
			default:
				//temporary - must be removed when we have other db types suppported
				return new PostgresPersistence();
		}
	}
}
