package dev.miniitsm.ticket.application;

import java.time.Clock;
import java.util.UUID;

import dev.miniitsm.ticket.domain.Ticket;
import dev.miniitsm.ticket.domain.TicketNotFoundException;
import dev.miniitsm.ticket.persistence.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository repository;
    private final Clock clock;

    TicketService(TicketRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public TicketView create(String title, String description) {
        Ticket ticket = Ticket.create(UUID.randomUUID(), title, description, clock);
        return TicketView.of(repository.save(ticket));
    }

    @Transactional(readOnly = true)
    public TicketView get(UUID id) {
        return repository.findById(id)
                .map(TicketView::of)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }
}
