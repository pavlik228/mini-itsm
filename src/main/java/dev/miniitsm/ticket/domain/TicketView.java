package dev.miniitsm.ticket.domain;

import java.time.Instant;
import java.util.UUID;

public record TicketView(UUID id, String title, String description, TicketStatus status, Instant createdAt) {

    static TicketView of(Ticket ticket) {
        return new TicketView(ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getStatus(), ticket.getCreatedAt());
    }
}
