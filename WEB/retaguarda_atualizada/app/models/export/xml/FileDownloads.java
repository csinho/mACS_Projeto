package models.export.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileDownloads {
	
	public InputStream prepararArquivo(File fileTmp){
		
		InputStream file = null;
		if (fileTmp.exists()) {
			byte[] array;
			try {
				array = Files.readAllBytes(fileTmp.toPath());
				file = new ByteArrayInputStream(array);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
}
