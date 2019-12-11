package util;

import java.io.File;

import play.Play;

public enum EnumUrlDirectory {
	XML(Play.tmpDir + File.separator + "fileExport" + File.separator + "xml"),
	ZIP(Play.tmpDir + File.separator + "fileExport" + File.separator + "zip"),
	ROUTE_DOWNLOADS(File.separator + "forms" + File.separator + "downloads" + File.separator);
	
	String url;
	private EnumUrlDirectory(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
}
