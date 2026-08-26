package com.aiinterview.resume;

import com.aiinterview.ai.dto.ResumeAnalysisResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.auth.JwtProvider;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.resume.extract.ExtractedResumeContent;
import com.aiinterview.resume.extract.ResumeTextExtractor;
import com.aiinterview.resume.repository.ResumeAnalysisRepository;
import com.aiinterview.resume.repository.ResumeRepository;
import com.aiinterview.user.entity.*;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ResumeIntegrationTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ai_interview").withUsername("postgres").withPassword("postgres");
    @Container static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired ResumeRepository resumeRepository;
    @Autowired ResumeAnalysisRepository resumeAnalysisRepository;
    @MockitoBean ResumeTextExtractor resumeTextExtractor;
    @MockitoBean AiService aiService;
    MockMvc mockMvc;
    User user;
    String token;

    @BeforeEach void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        user = userRepository.save(User.builder().email("resume@example.com").password("password").nickname("resume")
                .role(UserRole.USER).authProvider(AuthProvider.LOCAL).status(UserStatus.ACTIVE).build());
        token = jwtProvider.createAccessToken(user.getId(), user.getRole());
        given(resumeTextExtractor.extract(any())).willReturn(
                new ExtractedResumeContent("secret@example.com Java resume", "a".repeat(64)));
        given(aiService.analyzeResume(any())).willReturn(result());
    }

    @AfterEach void tearDown() {
        resumeAnalysisRepository.deleteAll();
        resumeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test void upload_savesOwnedResumeAndAnalysis_andListsSummary() throws Exception {
        mockMvc.perform(multipart("/api/v1/resumes/analyze").file(file()).header("Authorization", bearer()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.skills[0]").value("Java"));
        assertThat(resumeRepository.findAll()).singleElement()
                .satisfies(resume -> assertThat(resume.getUser().getId()).isEqualTo(user.getId()));
        assertThat(resumeAnalysisRepository.count()).isOne();
        mockMvc.perform(get("/api/v1/resumes").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].originalFileName").value("resume.pdf"));
    }

    @Test void upload_doesNotPersistWhenExtractionOrAiFails(CapturedOutput output) throws Exception {
        given(aiService.analyzeResume(any())).willThrow(new BusinessException(ErrorCode.AI_REQUEST_FAILED));
        mockMvc.perform(multipart("/api/v1/resumes/analyze").file(file()).header("Authorization", bearer()))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value("AI_REQUEST_FAILED"));
        assertThat(resumeRepository.count()).isZero();
        assertThat(output.getAll()).doesNotContain("secret@example.com Java resume");
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "../resume.pdf", "application/pdf", "%PDF-test".getBytes());
    }
    private String bearer() { return "Bearer " + token; }
    private ResumeAnalysisResult result() {
        return ResumeAnalysisResult.builder().summary("Backend engineer").skills(List.of("Java"))
                .workExperiences(List.of()).projects(List.of("API project")).education(List.of())
                .certifications(List.of()).achievements(List.of("30% improvement"))
                .strengths(List.of("Problem solving")).keywords(List.of("backend")).aiModel("test-model").build();
    }
}
