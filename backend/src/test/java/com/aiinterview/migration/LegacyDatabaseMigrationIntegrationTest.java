package com.aiinterview.migration;

import com.aiinterview.BackendApplication;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class LegacyDatabaseMigrationIntegrationTest {

    private static final String MARKER_EMAIL = "legacy-marker@example.test";
    private static final String JWT_SECRET =
            "c3ByaW5nLWJvb3QtYWktaW50ZXJ2aWV3LWNvYWNoLW1pZ3JhdGlvbi10ZXN0LXNlY3JldA==";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ai_interview")
            .withUsername("test")
            .withPassword("test");

    @Test
    void legacyV1SchemaBaselinesAtVersionOneAppliesV2AndPassesHibernateValidation() throws Exception {
        DataSource dataSource = dataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/migration/V1__initial_schema.sql"));
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                insert into users (
                    created_at, updated_at, auth_provider, email, nickname,
                    password, role, status
                ) values (?, ?, 'LOCAL', ?, 'legacy-marker', 'encoded', 'USER', 'ACTIVE')
                """, now, now, MARKER_EMAIL);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1"))
                .load()
                .migrate();

        assertThat(jdbcTemplate.queryForList("""
                select version || ':' || type || ':' || success
                from flyway_schema_history
                order by installed_rank
                """, String.class))
                .containsExactly("1:BASELINE:true", "2:SQL:true");
        assertThat(jdbcTemplate.queryForObject(
                "select to_regclass('public.admin_audit_logs')::text", String.class))
                .isEqualTo("admin_audit_logs");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where email = ?", Integer.class, MARKER_EMAIL))
                .isEqualTo(1);

        try (ConfigurableApplicationContext ignored = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--server.port=0",
                        "--spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                        "--spring.datasource.username=" + POSTGRES.getUsername(),
                        "--spring.datasource.password=" + POSTGRES.getPassword(),
                        "--spring.flyway.enabled=true",
                        "--spring.flyway.baseline-on-migrate=false",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--jwt.secret=" + JWT_SECRET,
                        "--springdoc.api-docs.enabled=false",
                        "--springdoc.swagger-ui.enabled=false")) {
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from users where email = ?", Integer.class, MARKER_EMAIL))
                    .isEqualTo(1);
        }
    }

    private DataSource dataSource() {
        return new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
    }
}
