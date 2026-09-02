package com.aiinterview.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class NormalizedNameMigrationIntegrationTest {

    private static final String MIGRATION = "/db/manual/V20260902__normalized_name_backfill.sql";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("migration_test").withUsername("postgres").withPassword("postgres");

    @BeforeEach
    void setUpSchema() throws SQLException {
        try (Connection connection = POSTGRES.createConnection("");
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists job_positions");
            statement.execute("drop table if exists companies");
            statement.execute("create table companies (id bigint primary key, name varchar(100) not null, "
                    + "normalized_name varchar(100))");
            statement.execute("create table job_positions (id bigint primary key, company_id bigint not null "
                    + "references companies(id), name varchar(100) not null, normalized_name varchar(100))");
        }
    }

    @Test
    void migration_backfillsNullTargetsAndAppliesFinalConstraints() throws Exception {
        execute("insert into companies(id, name) values (1, 'Ｏｐｅｎ   ＡＩ')");
        execute("insert into job_positions(id, company_id, name) values (1, 1, ' Backend   Developer ')");

        assertThat(queryLong("select count(*) from companies where normalized_name is null")).isOne();
        assertThat(queryLong("select count(*) from job_positions where normalized_name is null")).isOne();

        executeMigration();

        assertThat(queryString("select normalized_name from companies where id = 1")).isEqualTo("open ai");
        assertThat(queryString("select normalized_name from job_positions where id = 1"))
                .isEqualTo("backend developer");
        assertThat(queryLong("select count(*) from information_schema.columns where table_name = 'companies' "
                + "and column_name = 'normalized_name' and is_nullable = 'NO'"))
                .isOne();
        assertThat(queryLong("select count(*) from information_schema.columns where table_name = 'job_positions' "
                + "and column_name = 'normalized_name' and is_nullable = 'NO'"))
                .isOne();
    }

    @Test
    void migration_rejectsCompanyNormalizationCollision() throws Exception {
        execute("insert into companies(id, name) values (1, 'Open AI'), (2, 'Ｏｐｅｎ   ＡＩ')");

        assertThatThrownBy(this::executeMigration)
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Company normalized_name collision detected");
    }

    @Test
    void migration_rejectsJobPositionNormalizationCollisionWithinCompany() throws Exception {
        execute("insert into companies(id, name) values (1, 'Example')");
        execute("insert into job_positions(id, company_id, name) values "
                + "(1, 1, 'Backend Developer'), (2, 1, ' backend   developer ')");

        assertThatThrownBy(this::executeMigration)
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("JobPosition normalized_name collision detected");
    }

    private void executeMigration() throws Exception {
        try (var input = getClass().getResourceAsStream(MIGRATION)) {
            assertThat(input).isNotNull();
            execute(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = POSTGRES.createConnection("");
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private long queryLong(String sql) throws SQLException {
        try (Connection connection = POSTGRES.createConnection("");
             var statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private String queryString(String sql) throws SQLException {
        try (Connection connection = POSTGRES.createConnection("");
             var statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
