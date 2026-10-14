# 0009. Права агента

- Статус: Accepted
- Дата: 2026-10-14

## Контекст

Агент, меняющий свои права или инструкции, может повысить привилегии (в т.ч. через промпт-инъекцию). Правило Edit не закрывает запись через Bash.

## Решение

Принцип:
- deny — у агента нет законных сценариев;
- ask — сценарии есть, риск высокий;
- allow — обратимо и не повышает привилегий.

Правила:
- Bash — allowlist.
- deny `Edit(./.claude/**)`.
- ask `Edit(./CLAUDE.md)`, `Edit(./.github/**)`.
- git commit — ask; push и PR делает человек.
- CLAUDE.md и .claude/ пишет человек отдельным коммитом.

## Рассмотренные варианты

- **Denylist для Bash** — отвергнут: обходится.
- **Полный запрет коммитов агенту** — отвергнут: теряется честный Co-Authored-By.

## Последствия

- \+ Агент не может расширить свои права и правила.
- \+ Опасные операции (commit, push, правки CI и CLAUDE.md) требуют одобрения человека.
- \+ Коммиты агента отличимы в истории по Co-Authored-By.
- − Изоляцию, которую не обойти через Bash, даёт только контейнер с read-only монтированием (план: devcontainer, неделя 3).

## Ссылки

- a073ac1 chore: agent environment (permissions, CLAUDE.md)
- c165c54 chore: protect agent settings from agent edits
- 724838e chore: remove ineffective Write deny rule
- f233995 docs: update CLAUDE.md (Boot 4, layers, slice process)
- 0632457 chore: require approval for agent edits in .github
