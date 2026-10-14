package dev.miniitsm.ticket.application;

import java.time.Instant;
import java.util.UUID;

import dev.miniitsm.ticket.domain.Ticket;
import dev.miniitsm.ticket.domain.TicketStatus;

public record TicketView(UUID id, String title, String description, TicketStatus status, Instant createdAt) {

    static TicketView of(Ticket ticket) {
        return new TicketView(ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getStatus(), ticket.getCreatedAt());
    }
}
