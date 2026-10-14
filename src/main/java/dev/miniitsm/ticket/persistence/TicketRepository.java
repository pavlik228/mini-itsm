package dev.miniitsm.ticket.persistence;

import java.util.UUID;

import dev.miniitsm.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
