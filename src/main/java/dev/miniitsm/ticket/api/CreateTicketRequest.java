package dev.miniitsm.ticket.api;

import jakarta.validation.constraints.NotNull;

// Только синтаксическая проверка; нормализация и длины — в Ticket.create.
record CreateTicketRequest(@NotNull String title, String description) {
}
