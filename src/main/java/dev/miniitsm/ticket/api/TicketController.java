package dev.miniitsm.ticket.api;

import java.net.URI;
import java.util.UUID;

import dev.miniitsm.ticket.domain.TicketService;
import dev.miniitsm.ticket.domain.TicketView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
class TicketController {

    private final TicketService service;

    TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        TicketView created = service.create(request.title(), request.description());
        return ResponseEntity.created(URI.create("/api/tickets/" + created.id()))
                .body(TicketResponse.of(created));
    }

    @GetMapping("/{id}")
    TicketResponse get(@PathVariable UUID id) {
        return TicketResponse.of(service.get(id));
    }
}
