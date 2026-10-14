# 0002. Persistence: Spring Data JPA и Flyway

- Статус: Accepted
- Дата: 2026-10-14

## Контекст

Сначала был выбран Spring Data JDBC (7a55c52), затем заменён на JPA (1c03350, 87b5ade): у разработчика есть опыт с Hibernate.

## Решение

- Spring Data JPA (Hibernate). Версии берутся из BOM Spring Boot 4.1.1.
- Ticket — `@Entity` и реализует `Persistable<UUID>`.
- id генерирует application (UUID).
- Схема только через Flyway (`ddl-auto=validate`); `open-in-view=false`.
- V1 содержит `CHECK (status IN ('NEW'))` = `ticket_status_check`; новые статусы — только новой миграцией.

## Рассмотренные варианты

- **Spring Data JDBC** — отвергнут: у разработчика есть опыт с Hibernate.
- **id из БД** — отвергнут: сущность остаётся без id до save.
- **Без Persistable** — отвергнут: save() с заданным id делает merge с лишним SELECT.

## Последствия

- \+ Знакомый стек, меньше риска на пилоте.
- \+ `ddl-auto=validate` ловит расхождение схемы и сущности при старте.
- − Сложность JPA (Persistable, persist vs merge).
- V1 изменён до merge (добавлен CHECK — f925972, тест на CHECK — 5140e28) — локальную БД пересоздали из-за checksum Flyway. Правило «применённые миграции не менять» действует для всего, что попало в main.

## Ссылки

- 7a55c52 docs: slice 001 uses Spring Data JDBC
- 1c03350 docs: context pack 001 — JPA, Boot 4, field rules, checkpoints
- 87b5ade docs: context pack 001 — JPA, Boot 4, field rules, checkpoints
- 5fd7c03 feat: flyway V1 ticket table
- 370b120 feat: ticket domain and JPA repository
- 5140e28 test: archunit rules and status check constraint
- f925972 refactor: break domain-persistence cycle, harden local config
