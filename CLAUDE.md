# MiniITSM

Учебная ITSM-платформа: заявки (инциденты, запросы на обслуживание), SLA, маршрутизация, согласования.

## Стек
- Java 21 (Gradle toolchain), Spring Boot 4.1.x (Framework 7, Hibernate 7, Jackson 3), Gradle 9 Kotlin DSL
- PostgreSQL 16 + Flyway, Spring Data JPA
- Тесты: JUnit 6, AssertJ, Testcontainers 2, ArchUnit (archunit-junit6)
- Docker Compose для локальной инфраструктуры

## Команды
- `./gradlew test` — все тесты. Нужен запущенный Docker Desktop (Testcontainers)
- Если test UP-TO-DATE — `./gradlew test --rerun-tasks`
- Локальный запуск: `docker compose up -d`, затем `./gradlew bootRun`

## Архитектура
- Модульный монолит, пакеты по фичам: ticket, sla, routing, approval, scripting, integration. Базовый пакет dev.miniitsm
- Слои внутри модуля: api -> application -> domain; application -> persistence -> domain. domain ни от кого не зависит. Проверяет ArchitectureTest — не ослабляй его
- Модуль не обращается к persistence другого модуля, только к его публичному API
- Бизнес-правила и нормализация только в domain; DTO в api — только синтаксис (@NotNull)
- id сущностей генерирует application (UUID), сущность реализует Persistable<UUID>
- Схема только через Flyway, ddl-auto=validate, open-in-view=false
- Ошибки API — ProblemDetail через ApiExceptionHandler; 400: detail "Validation failed" + errors[{field, message}]

## Правила
- Не добавляй зависимости без согласования; версию подтверждай источником (Maven Central, релиз), не по памяти
- Время только через java.time.Clock (бин), никаких now() без Clock
- Миграции Flyway только новыми файлами, применённые не меняй
- Не меняй существующие тесты, если я об этом прямо не попросил
- Не изменяй .claude/ и CLAUDE.md никаким способом, включая Bash
- После изменений запускай сборку и тесты, задача не готова, пока они не зелёные
- Если требование неясно, задай вопрос, не додумывай

## Процесс слайса
1. План в plan mode, без кода
2. Красные тесты отдельным коммитом `test: ...`: покажи, что тесты обнаружены (XML-отчёт) и падают по ожидаемой причине. Стоп
3. Реализация без изменений src/test, зелёная сборка. Стоп
- Задачи: docs/context-packs/, планы слайсов: docs/slices/, решения: docs/adr/