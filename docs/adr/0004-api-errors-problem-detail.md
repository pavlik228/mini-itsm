# 0004. Ошибки API: ProblemDetail, фиксированная английская локаль

- Статус: Accepted
- Дата: 2026-10-14

## Контекст

Локаль JVM разработчика — ru_RU: тексты ошибок зависели от окружения, тест был нестабилен.

## Решение

- ProblemDetail (RFC 9457) через ApiExceptionHandler.
- 400: detail "Validation failed" + `errors[{field, message}]`.
- Сообщения всегда на английском: `spring.web.locale=en`, `spring.web.locale-resolver=fixed`.

## Рассмотренные варианты

- **Стандартный формат ошибок Spring** — отвергнут: нет `errors[]`.
- **Локаль из Accept-Language** — отвергнут: ответ зависит от клиента и машины.

## Последствия

- \+ Единый машинно-читаемый формат ошибок.
- \+ Тесты не зависят от локали машины.
- − Клиент не получает локализованных сообщений (локализация — задача UI).

## Ссылки

- cf2abac feat: POST/GET /api/tickets with ProblemDetail errors
- a50131f test: validation messages are locale-independent
- d4ee1ed fix: fixed english locale for api error messages
