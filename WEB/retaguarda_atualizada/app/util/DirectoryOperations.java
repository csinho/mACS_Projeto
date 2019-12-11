package util;

import java.io.File;

public class DirectoryOperations {
	
	public boolean delete(File dir){
		if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) { 
               boolean success = delete(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
	}

	public boolean isEmpty(File dir){
		if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children.length == 0){
            	return true;
            }
		}
		return false;
	}
	
}
