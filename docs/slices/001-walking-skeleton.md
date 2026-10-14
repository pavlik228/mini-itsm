# Слайс 001 — ходячий скелет: отчёт

Context pack: [docs/context-packs/001-walking-skeleton.md](../context-packs/001-walking-skeleton.md). Merge: bca1b50 (PR #1).

## Цель

Минимальный сквозной путь: HTTP → домен → PostgreSQL → HTTP, собирается и тестируется в CI. Функциональности ITSM (статусы, SLA, приоритет) в слайсе нет.

## Что сделано (по DoD)

| Пункт DoD | Итог |
|---|---|
| `./gradlew build` зелёный локально и в GitHub Actions | CI на main зелёный, тестов 23 (run 38599384215). |
| Все тесты из списка есть и проходят | Есть. В `TicketApiIntegrationTest` — интеграционные тесты: создание → 201 + Location; чтение → 200 с теми же полями; несуществующий id → 404 "Ticket not found"; пустой title → 400 + errors[].field=title; title из 201 символа → 400; не-UUID → 400. В `TicketTest` — unit-тесты Ticket.create: status NEW и createdAt из Clock; title stripped; пустая description → null. Проходят — по CI (см. строку выше). |
| Нет зависимостей вне утверждённого списка | Вне исходного списка только archunit-junit6:1.5.1, согласована человеком на ревью (5140e28). |
| `docker compose up -d` + `./gradlew bootRun`, приложение отвечает на POST/GET | Проверено человеком вручную во время слайса, после пересоздания БД; коммит не зафиксирован. |

## Отклонения от context pack

| Отклонение | Ссылка |
|---|---|
| TicketService и TicketView в application (pack указывал domain) | [ADR 0003](../adr/0003-modular-monolith-layers.md) |
| CHECK (status IN ('NEW')) в V1 | [ADR 0002](../adr/0002-persistence-jpa-flyway.md) |
| Зависимость archunit-junit6:1.5.1 | 5140e28 test: archunit rules and status check constraint |
| Фиксированная en-локаль сообщений об ошибках | [ADR 0004](../adr/0004-api-errors-problem-detail.md) |
| compose на 127.0.0.1:5432, креды через `${DB_*}` | f925972 refactor: break domain-persistence cycle, harden local config |

После слайса: 26e1f50 (PR #2, merge 92ec357) — CI на ubuntu-24.04 вместо ubuntu-latest, см. [ADR 0006](../adr/0006-ci-hardening.md).

## Метрики

- Тестов в CI: 23.

## Коммиты слайса

`git log --oneline bca1b50^1..bca1b50^2`:

```
77d034a ci: build and test on push and pull request
0632457 chore: require approval for agent edits in .github
7132a36 docs: agent journal 2026-10-14 review
d4ee1ed fix: fixed english locale for api error messages
a50131f test: validation messages are locale-independent
f233995 docs: update CLAUDE.md (Boot 4, layers, slice process)
f925972 refactor: break domain-persistence cycle, harden local config
5140e28 test: archunit rules and status check constraint
fefacbc docs: agent journal 2026-10-13/14
cf2abac feat: POST/GET /api/tickets with ProblemDetail errors
370b120 feat: ticket domain and JPA repository
5fd7c03 feat: flyway V1 ticket table
936514a test: integration clock with nanoseconds
724838e chore: remove ineffective Write deny rule
39aa34e test: slice 001 failing tests
8a4f600 build: slice 001 project skeleton
```

## Отложено

- CI: проверка skipped > 0.
- Изоляция данных интеграционных тестов.
- Проверка имени constraint в тесте CHECK.
- Тест локали через RANDOM_PORT.
- Хук против правок src/test на фазе реализации.

## Открытые вопросы (ВОПРОС)

Нет: все ВОПРОС закрыты в разделах «Дополнение», «Дополнение 2» и «Дополнение 3» context pack 001-docs-adr. Коммит усечения до MICROS для ADR 0005 найден через `git log -S "MICROS" --oneline -- src/main` (ровно один: 370b120). Все SHA в ADR и отчёте сверены через `git log` 2026-10-14, subject совпадают.
