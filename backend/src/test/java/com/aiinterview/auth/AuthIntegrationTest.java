package com.aiinterview.auth;

import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Testcontainers
@SpringBootTest
class AuthIntegrationTest {

        private static final String EMAIL = "auth-test@example.com";
        private static final String PASSWORD = "Password123!";
        private static final String JWT_SECRET = "c3ByaW5nLWJvb3QtYWktaW50ZXJ2aWV3LWNvYWNoLXBvcnRmb2xpby1zZWNyZXQta2V5LTMyei1ieXRlcy1sZW5ndGg=";

        @Container
        static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                        .withDatabaseName("ai_interview")
                        .withUsername("postgres")
                        .withPassword("postgres");

        @Container
        static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                        .withExposedPorts(6379);

        @DynamicPropertySource
        static void configureProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
                registry.add("spring.datasource.username", POSTGRES::getUsername);
                registry.add("spring.datasource.password", POSTGRES::getPassword);
                registry.add("spring.data.redis.host", REDIS::getHost);
                registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        }

        private MockMvc mockMvc;
        @Autowired
        private WebApplicationContext webApplicationContext;
        private final ObjectMapper objectMapper = new ObjectMapper();
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private RefreshTokenRepository refreshTokenRepository;
        @Autowired
        private BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
        @Autowired
        private StringRedisTemplate redisTemplate;

        @BeforeEach
        void setUpMockMvc() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();
        }

        @AfterEach
        void tearDown() {
                redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
                userRepository.deleteAll();
        }

        @Test
        void authenticationFlow_loginMeRtrLogoutBlacklistAndInvalidTokens() throws Exception {
                User user = userRepository.save(User.builder()
                                .email(EMAIL)
                                .password(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                                                .encode(PASSWORD))
                                .nickname("auth-test").role(UserRole.USER).authProvider(AuthProvider.LOCAL)
                                .status(UserStatus.ACTIVE).build());

                Tokens loginTokens = login();
                assertThat(refreshTokenRepository.findById(user.getId())).hasValueSatisfying(
                                token -> assertThat(token.getToken()).isEqualTo(loginTokens.refreshToken()));

                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + loginTokens.accessToken()))
                                .andExpect(status().isOk());

                Tokens rotatedTokens = reissue(loginTokens.refreshToken());
                assertThat(rotatedTokens.accessToken()).isNotEqualTo(loginTokens.accessToken());
                assertThat(rotatedTokens.refreshToken()).isNotEqualTo(loginTokens.refreshToken());
                assertThat(refreshTokenRepository.findById(user.getId())).hasValueSatisfying(
                                token -> assertThat(token.getToken()).isEqualTo(rotatedTokens.refreshToken()));

                mockMvc.perform(post("/api/v1/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + loginTokens.refreshToken() + "\"}"))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(post("/api/v1/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"invalid\"}"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/api/v1/auth/logout").header("Authorization",
                                "Bearer " + rotatedTokens.accessToken()))
                                .andExpect(status().isOk());
                assertThat(refreshTokenRepository.findById(user.getId())).isEmpty();
                assertThat(blacklistedAccessTokenRepository.existsById(rotatedTokens.accessToken())).isTrue();

                mockMvc.perform(get("/api/v1/users/me").header("Authorization",
                                "Bearer " + rotatedTokens.accessToken()))
                                .andExpect(status().isForbidden());

                String expiredToken = new JwtProvider(JWT_SECRET, 1L, 1209600000L).createAccessToken(user.getId(),
                                UserRole.USER);
                Thread.sleep(10);
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + expiredToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void signup_deserializesJsonRequest() throws Exception {
                String email = "signup-request-test@example.com";

                mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"" + email
                                                + "\",\"password\":\"Password123!\",\"nickname\":\"signup-test\"}"))
                                .andExpect(status().isCreated());

                assertThat(userRepository.findByEmail(email)).isPresent();
        }

        private Tokens login() throws Exception {
                String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
                return tokens(body);
        }

        private Tokens reissue(String refreshToken) throws Exception {
                String body = mockMvc.perform(post("/api/v1/auth/reissue").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
                return tokens(body);
        }

        private Tokens tokens(String body) throws Exception {
                JsonNode data = objectMapper.readTree(body).path("data");
                return new Tokens(data.path("accessToken").asText(), data.path("refreshToken").asText());
        }

        private record Tokens(String accessToken, String refreshToken) {
        }
}
