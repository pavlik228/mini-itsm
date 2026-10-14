package dev.miniitsm.ticket.domain;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {

    private final UUID id;

    public TicketNotFoundException(UUID id) {
        super("Ticket " + id + " not found");
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}
