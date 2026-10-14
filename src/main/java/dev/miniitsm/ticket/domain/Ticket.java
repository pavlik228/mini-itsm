package dev.miniitsm.ticket.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "ticket")
public class Ticket implements Persistable<UUID> {

    static final int TITLE_MAX_LENGTH = 200;
    static final int DESCRIPTION_MAX_LENGTH = 4000;

    @Id
    private UUID id;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // id задаёт приложение, поэтому новизну сущности Spring Data узнаёт отсюда, а не по id == null.
    @Transient
    private boolean isNew = true;

    protected Ticket() {
    }

    private Ticket(UUID id, String title, String description, TicketStatus status, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Ticket create(UUID id, String title, String description, Clock clock) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(clock, "clock");
        return new Ticket(
                id,
                normalizeTitle(title),
                normalizeDescription(description),
                TicketStatus.NEW,
                // TIMESTAMPTZ хранит микросекунды: без обрезки ответ POST и последующий GET разойдутся.
                Instant.now(clock).truncatedTo(ChronoUnit.MICROS));
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new TicketValidationException("title", "must not be blank");
        }
        String stripped = title.strip();
        if (stripped.length() > TITLE_MAX_LENGTH) {
            throw new TicketValidationException("title",
                    "length must be at most " + TITLE_MAX_LENGTH + " characters");
        }
        return stripped;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new TicketValidationException("description",
                    "length must be at most " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return description;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
