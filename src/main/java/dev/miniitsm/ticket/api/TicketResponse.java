package dev.miniitsm.ticket.api;

import java.time.Instant;
import java.util.UUID;

import dev.miniitsm.ticket.application.TicketView;
import dev.miniitsm.ticket.domain.TicketStatus;

record TicketResponse(UUID id, String title, String description, TicketStatus status, Instant createdAt) {

    static TicketResponse of(TicketView view) {
        return new TicketResponse(view.id(), view.title(), view.description(), view.status(), view.createdAt());
    }
}
