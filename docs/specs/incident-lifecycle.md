# ФЗ: жизненный цикл инцидента и приоритет

Источник требований для слайсов 002–005. Context pack слайса ссылается на разделы этого файла.

## Контекст
Все заявки пока — инциденты. Исполнителя, SLA и запросов на обслуживание нет (недели 2–3).

## 1. Статусы
| Статус | Смысл | Финальный |
|---|---|---|
| NEW | создан, никто не взял | нет |
| IN_PROGRESS | в работе | нет |
| ON_HOLD | ждём пользователя или третью сторону | нет |
| RESOLVED | решение предложено | нет |
| CLOSED | закрыт | да |
| CANCELLED | ошибочный или дубль | да |

ASSIGNED нет: нет исполнителя (маршрутизация, неделя 2).

## 2. Переходы
| Из | Разрешено в |
|---|---|
| NEW | IN_PROGRESS, CANCELLED |
| IN_PROGRESS | ON_HOLD, RESOLVED, CANCELLED |
| ON_HOLD | IN_PROGRESS, CANCELLED |
| RESOLVED | IN_PROGRESS (переоткрытие), CLOSED |
| CLOSED, CANCELLED | — |

- Всё, чего нет в таблице, запрещено, включая переход в тот же статус.
- ON_HOLD → RESOLVED запрещён: выход из паузы — явное событие (будущая пауза SLA).
- CLOSED не переоткрывается: создаётся новый инцидент.

## 3. API перехода
POST /api/tickets/{id}/transitions, тело {"targetStatus": "...", "reason": "..."}, ответ 200 — заявка.
- Запрещённый переход → 409, ProblemDetail: title "Invalid status transition",
  detail "Cannot change status from <FROM> to <TO>", расширения from, to.
- Нет обязательной причины, неизвестный статус, причина длиннее 1000 → 400 по ADR 0004.
- Заявка не найдена → 404.

## 4. Причина перехода
Обязательна (после strip 1..1000): → ON_HOLD, → RESOLVED, → CANCELLED, RESOLVED → IN_PROGRESS.
Для остальных необязательна; пустая или из пробелов → null.

## 5. История статусов
Каждый успешный переход — запись в append-only таблицу ticket_status_change:
id, ticket_id, from_status, to_status, reason, changed_at (через Clock).
API чтения истории — вне слайсов 002–005.

## 6. Приоритет
impact, urgency: HIGH, MEDIUM, LOW. priority: P1..P5.

| Impact \ Urgency | HIGH | MEDIUM | LOW |
|---|---|---|---|
| HIGH | P1 | P2 | P3 |
| MEDIUM | P2 | P3 | P4 |
| LOW | P3 | P4 | P5 |

- Матрица — чистая функция в domain. Ручного переопределения нет.
- При создании impact и urgency необязательны, по умолчанию MEDIUM/MEDIUM (P3) — совместимость с клиентами и тестами слайса 001.
- PUT /api/tickets/{id}/classification {"impact", "urgency"} пересчитывает приоритет; в CLOSED и CANCELLED → 409.
- priority хранится в БД (сортировка, фильтры, SLA); вычисляет только домен.
- Ответы POST и GET содержат impact, urgency, priority.

## 7. Конкурентные изменения
Оптимистичная блокировка (колонка version). Проигравший параллельный запрос → 409 "Concurrent modification".

## 8. Схема БД
Каждый слайс — своя миграция, отдельный коммит feat:. Применённые миграции не меняются (ADR 0002).
- V2 (слайс 002): impact, urgency, priority NOT NULL + CHECK; существующие строки → MEDIUM/MEDIUM/P3; DEFAULT после заполнения убрать (значение задаёт домен).
- V3 (слайс 003): CHECK статуса на 6 значений (имя текущего constraint сверить в БД через \d ticket); таблица ticket_status_change с FK на ticket.
- V4 (слайс 004): version NOT NULL DEFAULT 0.

## 9. Вне слайсов 002–005
- Тип заявки (incident/service request) — неделя 3.
- Исполнитель, группы, назначение — неделя 2.
- SLA, пауза SLA в ON_HOLD, автозакрытие RESOLVED → CLOSED — неделя 2.
- API истории, список и поиск заявок, права на переходы.

## 10. Кандидаты в ADR (агент пишет Proposed)
- 0010: приоритет вычисляет домен по матрице, хранится в БД (слайс 002).
- 0011: переход — команда, недопустимый переход → 409 (слайс 003).
- 0012: оптимистичная блокировка (слайс 004).

## 11. Примеры приёмки
- NEW → IN_PROGRESS: 200, статус IN_PROGRESS, в истории одна запись NEW → IN_PROGRESS.
- NEW → RESOLVED: 409; статус и история не изменились.
- IN_PROGRESS → RESOLVED без reason: 400, errors[].field=reason.
- CLOSED → любой статус: 409.
- Создание с impact=HIGH, urgency=LOW → P3; без полей → MEDIUM/MEDIUM/P3.
- Матрица: тест по всем 9 клеткам; повышение impact или urgency никогда не понижает приоритет.

## 12. Нарезка
| Слайс | Разделы | Миграция |
|---|---|---|
| 002-priority | 6 (кроме PUT), 11 | V2 |
| 003-status-transitions | 1–5, 11 | V3 |
| 004-optimistic-locking | 7 | V4 |
| 005-reclassification | 6 (PUT) | — |
