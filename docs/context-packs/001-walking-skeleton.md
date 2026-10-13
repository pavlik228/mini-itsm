# Context pack: слайс 001 — ходячий скелет

## Цель
Минимальный сквозной путь: HTTP → домен → PostgreSQL → HTTP, собирается и тестируется в CI.
Это фундамент; функциональности ITSM (статусы, SLA, приоритет) здесь НЕТ.

## Входит в слайс
1. Gradle-проект (Kotlin DSL, wrapper, toolchain Java 21), Spring Boot 3.x (последняя стабильная).
2. Создание заявки: POST /api/tickets
3. Чтение заявки: GET /api/tickets/{id}
4. Миграция Flyway V1 — таблица ticket.
5. docker-compose.yml — только PostgreSQL для локальной разработки (приложение запускается через bootRun).
6. GitHub Actions: ./gradlew build на push и pull_request.

## НЕ входит (не делать)
- Список/поиск заявок, обновление, удаление, статусы, приоритет, SLA.
- Аутентификация, Actuator, OpenAPI/Swagger.
- JPA/Hibernate, Lombok, MapStruct.

## Контракт API
POST /api/tickets
- Тело: { "title": string, обязательно, 1..200 символов после trim;
  "description": string, необязательно, до 4000 символов }
- 201 Created, заголовок Location: /api/tickets/{id}, тело — заявка.
- 400 — ошибка валидации, тело ProblemDetail (RFC 9457) с перечнем полей.

GET /api/tickets/{id}
- 200 — заявка: { "id": uuid, "title", "description", "status": "NEW", "createdAt": ISO-8601 UTC }
- 404 — ProblemDetail.
- 400 — если id не UUID, ProblemDetail.

## Модель данных
ticket: id UUID PK, title VARCHAR(200) NOT NULL, description VARCHAR(4000) NULL,
status VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL.
Статус в этом слайсе всегда NEW (enum TicketStatus с одним значением).

## Технические решения
- Пакеты: dev.miniitsm.ticket.api (контроллер, DTO), .domain (Ticket, TicketStatus, сервис), .persistence (репозиторий).
- Доступ к БД: Spring Data JPA (Hibernate). spring.jpa.open-in-view=false, spring.jpa.hibernate.ddl-auto=validate (схему ведёт только Flyway).
- Сущность Ticket — обычный класс (@Entity), НЕ record; DTO — records. Сущность не выходит за пределы сервиса, контроллер работает с DTO.
- id генерирует приложение → сущность реализует Persistable<UUID> (isNew), иначе save() сделает merge с лишним SELECT.
- DTO и доменные объекты — records.
- Время — только через инжектируемый java.time.Clock; в тестах фиксированный Clock.
- UUID генерирует приложение.
- Ошибки — через @RestControllerAdvice + ProblemDetail.

## Разрешённые зависимости (других не добавлять без вопроса)
spring-boot-starter-web, spring-boot-starter-validation, spring-boot-starter-data-jpa,
flyway-core, flyway-database-postgresql, postgresql (runtime),
spring-boot-starter-test, spring-boot-testcontainers, testcontainers: postgresql, junit-jupiter.

## Тесты (пишутся первыми)
- Интеграционные (@SpringBootTest + Testcontainers PostgreSQL + MockMvc):
  создание → 201 + Location; чтение созданной → 200 с теми же полями;
  несуществующий id → 404 ProblemDetail; пустой title → 400; title 201 символ → 400; не-UUID → 400.
- Unit: создание Ticket присваивает status NEW и createdAt из Clock.

## Definition of Done
- ./gradlew build зелёный локально и в GitHub Actions.
- Все тесты из списка выше есть и проходят.
- Нет зависимостей вне списка.
- docker compose up -d поднимает PostgreSQL, приложение стартует через ./gradlew bootRun и отвечает на POST/GET.

## Окружение
macOS Intel, Docker Desktop (Testcontainers работает локально), JDK 21 через toolchain (по умолчанию в системе JDK 22).