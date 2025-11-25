# Локальный запуск Resto-Hub

Это руководство поможет вам запустить Resto-Hub на вашем локальном компьютере для разработки и тестирования.

## Требования

- Docker Desktop (BuildKit включен по умолчанию в Docker Desktop)
- Git

## Быстрый старт

### 1. Клонирование репозитория:
```bash
git clone <repository-url>
cd resto-hub
```

### 2. Настройте локальные домены (один раз):

Выберите один из двух вариантов:

**Вариант 1: Автоматическая настройка (рекомендуется)**

**Windows:**
```powershell
# Запустите PowerShell от имени администратора из корня проекта
.\local-deployment\setup-hosts.ps1
```

**Вариант 2: Ручная настройка**

Отредактируйте файл hosts и добавьте следующие строки:
```
127.0.0.1 restohub.local
127.0.0.1 partner.restohub.local
127.0.0.1 api.restohub.local
```

Расположение файла hosts:
- Windows: `C:\Windows\System32\drivers\etc\hosts`

### 3. Запуск сервисов:

```bash
docker compose up --build
```

### 4. Откройте в браузере:
- http://restohub.local - Клиентское приложение
- http://partner.restohub.local - Админ-панель

Готово! Все сервисы будут собраны и запущены автоматически.

## Заметки
**Примечание:** При первом запуске Docker будет:
- Загружать базовые образы (Maven, Node.js, PostgreSQL, nginx и т.д.)
- Загружать все зависимости Maven и npm
- Собирать все проекты (Spring Boot приложения и React приложения)
- Создавать и запускать все контейнеры

Это может занять 5-10 минут в зависимости от скорости вашего интернет-соединения. Последующие сборки будут намного быстрее благодаря кешированию слоев Docker.

## Сервисы

- **PostgreSQL** - База данных (порт 5432)
- **admin-api** - Админ API (порт 8082)
- **client-api** - Клиентский API (порт 8081)
- **admin-web** - Админ веб-интерфейс
- **client-web** - Клиентский веб-интерфейс
- **nginx** - Обратный прокси (порт 80)

## Локальные домены

Проект настроен для работы через локальные домены без указания портов:

- `restohub.local` → Клиентский веб-интерфейс
- `partner.restohub.local` → Админ веб-интерфейс
- `api.restohub.local/client-api` → Клиентский API
- `api.restohub.local/admin-api` → Админ API

Все домены работают только на вашем компьютере и не доступны извне.

## Сборка

Все проекты собираются автоматически внутри Docker контейнеров. Нет необходимости запускать Maven или npm локально.

Процесс сборки:
- Загружает зависимости (кешируются между сборками)
- Компилирует исходный код
- Создает артефакты для продакшена
- Упаковывает в Docker образы

## Переменные окружения для локальной разработки

Все доменные URL настраиваются через переменные окружения. Вы можете переопределить их, создав файл `.env` в корне проекта или установив их в вашем окружении.

### Frontend Build Variables (Vite)
Эти переменные встраиваются во время сборки:
- `VITE_API_BASE_URL` - Базовый URL API (по умолчанию: `http://api.restohub.local`)
- `VITE_PARTNER_DOMAIN` - Домен партнера/админа (по умолчанию: `http://partner.restohub.local`)

### Backend Runtime Variables
Эти переменные можно изменить во время выполнения:
- `ADMIN_WEB_URL` - URL админ веб-интерфейса для CORS (по умолчанию: `http://partner.restohub.local`)
- `CLIENT_WEB_URL` - URL клиентского веб-интерфейса для CORS (по умолчанию: `http://restohub.local`)

### Nginx Domain Variables
Эти переменные настраивают имена серверов в nginx:
- `CLIENT_DOMAIN` - Имя клиентского домена (по умолчанию: `restohub.local`)
- `PARTNER_DOMAIN` - Имя домена партнера/админа (по умолчанию: `partner.restohub.local`)
- `API_DOMAIN` - Имя домена API (по умолчанию: `api.restohub.local`)

## Полезные команды

### Перезапуск сервисов
```bash
docker compose down
docker compose up -d --build
```

### Просмотр логов
```bash
# Все сервисы
docker compose logs -f

# Конкретный сервис
docker compose logs -f admin-api
docker compose logs -f client-api
```

### Остановка всех сервисов
```bash
docker compose down
```

### Очистка Docker (если возникли проблемы)
```bash
docker compose down -v
docker system prune -a --volumes
```

## Тестирование API

### Активация подписки

Пример curl команды для активации подписки находится в файле `local-deployment/curl-subscription-activate`.

Или используйте:

```bash
curl --location 'http://localhost:8082/admin-api/subscriptions/activate' \
--header 'Content-Type: application/json' \
--header 'X-API-Key: change-me-in-production' \
--data '{
    "paymentReference": "SUB-2025-483386",
    "amount": 10000.00,
    "paymentDate": "2025-11-25T12:00:00",
    "externalTransactionId": "1с-001"
}'
```

## Решение проблем

**Если сервисы не запускаются:**
- Проверьте логи: `docker compose logs [service-name]`
- Убедитесь, что все необходимые порты свободны
- Проверьте, что Docker Desktop запущен

**Если домены не работают:**
- Убедитесь, что файл hosts настроен правильно
- Проверьте, что nginx контейнер запущен: `docker compose ps`
- Попробуйте очистить кеш браузера

**Если порт 80 занят:**
- Остановите другие сервисы, использующие порт 80 (например, IIS на Windows)
- Или измените порт nginx в `docker-compose.yml`

**Если возникают ошибки сборки:**
- Очистите Docker кеш: `docker system prune -a --volumes`
- Пересоберите образы: `docker compose build --no-cache`

## Примечания

- Первая сборка может занять больше времени, так как загружаются зависимости
- Последующие сборки используют кешированные зависимости для более быстрой сборки
- Миграции базы данных применяются автоматически при запуске admin-api
- Все сервисы общаются через Docker network
- Frontend приложения используют переменные окружения для всех доменных URL
- CORS настроен для разрешения запросов с настроенных доменов
- Если порт 80 уже используется, вам может потребоваться остановить другие сервисы (например, IIS на Windows)

