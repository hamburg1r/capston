package com.document.dto;

public class DocumentUploadRequestDto {
	  private String fileName;
	    private String fileType;
	    private String fileSize;
	    public DocumentUploadRequestDto() {}
	    
	    public DocumentUploadRequestDto(String fileName, String fileType, String fileSize) {
			super();
			this.fileName = fileName;
			this.fileType = fileType;
			this.fileSize = fileSize;
		}

		public String getFileName() {
	        return fileName;
	    }
	    public void setFileName(String fileName) {
	        this.fileName = fileName;
	    }
	    public String getFileType() {
	        return fileType;
	    }
	    public void setFileType(String fileType) {
	        this.fileType = fileType;
	    }
	    public String getFileSize() {
	        return fileSize;
	    }
	    public void setFileSize(String fileSize) {
	        this.fileSize = fileSize;
	    }

}
