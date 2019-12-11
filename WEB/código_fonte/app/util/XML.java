package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class XML {
	
	public void saveXML(Document documentXML, String path){
		XMLOutputter xout = new XMLOutputter();
		
		try {
			xout.setFormat(Format.getPrettyFormat().setIndent("\t").setEncoding("UTF-8"));
			//inserir url do arquvio;
			FileWriter file = new FileWriter(path);
			xout.output(documentXML, file);
			file.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}