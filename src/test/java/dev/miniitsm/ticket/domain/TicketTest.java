package dev.miniitsm.ticket.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TicketTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-10-14T10:15:30.123456789Z"), ZoneOffset.UTC);

    private final UUID id = UUID.randomUUID();

    @Test
    void createAssignsGivenIdStatusNewAndCreatedAtFromClockTruncatedToMicros() {
        Ticket ticket = Ticket.create(id, "Printer is broken", "Paper jam", CLOCK);

        assertThat(ticket.getId()).isEqualTo(id);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
        assertThat(ticket.getCreatedAt()).isEqualTo(Instant.parse("2026-10-14T10:15:30.123456Z"));
        assertThat(ticket.getDescription()).isEqualTo("Paper jam");
    }

    @Test
    void createStripsTitle() {
        Ticket ticket = Ticket.create(id, "  Printer is broken \t", null, CLOCK);

        assertThat(ticket.getTitle()).isEqualTo("Printer is broken");
    }

    @Test
    void createAcceptsTitleOf200CharsAfterStrip() {
        String title = "a".repeat(200);

        Ticket ticket = Ticket.create(id, "  " + title + "  ", null, CLOCK);

        assertThat(ticket.getTitle()).isEqualTo(title);
    }

    @Test
    void createRejectsBlankTitle() {
        assertThatThrownBy(() -> Ticket.create(id, "   ", null, CLOCK))
                .isInstanceOfSatisfying(TicketValidationException.class,
                        e -> assertThat(e.field()).isEqualTo("title"));
    }

    @Test
    void createRejectsNullTitle() {
        assertThatThrownBy(() -> Ticket.create(id, null, null, CLOCK))
                .isInstanceOfSatisfying(TicketValidationException.class,
                        e -> assertThat(e.field()).isEqualTo("title"));
    }

    @Test
    void createRejectsTitleLongerThan200CharsAfterStrip() {
        assertThatThrownBy(() -> Ticket.create(id, " " + "a".repeat(201) + " ", null, CLOCK))
                .isInstanceOfSatisfying(TicketValidationException.class,
                        e -> assertThat(e.field()).isEqualTo("title"));
    }

    @Test
    void createTurnsBlankDescriptionIntoNull() {
        assertThat(Ticket.create(id, "Title", "   ", CLOCK).getDescription()).isNull();
        assertThat(Ticket.create(id, "Title", "", CLOCK).getDescription()).isNull();
    }

    @Test
    void createDoesNotStripDescription() {
        Ticket ticket = Ticket.create(id, "Title", "  details  ", CLOCK);

        assertThat(ticket.getDescription()).isEqualTo("  details  ");
    }

    @Test
    void createAcceptsDescriptionOf4000Chars() {
        String description = "d".repeat(4000);

        assertThat(Ticket.create(id, "Title", description, CLOCK).getDescription()).isEqualTo(description);
    }

    @Test
    void createRejectsDescriptionLongerThan4000Chars() {
        assertThatThrownBy(() -> Ticket.create(id, "Title", "d".repeat(4001), CLOCK))
                .isInstanceOfSatisfying(TicketValidationException.class,
                        e -> assertThat(e.field()).isEqualTo("description"));
    }
}
