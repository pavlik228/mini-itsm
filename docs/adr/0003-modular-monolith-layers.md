# 0003. Модульный монолит и слои

- Статус: Proposed
- Дата: 2026-10-14

## Контекст

Пилот на несколько модулей (заявки, SLA, маршрутизация, согласования), одна команда, сжатые сроки.

На ревью TicketService и TicketView перенесены из domain в application (первоначальный context pack указывал domain).

## Решение

- Пакеты по фичам: ticket, sla, routing, approval, scripting, integration; базовый пакет `dev.miniitsm`.
- Слои: api -> application -> domain; application -> persistence -> domain; domain ни от кого не зависит.
- Модуль не обращается к persistence другого модуля.
- Правила проверяет ArchitectureTest (ArchUnit).

## Рассмотренные варианты

- **Пакеты по слоям** — отвергнут: низкая связность фичи.
- **Правило только в CLAUDE.md** — отвергнут: инструкции вероятностны, тест детерминирован.
- **Микросервисы** — отвергнуты: операционная сложность не нужна пилоту; модульность по фичам оставляет путь к выделению модуля позже.

## Последствия

- \+ Границы проверяются тестом.
- − ArchUnit-правила нужно расширять с каждым новым модулем.

## Ссылки

- 5140e28 test: archunit rules and status check constraint
- f925972 refactor: break domain-persistence cycle, harden local config
