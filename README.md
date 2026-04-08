# emotion-mifrations

Production-ready backend-сервис для синхронизации постов из Telegram-каналов в VK-сообщества с clean architecture, файловым state storage и поддержкой редактирования постов.

## Ключевые архитектурные решения
- **Clean Architecture**: `domain`, `application`, `infrastructure`, `presentation`, `config`.
- **Порты и адаптеры**: интеграции с Telegram, VK, уведомлениями и storage спрятаны за портами.
- **Без БД**: состояние хранится в файле + in-memory cache, с retention 5 дней и регулярной очисткой.
- **Надежность**: retry (3 попытки, exponential backoff) для внешних вызовов.
- **Наблюдаемость**: структурированные логи, correlation id, in-memory event journal.

## Почему TDLib, а не Telegram Bot API
### Ограничения Bot API в данном кейсе
1. Bot API надежно работает только там, где бот добавлен и имеет нужные права.
2. Ограниченная полнота данных для историй редактирования/сложных сущностей каналов в enterprise-сценариях.
3. Обработка медиа-групп и редактирований из каналов с высокой нагрузкой менее предсказуема, чем клиентский API.

### Плюсы TDLib
1. TDLib работает как полноценный Telegram-клиент и дает более стабильный доступ к каналам и их контенту.
2. Лучше подходит для обработки постов с медиа, альбомов и редактирований.
3. Позволяет построить единый поток событий для новых и отредактированных постов.

### Ограничения TDLib
- Требуются `api_id`, `api_hash`, авторизация аккаунта и операционное сопровождение клиентской сессии.
- Необходима дисциплина хранения сессии и защита окружения с секретами.

## Режимы обработки
- **Новые посты**: принимаются только события после старта (старые не поднимаются).
- **Edited posts**: по `messageId` и `editVersion` выполняется обновление существующего VK-поста.
- **Media groups**: обрабатываются как единый пост, публикация только после полной сборки.
- **Форматирование**: переносы, emoji, ссылки сохраняются; в конце добавляется ссылка `ЭмоциON`.
- **Опросы/викторины**: создаются в VK API как poll с best-effort маппингом.

## Важные ограничения API и компромиссы
1. Полное 1:1 соответствие Telegram entities и VK markup недостижимо — реализована практическая конвертация текста.
2. Некоторые типы контента Telegram (кружки, документы, аудио) осознанно не публикуются.
3. Викторины Telegram могут терять часть семантики в VK (например, расширенные режимы приватности).

## Конфигурация
Используются только `application.properties` и env vars.

Основные префиксы:
- `app.telegram.tdlib.*`
- `app.telegram.channels[*].*`
- `app.vk.*`
- `app.state.*`
- `app.retry.*`
- `app.notifications.*`
- `app.formatting.*`

См. `src/main/resources/application-example.properties`.
- Шаблон переменных окружения: `.env.example` (для локального старта без хранения секретов в Git).


## REST API
- `GET /api/v1/system/health`
- `GET /api/v1/system/integrations`
- `GET /api/v1/system/mappings`
- `GET /api/v1/system/events`
- `GET /api/v1/system/state`
- `POST /api/v1/operations/retry`

Swagger UI: `/swagger-ui/index.html`

## Запуск
```bash
mvn clean test
mvn spring-boot:run
```

## Тесты
Покрыты:
- deduplication;
- retention cleanup (старше 5 дней);
- TG->VK mapping;
- use case публикации/обновления;
- retry;
- форматирование;
- unsupported content;
- poll creation (через use case);
- global exception handler.

## Реализовано дополнительно
1. VK adapter переведён на HTTP-взаимодействие с upload flow для `photo` и `video` (через `photos.getWallUploadServer`/`photos.saveWallPhoto` и `video.save`).
2. Event-journal вынесен в файловый JSONL-журнал с ротацией по размеру и лимитом архивов.
3. TDLib ingestion слой переведён на gateway-модель (`TdlibGateway` + `TdlibTelegramSourceAdapter`) для подключения реального клиента без утечки SDK в domain/application.

## Возможные улучшения
1. Подключить конкретную реализацию `TdlibGateway` на выбранной Java-библиотеке TDLib (tdlight/jni) с авторизацией и durable session state.
2. Добавить интеграционные тесты VK API и TDLib-адаптера с WireMock/Testcontainers.
3. Добавить политику повторной загрузки неуспешных media-групп в отдельной очереди.

