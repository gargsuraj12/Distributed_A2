package common;

import java.io.Serializable;

public class FileDetails implements Serializable{
	private static final long serialVersionUID = 1L;
	private String fileName;
	private String filePath;
	
	public FileDetails() {
		super();
	}
	
	public FileDetails(String fileName, String filePath) {
		super();
		this.fileName = fileName;
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
}
