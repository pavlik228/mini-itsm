package dev.miniitsm.ticket.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class TicketTableConstraintsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private JdbcClient jdbc;

    @Test
    void insertWithUnknownStatusIsRejected() {
        assertThatThrownBy(() -> jdbc.sql("""
                        INSERT INTO ticket (id, title, status, created_at)
                        VALUES (:id, 'Bogus ticket', 'BOGUS', now())
                        """)
                .param("id", UUID.randomUUID())
                .update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
