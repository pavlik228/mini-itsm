# Context pack: слайс 001 — ходячий скелет

## Цель
Минимальный сквозной путь: HTTP → домен → PostgreSQL → HTTP, собирается и тестируется в CI.
Это фундамент; функциональности ITSM (статусы, SLA, приоритет) здесь НЕТ.

## Входит в слайс
1. Gradle-проект (Kotlin DSL, wrapper, toolchain Java 21), Spring Boot 4.x — последний стабильный патч.
   (Если решено остаться на 3.5 — заменить на «Spring Boot 3.5.x» и поправить зависимости.)
2. Создание заявки: POST /api/tickets
3. Чтение заявки: GET /api/tickets/{id}
4. Миграция Flyway V1 — таблица ticket.
5. docker-compose.yml — только PostgreSQL 16 для локальной разработки (приложение через bootRun).
6. GitHub Actions: ./gradlew build на push и pull_request.

## НЕ входит (не делать)
- Список/поиск заявок, обновление, удаление, статусы кроме NEW, приоритет, SLA.
- Аутентификация, Actuator, OpenAPI/Swagger.
- Lombok, MapStruct, Spring Data JDBC.

## Контракт API
POST /api/tickets
- Тело: { "title": string, обязательно; "description": string, необязательно }
- 201 Created, заголовок Location: /api/tickets/{id} (относительный), тело — заявка.
- 400 — ошибка валидации, ProblemDetail (RFC 9457) с расширением errors:
  {"type":"about:blank","title":"Bad Request","status":400,"detail":"Validation failed",
  "instance":"/api/tickets","errors":[{"field":"title","message":"..."}]}

GET /api/tickets/{id}
- 200 — { "id": uuid, "title", "description", "status": "NEW", "createdAt": ISO-8601 UTC }
- 404 — ProblemDetail с title "Ticket not found" и id в detail.
- 400 — если id не UUID, ProblemDetail.

## Правила полей (живут в домене)
- title: strip(); после strip длина 1..200, иначе ошибка валидации.
- description: не тримится; пустая или из пробелов → null; длина ≤ 4000.
- Нормализация и инварианты — только в Ticket.create. DTO — только синтаксическая валидация.

## Модель данных
ticket: id UUID PK, title VARCHAR(200) NOT NULL, description VARCHAR(4000) NULL,
status VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL.

## Технические решения
- Пакеты: dev.miniitsm.ticket.api (контроллер, DTO, обработчик ошибок), .domain (Ticket, TicketStatus, сервис), .persistence (репозиторий).
- Доступ к БД: Spring Data JPA (Hibernate). spring.jpa.open-in-view=false, spring.jpa.hibernate.ddl-auto=validate — схему ведёт только Flyway.
- Ticket — класс @Entity (НЕ record), не выходит за пределы сервиса. Records — только DTO и view-модели.
- id генерирует сервис (UUID) и передаёт в Ticket.create(id, title, description, clock).
  Ticket реализует Persistable<UUID> (isNew), иначе save() сделает merge с лишним SELECT.
- Время — только через инжектируемый java.time.Clock; в тестах фиксированный Clock.
  createdAt обрезается до микросекунд (точность TIMESTAMPTZ).
- Ошибки — @RestControllerAdvice + ProblemDetail.

## Разрешённые зависимости (других не добавлять без согласования)
web, validation, data-jpa, Flyway + поддержка PostgreSQL, драйвер postgresql (runtime),
test-стартер, spring-boot-testcontainers, Testcontainers PostgreSQL + JUnit 5.
Точные артефакты для Boot 4 агент сверяет по официальным источникам; итоговый список
утверждает человек и вписывает сюда:
- (заполнить после согласования)

## Тесты (пишутся первыми)
- Интеграционные (@SpringBootTest + Testcontainers PostgreSQL + MockMvc):
  создание → 201 + Location; чтение созданной → 200 с теми же полями;
  несуществующий id → 404 с title "Ticket not found"; пустой title → 400 + errors[].field=title;
  title из 201 символа → 400; не-UUID → 400.
- Unit: Ticket.create — status NEW, createdAt из Clock, title stripped, пустая description → null.
- Правило: каждый тест должен падать до реализации. Тест, который может пройти без нашего кода, переписывается.

## CI
- GitHub Actions, ubuntu-latest, JDK 21 (temurin), ./gradlew build.
- permissions: contents: read (least privilege для GITHUB_TOKEN).

## Процесс
- Ветка slice/001-walking-skeleton.
- Контрольная точка: после красных тестов — стоп, тесты отдельным коммитом, реализация только после ревью человека.
- curl в deny — HTTP-запросы для ручной проверки запускает человек.

## Definition of Done
- ./gradlew build зелёный локально и в GitHub Actions.
- Все тесты из списка есть и проходят.
- Нет зависимостей вне утверждённого списка.
- docker compose up -d поднимает PostgreSQL, приложение стартует через ./gradlew bootRun и отвечает на POST/GET.

## Окружение
macOS Intel, Docker Desktop (Testcontainers локально), JDK 21 через toolchain (по умолчанию в системе JDK 22).