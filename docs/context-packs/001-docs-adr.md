# Context pack: ADR и отчёт по слайсу 001

## Цель
Зафиксировать решения слайса 001 в docs/adr/ и итог слайса в docs/slices/.
Источник фактов — ТОЛЬКО этот файл и git log. Ничего не додумывать.

## Входит
1. docs/adr/0001..0009 — по одному решению на файл, имена: NNNN-kebab-case.md
2. docs/adr/README.md — индекс: номер, название, статус
3. docs/slices/001-walking-skeleton.md — отчёт по слайсу

## НЕ входит (не трогать)
- CLAUDE.md, .claude/, .github/, src/, build-файлы
- docs/agent-journal.md, docs/context-packs/ (кроме чтения)

## Формат ADR (MADR-lite, на русском)
# NNNN. Название
- Статус: Proposed  (Accepted ставит человек после ревью — не ставить самому)
- Дата: дата коммита, реализовавшего решение (из git log)
## Контекст
## Решение
## Рассмотренные варианты (каждый: суть + почему отвергнут)
## Последствия (плюсы и цена)
## Ссылки (коммиты: короткий SHA + subject из git log)

Правила:
- Каждый SHA сверить через git log. Не нашёлся или subject не совпадает → "ВОПРОС: ..." вместо ссылки.
- Чего нет в этом файле — не писать. Пробел → строка "ВОПРОС: ...".
- Без оценочных слов ("надёжно", "лучшая практика") без причины рядом.

## Решения (источник правды для ADR)

### 0001 Ведём ADR
Контекст: агенты не видят истории решений и "упрощают" неочевидный код; CLAUDE.md должен оставаться коротким.
Решение: одно решение = один файл MADR-lite; принятый ADR не редактируется, изменение = новый ADR со ссылкой "Supersedes"; старый получает "Superseded by".
Варианты: только CLAUDE.md (раздувается, нет "почему"); только коммиты/PR (причину не найти, нет альтернатив).

### 0002 Persistence
Решение: Spring Data JPA (Hibernate 7.4); Ticket — @Entity + Persistable<UUID>; id генерирует application (UUID); схема только Flyway (ddl-auto=validate); open-in-view=false; V1 содержит CHECK (status IN ('NEW')) = ticket_status_check, новые статусы — только новой миграцией.
Варианты: Spring Data JDBC (исключён в context pack слайса 001); id из БД (сущность без id до save); без Persistable — save() с заданным id делает merge с лишним SELECT.
Коммиты: 5fd7c03, 370b120, 5140e28.

### 0003 Модульный монолит и слои
Решение: пакеты по фичам (ticket, sla, routing, approval, scripting, integration), база dev.miniitsm. Слои: api -> application -> domain; application -> persistence -> domain; domain ни от кого. Проверяет ArchitectureTest (ArchUnit). Модуль не ходит в persistence другого модуля.
Контекст: на ревью TicketService и TicketView перенесены из domain в application (первоначальный pack указывал domain).
Варианты: пакеты по слоям (низкая связность фичи); правило только в CLAUDE.md (инструкции вероятностны, тест детерминирован).
Коммиты: 5140e28, f925972.

### 0004 Ошибки API
Решение: ProblemDetail (RFC 9457) через ApiExceptionHandler; 400 = detail "Validation failed" + errors[{field,message}]; сообщения всегда на английском: spring.web.locale=en, spring.web.locale-resolver=fixed.
Контекст: локаль JVM разработчика ru_RU — тексты ошибок зависели от окружения, тест был нестабилен.
Варианты: стандартный формат ошибок Spring (без errors[]); локаль из Accept-Language (ответ зависит от клиента и машины).
Коммиты: a50131f, d4ee1ed.

### 0005 Время через Clock
Решение: время только через бин java.time.Clock; createdAt усекается до MICROS в домене (точность TIMESTAMPTZ).
Контекст: Clock даёт наносекунды, Postgres хранит микросекунды — значение после чтения из БД не совпадало с исходным.
Варианты: Instant.now() (нетестируемо); усечение в БД/маппинге (правило вне домена).
Коммиты: 936514a.

### 0006 Hardening CI
Решение: триггеры push в main + pull_request (не pull_request_target); permissions: contents: read; actions закреплены полным SHA; checkout с persist-credentials: false; шаг проверки суммы tests= в XML, падает при 0; runs-on: ubuntu-24.04; timeout 15 мин.
Контекст: "0 тестов = BUILD SUCCESSFUL" — ложный зелёный; тег action можно перевесить; ubuntu-latest меняет мажор без нашего решения.
Варианты: теги вместо SHA; проверка "== 23" (хардкод — шум и подгонка); ubuntu-latest; pull_request_target (выполняется с секретами и write-токеном).
Последствия: SHA нужно обновлять (план: Dependabot); "> 0" не ловит частичную потерю и skipped (отложено).
Коммиты: 77d034a, 26e1f50.

### 0007 Стратегия merge
Решение: только merge commit (squash и rebase выключены в настройках репо и ruleset). Слайс = один merge-коммит, откат git revert -m 1.
Варианты: squash (теряются отдельные коммиты test:/feat: — доказательство test-first); rebase (новые SHA ломают ссылки аудита).
Коммиты: bca1b50.

### 0008 Защита main
Решение: ruleset protect-main без bypass: запрет удаления и force-push; только через PR (merge commit); обязательная проверка "build" (github-actions), strict. Репозиторий публичный; secret scanning + push protection включены.
Контекст: на Free-плане rulesets не работают для приватных репо. Перед публикацией — gitleaks по всей истории: no leaks.
Проверка негативными тестами: fast-forward push отклонён (правило метода merge); push пустого коммита вне PR отклонён ("через PR" + "build"). Инцидент: push merge-коммита с головой зелёного PR #2 принят как CLI-merge PR #2 (92ec357).
Варианты: приватный репо + платный план; защита без ruleset (только договорённость).
Последствия: 0 approvals — ревью не гарантировано механически (свой PR одобрить нельзя); план: отдельная идентичность агента.
Коммиты: 92ec357.

### 0009 Права агента
Решение: принцип: deny — у агента нет законных сценариев; ask — сценарии есть, риск высокий; allow — обратимо и не повышает привилегий. Bash — allowlist. deny Edit(./.claude/**); ask Edit(./CLAUDE.md), Edit(./.github/**); git commit — ask, push и PR делает человек. CLAUDE.md и .claude/ пишет человек отдельным коммитом.
Контекст: агент, меняющий свои права или инструкции, может повысить привилегии (в т.ч. через промпт-инъекцию). Правило Edit не закрывает запись через Bash.
Варианты: denylist для Bash (обходится); полный запрет коммитов агенту (теряется честный Co-Authored-By).
Последствия: надёжная изоляция только в контейнере с read-only монтированием (план: devcontainer, неделя 3).
Коммиты: 724838e, f233995, 0632457. ВОПРОС для c165c54 — сверить по git log.

## Отчёт docs/slices/001-walking-skeleton.md
Разделы: Цель; Что сделано (по DoD из docs/context-packs/001-walking-skeleton.md); Отклонения от context pack (каждое со ссылкой на ADR); Метрики (тестов в CI: 23); Коммиты слайса (git log bca1b50^2 --oneline, без пересказа диффов); Отложено.
Отклонения: TicketService/TicketView в application; CHECK в V1; archunit-junit6:1.5.1; фиксированная en-локаль; compose на 127.0.0.1:5432 и креды через ${DB_*}; CI на ubuntu-24.04 вместо ubuntu-latest (после слайса).
Отложено: CI skipped>0; изоляция данных интеграционных тестов; проверка имени constraint в тесте CHECK; тест локали через RANDOM_PORT; хук против правок src/test на фазе реализации.

## Процесс
1. Plan mode: список файлов + для каждого ADR — какие разделы заполнены из пака, где будут ВОПРОС. Стоп.
2. Запись файлов, git log для сверки SHA, список всех ВОПРОС в отчёте. Стоп.
3. Коммит "docs: ADR 0001-0009 and slice 001 report" — после одобрения человеком.

## Definition of Done
- 9 ADR + README + отчёт; статус всех ADR — Proposed.
- Каждый SHA проверен git log; непроверенное помечено ВОПРОС.
- git diff --stat затрагивает только docs/adr/ и docs/slices/.
