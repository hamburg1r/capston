package com.document.dto;

public class DocumentUploadRequestDto {
	  private String fileName;
	    private String fileType;
	    private long fileSize;
	    public DocumentUploadRequestDto() {}
	    
	    public DocumentUploadRequestDto(String fileName, String fileType, long fileSize) {
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
	    public long getFileSize() {
	        return fileSize;
	    }
	    public void setFileSize(long fileSize) {
	        this.fileSize = fileSize;
	    }

}
