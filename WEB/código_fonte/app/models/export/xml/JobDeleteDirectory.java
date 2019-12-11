package models.export.xml;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Play;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import sun.text.resources.cldr.FormatData;
import util.DirectoryOperations;
import util.EnumUrlDirectory;
import util.Format;

@OnApplicationStart(async=true)
@On("0 43 03 * * ?")

public class JobDeleteDirectory extends Job{
	
	public void doJob() {
		File xmlSubDir = new File(EnumUrlDirectory.XML.getUrl());
        File zipSubDir = new File(EnumUrlDirectory.ZIP.getUrl());
        
        if (xmlSubDir.exists()){
        	deleteDirectory(xmlSubDir); 
        }
        
        if (zipSubDir.exists()){
        	deleteDirectory(zipSubDir);
        }
        
	}
	
	private void deleteDirectory(File dir){
		DirectoryOperations directoryOperations = new DirectoryOperations();
		if (!directoryOperations.isEmpty(dir)){
			String[] children = dir.list();
	        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
	        
	        Date dataAtual = null;
	        try {
				dataAtual = formatDate.parse(formatDate.format(new Date()));
	        } catch (ParseException e) {
				e.printStackTrace();
			}
	        
	        for (String name : children){
	        	File d = new File(dir.getPath() + File.separator + name);
	        	try {
	        		Date dateTemp = new Date();
	        		dateTemp.setTime(d.lastModified());
					Date dateDir = formatDate.parse(formatDate.format(dateTemp));
					if (dataAtual.getTime() != dateDir.getTime()){
						directoryOperations.delete(d);
					}
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
	        }
		}
	}
}
