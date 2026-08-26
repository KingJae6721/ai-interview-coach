package com.aiinterview.resume.extract;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PdfBoxResumeTextExtractor implements ResumeTextExtractor {
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};
    private static final int MAX_EXTRACTED_TEXT_LENGTH = 50_000;
    private final long maxFileSizeBytes;

    public PdfBoxResumeTextExtractor(@Value("${resume.max-file-size-bytes:5242880}") long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public ExtractedResumeContent extract(MultipartFile file) {
        validateMetadata(file);
        try {
            byte[] bytes = file.getBytes();
            validateSignature(bytes);
            String text = extractText(bytes);
            if (!StringUtils.hasText(text)) {
                throw new BusinessException(ErrorCode.RESUME_CONTENT_NOT_FOUND);
            }
            String limitedText = text.length() > MAX_EXTRACTED_TEXT_LENGTH
                    ? text.substring(0, MAX_EXTRACTED_TEXT_LENGTH) : text;
            return new ExtractedResumeContent(limitedText, sha256(bytes));
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new BusinessException(ErrorCode.RESUME_TEXT_EXTRACTION_FAILED);
        }
    }

    private void validateMetadata(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.RESUME_INVALID_FILE);
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException(ErrorCode.RESUME_FILE_TOO_LARGE);
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new BusinessException(ErrorCode.RESUME_INVALID_FILE);
        }
    }

    private void validateSignature(byte[] bytes) {
        if (bytes.length < PDF_SIGNATURE.length) {
            throw new BusinessException(ErrorCode.RESUME_INVALID_FILE);
        }
        for (int index = 0; index < PDF_SIGNATURE.length; index++) {
            if (bytes[index] != PDF_SIGNATURE[index]) {
                throw new BusinessException(ErrorCode.RESUME_INVALID_FILE);
            }
        }
    }

    private String extractText(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document).trim();
        }
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
