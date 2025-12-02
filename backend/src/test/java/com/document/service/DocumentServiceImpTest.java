

package com.document.service;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.document.exception.DocumentNotFoundException;
import com.document.model.DocumentModel;
import com.document.repository.DocumentRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImpTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private DocumentServiceImp documentService;

    private DocumentModel testDocument;
    private String testUserId;
    private String testDocumentId;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testDocumentId = "doc123";

        testDocument = new DocumentModel();
        testDocument.setDocumentId(testDocumentId);
        testDocument.setUserId(testUserId);
        testDocument.setFileName("test.pdf");
        testDocument.setFileType("application/pdf");
        testDocument.setFileSize("1024");
        testDocument.setStatus("UPLOADED");
        testDocument.setS3Key("user123/doc123/test.pdf");
    }

    @Test
    void createDocument_ShouldCreateNewDocument() {
        // Given
        String fileName = "test.pdf";
        String fileType = "application/pdf";
        String fileSize = "1024";

        // When
        DocumentModel result = documentService.createDocument(testUserId, fileName, fileType, fileSize);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getFileName()).isEqualTo(fileName);
        assertThat(result.getFileType()).isEqualTo(fileType);
        assertThat(result.getFileSize()).isEqualTo(fileSize);
        assertThat(result.getStatus()).isEqualTo("UPLOADED");
        assertThat(result.getDocumentId()).isNotNull();
        assertThat(result.getUploadDate()).isNotNull();

        verify(documentRepository, times(1)).save(any(DocumentModel.class));
    }

    @Test
    void updateDocument_ShouldCallRepositorySave() {
        // When
        documentService.updateDocument(testDocument);

        // Then
        verify(documentRepository, times(1)).save(testDocument);
    }

    @Test
    void getById_WhenDocumentExists_ShouldReturnDocument() {
        // Given
        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(testDocument);

        // When
        DocumentModel result = documentService.getById(testDocumentId, testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentId()).isEqualTo(testDocumentId);
        assertThat(result.getUserId()).isEqualTo(testUserId);

        verify(documentRepository, times(1)).findByDocumentIdAndUserId(testDocumentId, testUserId);
    }

    @Test
    void getById_WhenDocumentDoesNotExist_ShouldThrowException() {
        // Given
        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> documentService.getById(testDocumentId, testUserId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining(testDocumentId)
                .hasMessageContaining(testUserId);

        verify(documentRepository, times(1)).findByDocumentIdAndUserId(testDocumentId, testUserId);
    }

    @Test
    void getUserDocuments_ShouldReturnListOfDocuments() {
        // Given
        DocumentModel doc1 = new DocumentModel();
        doc1.setDocumentId("doc1");
        doc1.setUserId(testUserId);

        DocumentModel doc2 = new DocumentModel();
        doc2.setDocumentId("doc2");
        doc2.setUserId(testUserId);

        List<DocumentModel> expectedDocuments = Arrays.asList(doc1, doc2);

        when(documentRepository.findByUserId(testUserId)).thenReturn(expectedDocuments);

        // When
        List<DocumentModel> result = documentService.getUserDocuments(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(doc1, doc2);

        verify(documentRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    void markUploadCompleted_WhenDocumentExists_ShouldUpdateDocument() {
        // Given
        String fileName = "updated.pdf";
        String fileType = "application/pdf";
        String fileSize = "2048";

        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(testDocument);

        // When
        documentService.markUploadCompleted(testDocumentId, testUserId, fileName, fileType, fileSize);

        // Then
        assertThat(testDocument.getFileName()).isEqualTo(fileName);
        assertThat(testDocument.getFileType()).isEqualTo(fileType);
        assertThat(testDocument.getFileSize()).isEqualTo(fileSize);
        assertThat(testDocument.getStatus()).isEqualTo("COMPLETED");
        assertThat(testDocument.getS3Key()).contains(testUserId);
        assertThat(testDocument.getS3Key()).contains(testDocumentId);
        assertThat(testDocument.getS3Key()).contains(fileName);

        verify(documentRepository, times(1)).save(testDocument);
    }

    @Test
    void markUploadCompleted_WhenDocumentDoesNotExist_ShouldThrowException() {
        // Given
        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> documentService.markUploadCompleted(
                testDocumentId, testUserId, "test.pdf", "application/pdf", "1024"))
                .isInstanceOf(DocumentNotFoundException.class);

        verify(documentRepository, never()).save(any(DocumentModel.class));
    }

    @Test
    void generateDownloadUrl_WhenDocumentExists_ShouldReturnUrl() {
        // Given
        String expectedUrl = "https://s3.amazonaws.com/bucket/key?signature";
        testDocument.setS3Key("user123/doc123/test.pdf");

        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(testDocument);
        when(s3Service.generateDownloadUrl(testDocument.getS3Key()))
                .thenReturn(expectedUrl);

        // When
        String result = documentService.generateDownloadUrl(testDocumentId, testUserId);

        // Then
        assertThat(result).isEqualTo(expectedUrl);

        verify(documentRepository, times(1)).findByDocumentIdAndUserId(testDocumentId, testUserId);
        verify(s3Service, times(1)).generateDownloadUrl(testDocument.getS3Key());
    }

    @Test
    void generateDownloadUrl_WhenDocumentDoesNotExist_ShouldThrowException() {
        // Given
        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> documentService.generateDownloadUrl(testDocumentId, testUserId))
                .isInstanceOf(DocumentNotFoundException.class);

        verify(s3Service, never()).generateDownloadUrl(anyString());
    }

    @Test
    void deleteDocument_WhenDocumentExists_ShouldDeleteFromS3AndDB() {
        // Given
        testDocument.setS3Key("user123/doc123/test.pdf");

        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(testDocument);

        // When
        documentService.deleteDocument(testDocumentId, testUserId);

        // Then
        verify(s3Service, times(1)).deleteFile(testDocument.getS3Key());
        verify(documentRepository, times(1)).delete(testDocument);
    }

    @Test
    void deleteDocument_WhenDocumentHasNoS3Key_ShouldOnlyDeleteFromDB() {
        // Given
        testDocument.setS3Key(null);

        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(testDocument);

        // When
        documentService.deleteDocument(testDocumentId, testUserId);

        // Then
        verify(s3Service, never()).deleteFile(anyString());
        verify(documentRepository, times(1)).delete(testDocument);
    }

    @Test
    void deleteDocument_WhenDocumentDoesNotExist_ShouldThrowException() {
        // Given
        when(documentRepository.findByDocumentIdAndUserId(testDocumentId, testUserId))
                .thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> documentService.deleteDocument(testDocumentId, testUserId))
                .isInstanceOf(DocumentNotFoundException.class);

        verify(s3Service, never()).deleteFile(anyString());
        verify(documentRepository, never()).delete(any(DocumentModel.class));
    }
}