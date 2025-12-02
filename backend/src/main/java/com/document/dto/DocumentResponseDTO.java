package com.document.dto;

import jakarta.validation.constraints.NotNull;

public class DocumentResponseDTO {
	@NotNull
    private String documentId;
	@NotNull
    private String uploadUrl;
    public DocumentResponseDTO() {}
    public DocumentResponseDTO(String documentId, String uploadUrl) {
        this.documentId = documentId;
        this.uploadUrl = uploadUrl;
    }
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public String getUploadUrl() {
        return uploadUrl;
    }
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
	@Override
	public String toString() {
		return "DocumentResponseDTO [documentId=" + documentId + ", uploadUrl=" + uploadUrl + "]";
	}
    
}
