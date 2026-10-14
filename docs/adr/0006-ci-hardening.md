# 0006. Hardening CI

- Статус: Accepted
- Дата: 2026-10-14

## Контекст

- «0 тестов = BUILD SUCCESSFUL» — ложный зелёный.
- Тег action можно перевесить.
- ubuntu-latest меняет мажор без нашего решения.

## Решение

- Триггеры: push в main + pull_request (не pull_request_target).
- `permissions: contents: read`.
- Actions закреплены полным SHA.
- checkout с `persist-credentials: false`.
- Шаг проверки суммы `tests=` в XML-отчётах, падает при 0.
- `runs-on: ubuntu-24.04`.
- timeout 15 мин.

## Рассмотренные варианты

- **Теги вместо SHA** — отвергнут: тег action можно перевесить.
- **Проверка «== 23»** — отвергнут: хардкод — шум и подгонка.
- **ubuntu-latest** — отвергнут: меняет мажор без нашего решения.
- **pull_request_target** — отвергнут: выполняется с секретами и write-токеном.

## Последствия

- \+ Воспроизводимая сборка.
- \+ Подмена action по тегу невозможна.
- \+ CI падает, если тесты не запустились.
- − SHA нужно обновлять (план: Dependabot).
- − Проверка «> 0» не ловит частичную потерю тестов и skipped (отложено).

## Ссылки

- 77d034a ci: build and test on push and pull request
- 26e1f50 ci: pin runner to ubuntu-24.04
