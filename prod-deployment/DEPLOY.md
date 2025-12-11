# 🚀 Инструкция по деплою Resto-Hub

Краткая пошаговая инструкция для деплоя приложения в продакшн.

## 📋 Чеклист деплоя

- [ ] Образы собраны и опубликованы в Docker Hub
- [ ] VPS подготовлен (Docker установлен)
- [ ] DNS записи настроены
- [ ] SSL сертификаты получены
- [ ] Переменные окружения настроены
- [ ] Приложение запущено и работает

---

## 1️⃣ Локальная сборка и публикация образов

**Выполняется на вашем локальном компьютере перед деплоем.**

### Шаг 1: Установка переменных окружения

**Windows (PowerShell):**
```powershell
$env:DOCKER_HUB_USERNAME = "mouzitoto"  # ваш логин на Docker Hub
$env:DOCKER_HUB_REPO = "resto-hub-repo"  # название вашего репозитория
$env:VITE_API_BASE_URL = "https://api.restohub.kz"
$env:VITE_PARTNER_DOMAIN = "https://partner.restohub.kz"
```

**Linux/Mac:**
```bash
export DOCKER_HUB_USERNAME=mouzitoto
export DOCKER_HUB_REPO=resto-hub-repo
export VITE_API_BASE_URL=https://api.restohub.kz
export VITE_PARTNER_DOMAIN=https://partner.restohub.kz
```

### Шаг 2: Авторизация в Docker Hub

```bash
docker login
```

Введите ваш username и password (или Access Token) от Docker Hub.

### Шаг 3: Сборка и публикация образов

**Windows (PowerShell):**
```powershell
cd resto-hub
cd prod-deployment
.\build-and-push.ps1 latest
```

**Linux/Mac:**
```bash
cd resto-hub
cd prod-deployment
chmod +x build-and-push.sh
./build-and-push.sh latest
```

**Что происходит:**
- Собираются 5 Docker образов: `admin-api`, `client-api`, `client-web`, `admin-web`, `nginx`
- Образы публикуются в Docker Hub: `mouzitoto/resto-hub-repo:admin-api-latest` и т.д.

**Проверка:** Убедитесь, что все образы успешно опубликованы на [Docker Hub](https://hub.docker.com/r/mouzitoto/resto-hub-repo).

---

## 2️⃣ Действия на VPS

**Все следующие команды выполняются на VPS сервере.**

### Шаг 1: Подключение к VPS

```bash
ssh your-user@your-vps-ip
```

### Шаг 2: Переход в директорию проекта

```bash
cd ~/resto-hub-prod/prod-deployment
```

**Если директория не существует:**
```bash
mkdir -p ~/resto-hub-prod/prod-deployment
cd ~/resto-hub-prod/prod-deployment
```

### Шаг 3: Копирование конфигурационных файлов

Убедитесь, что на VPS есть следующие файлы:
- `docker-compose.prod.yml`
- `.env` (создан из `env.template`)
- `nginx.conf.template`
- `ssl/` (директория с SSL сертификатами, если используется HTTPS)

**Если файлов нет, скопируйте их с локального компьютера:**
```bash
# На локальном компьютере
scp prod-deployment/docker-compose.prod.yml ubuntu@your-vps-ip:~/resto-hub-prod/prod-deployment/
scp prod-deployment/env.template ubuntu@your-vps-ip:~/resto-hub-prod/prod-deployment/
scp prod-deployment/nginx.conf.template ubuntu@your-vps-ip:~/resto-hub-prod/prod-deployment/
```

**На VPS создайте .env файл:**
```bash
cd ~/resto-hub-prod/prod-deployment
cp env.template .env
nano .env  # отредактируйте значения
```

### Шаг 4: Настройка .env файла

Откройте `.env` и заполните следующие переменные:

```bash
# Docker Hub настройки
DOCKER_HUB_USERNAME=mouzitoto
DOCKER_HUB_REPO=resto-hub-repo
IMAGE_VERSION=latest

# База данных
POSTGRES_PASSWORD=your_very_strong_password_here_min_16_chars

# Домены
CLIENT_DOMAIN=restohub.kz
PARTNER_DOMAIN=partner.restohub.kz
API_DOMAIN=api.restohub.kz

# Backend переменные (для CORS) - используйте HTTPS!
ADMIN_WEB_URL=https://partner.restohub.kz
CLIENT_WEB_URL=https://restohub.kz

# JWT секрет (обязательно измените!)
JWT_SECRET=your-very-long-and-secure-secret-key-at-least-256-bits-long

# Resend для отправки писем
RESEND_API_KEY=your-resend-api-key-here
RESEND_FROM=no-reply@restohub.kz
```

**Важно:** Используйте сильные пароли и уникальные секретные ключи!

### Шаг 5: Авторизация в Docker Hub на VPS

```bash
docker login
```

Введите ваш username и password от Docker Hub.

### Шаг 6: Проверка конфигурации

```bash
docker compose -f docker-compose.prod.yml config
```

Убедитесь, что нет ошибок в конфигурации.

### Шаг 7: Остановка старых контейнеров (если есть)

```bash
docker compose -f docker-compose.prod.yml down
```

### Шаг 8: Обновление образов

```bash
docker compose -f docker-compose.prod.yml pull
```

Это скачает последние версии образов из Docker Hub.

### Шаг 9: Запуск приложения

```bash
docker compose -f docker-compose.prod.yml up -d
```

**Что происходит:**
- Скачиваются образы из Docker Hub (если еще не скачаны)
- Создаются и запускаются контейнеры
- Все сервисы запускаются в фоновом режиме

### Шаг 10: Проверка статуса

```bash
# Статус всех контейнеров
docker compose -f docker-compose.prod.yml ps

# Просмотр логов
docker compose -f docker-compose.prod.yml logs -f
```

Все сервисы должны быть в статусе `Up` и `healthy`.

### Шаг 11: Проверка работоспособности

```bash
# Проверка API
curl http://localhost:8081/client-api/actuator/health
curl http://localhost:8082/admin-api/actuator/health

# Проверка через домен (если DNS настроен)
curl https://api.restohub.kz/client-api/actuator/health
curl https://api.restohub.kz/admin-api/actuator/health
```

Ожидаемый ответ: `{"status":"UP"}`

---

## 🔄 Обновление приложения

Когда нужно обновить приложение после изменений в коде:

### На локальном компьютере:

1. Внесите изменения в код
2. Соберите и опубликуйте новые образы:
   ```powershell
   # Windows
   cd resto-hub\prod-deployment
   $env:DOCKER_HUB_USERNAME = "mouzitoto"
   $env:DOCKER_HUB_REPO = "resto-hub-repo"
   $env:VITE_API_BASE_URL = "https://api.restohub.kz"
   $env:VITE_PARTNER_DOMAIN = "https://partner.restohub.kz"
   .\build-and-push.ps1 latest
   ```

### На VPS:

```bash
cd ~/resto-hub-prod/prod-deployment

# Остановка сервисов
docker compose -f docker-compose.prod.yml down

# Обновление образов
docker compose -f docker-compose.prod.yml pull

# Запуск с новыми образами
docker compose -f docker-compose.prod.yml up -d

# Проверка логов
docker compose -f docker-compose.prod.yml logs -f
```

---

## 🛠️ Полезные команды

### Просмотр логов

```bash
# Все сервисы
docker compose -f docker-compose.prod.yml logs -f

# Конкретный сервис
docker compose -f docker-compose.prod.yml logs -f admin-api
docker compose -f docker-compose.prod.yml logs -f postgres
```

### Перезапуск сервиса

```bash
docker compose -f docker-compose.prod.yml restart admin-api
```

### Остановка всех сервисов

```bash
docker compose -f docker-compose.prod.yml down
```

### Просмотр использования ресурсов

```bash
docker stats
```

### Резервное копирование базы данных

```bash
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U restohub restohub > backup_$(date +%Y%m%d_%H%M%S).sql
```

---

## ❗ Решение проблем

### Контейнеры не запускаются

```bash
# Проверка логов
docker compose -f docker-compose.prod.yml logs

# Проверка статуса
docker compose -f docker-compose.prod.yml ps

# Перезапуск
docker compose -f docker-compose.prod.yml restart
```

### Образы не найдены

1. Убедитесь, что вы авторизованы: `docker login`
2. Проверьте `.env` файл: `DOCKER_HUB_USERNAME` и `DOCKER_HUB_REPO`
3. Проверьте, что образы опубликованы на Docker Hub

### База данных не подключается

```bash
# Проверка логов PostgreSQL
docker compose -f docker-compose.prod.yml logs postgres

# Проверка подключения
docker compose -f docker-compose.prod.yml exec postgres psql -U restohub -d restohub -c "SELECT version();"
```

### Порты заняты

```bash
# Проверка занятых портов
sudo ss -tulpn | grep :80
sudo ss -tulpn | grep :443

# Остановка конфликтующих сервисов
sudo systemctl stop nginx  # если установлен системный nginx
sudo systemctl stop apache2  # если установлен apache
```

---

## 📚 Дополнительная информация

Для подробной информации о:
- Настройке VPS с нуля
- Настройке DNS
- Получении SSL сертификатов
- Настройке мониторинга

См. [README.md](README.md)

---

## ✅ Чеклист после деплоя

- [ ] Все контейнеры запущены (`docker compose ps`)
- [ ] API доступны через health check endpoints
- [ ] Веб-интерфейсы открываются в браузере
- [ ] SSL сертификаты работают (если используется HTTPS)
- [ ] Логи не содержат ошибок
- [ ] Резервное копирование настроено

**Поздравляем! Приложение развернуто в продакшене! 🎉**

