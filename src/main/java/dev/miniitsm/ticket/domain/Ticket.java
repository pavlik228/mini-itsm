package dev.miniitsm.ticket.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

// Заглушка до реализации слайса 001: тесты компилируются и падают.
public class Ticket {

    public static Ticket create(UUID id, String title, String description, Clock clock) {
        throw new UnsupportedOperationException("not implemented");
    }

    public UUID getId() {
        throw new UnsupportedOperationException("not implemented");
    }

    public String getTitle() {
        throw new UnsupportedOperationException("not implemented");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("not implemented");
    }

    public TicketStatus getStatus() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Instant getCreatedAt() {
        throw new UnsupportedOperationException("not implemented");
    }
}
