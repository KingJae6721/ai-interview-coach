package com.aiinterview.resume.extract;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfBoxResumeTextExtractorTest {
    private final PdfBoxResumeTextExtractor extractor = new PdfBoxResumeTextExtractor(1024 * 1024);

    @Test
    void extract_readsTextAndCreatesHash() throws Exception {
        MockMultipartFile file = pdfFile("resume.pdf", "Java Backend Engineer");
        assertThat(extractor.extract(file)).satisfies(result -> {
            assertThat(result.text()).contains("Java Backend Engineer");
            assertThat(result.sha256()).hasSize(64);
        });
    }

    @Test
    void extract_rejectsEmptyAndNonPdfFiles() {
        assertError(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]),
                ErrorCode.RESUME_INVALID_FILE);
        assertError(new MockMultipartFile("file", "fake.pdf", "application/pdf", "not pdf".getBytes()),
                ErrorCode.RESUME_INVALID_FILE);
        assertError(new MockMultipartFile("file", "text.txt", "text/plain", "%PDF-fake".getBytes()),
                ErrorCode.RESUME_INVALID_FILE);
    }

    @Test
    void extract_rejectsOversizedFile() {
        PdfBoxResumeTextExtractor smallExtractor = new PdfBoxResumeTextExtractor(4);
        assertThatThrownBy(() -> smallExtractor.extract(
                new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-".getBytes())))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESUME_FILE_TOO_LARGE));
    }

    @Test
    void extract_rejectsMalformedAndTextlessPdf() throws Exception {
        assertError(new MockMultipartFile("file", "broken.pdf", "application/pdf", "%PDF-broken".getBytes()),
                ErrorCode.RESUME_TEXT_EXTRACTION_FAILED);
        assertError(pdfFile("empty.pdf", null), ErrorCode.RESUME_CONTENT_NOT_FOUND);
    }

    private MockMultipartFile pdfFile(String name, String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            if (text != null) {
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(output);
            return new MockMultipartFile("file", name, "application/pdf", output.toByteArray());
        }
    }

    private void assertError(MockMultipartFile file, ErrorCode errorCode) {
        assertThatThrownBy(() -> extractor.extract(file))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
