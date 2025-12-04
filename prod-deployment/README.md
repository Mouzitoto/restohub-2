# Деплой Resto-Hub в продакшн

Пошаговая инструкция по развертыванию Resto-Hub на VPS сервере.

## 🚀 Быстрый старт

**Требуемая ОС:** Ubuntu 22.04 LTS или Ubuntu 24.04 LTS

1. Выберите Ubuntu 22.04 LTS или Ubuntu 24.04 LTS при создании VPS
2. Следуйте инструкциям ниже
3. Все команды готовы к использованию

## 📋 Содержание

1. [Требования к VPS](#требования-к-vps)
2. [Подготовка VPS](#подготовка-vps)
3. [Настройка доменов и DNS](#настройка-доменов-и-dns)
4. [Настройка SSL сертификатов](#настройка-ssl-сертификатов)
5. [Настройка переменных окружения](#настройка-переменных-окружения)
6. [Настройка nginx](#настройка-nginx)
7. [Деплой приложения](#деплой-приложения)
8. [Проверка работоспособности](#проверка-работоспособности)
9. [Мониторинг и обслуживание](#мониторинг-и-обслуживание)
10. [Решение проблем](#решение-проблем)

---

## 🎯 Требования к операционной системе

**Поддерживаемые версии Ubuntu:**
- ✅ **Ubuntu 22.04 LTS** (рекомендуется)
- ✅ **Ubuntu 24.04 LTS**

**Почему Ubuntu:**
- Отличная поддержка Docker (официальные репозитории)
- Долгосрочная поддержка (LTS)
- Большое сообщество и обширная документация
- Стабильность и безопасность
- Простота настройки и обслуживания

### Проверка версии Ubuntu

Если вы уже создали VPS, проверьте версию:

```bash
# Проверка версии Ubuntu
lsb_release -a

# Или
cat /etc/os-release
```

---

## Требования к VPS

### Минимальные требования (для начала работы):
- **RAM:** 4 ГБ
- **Диск:** 80 ГБ SSD
- **CPU:** 4 ядра (разделяемый процессор)
- **IPv4:** 1 адрес
- **ОС:** Ubuntu 22.04 LTS или Ubuntu 24.04 LTS

### Рекомендуемые требования (для комфортной работы):
- **RAM:** 6-8 ГБ
- **Диск:** 100+ ГБ SSD
- **CPU:** 4+ ядра

### Распределение памяти (для 4 ГБ RAM):

| Сервис | Ограничение памяти | Резервирование |
|--------|-------------------|----------------|
| PostgreSQL | 800 МБ | 400 МБ |
| admin-api | 600 МБ | 300 МБ |
| client-api | 600 МБ | 300 МБ |
| client-web | 64 МБ | 32 МБ |
| admin-web | 64 МБ | 32 МБ |
| nginx | 128 МБ | 64 МБ |
| Система + Docker | ~500-800 МБ | - |
| **Итого** | **~2.2 ГБ** | **~1.1 ГБ** |

**Остаток:** ~1.8 ГБ для пиковых нагрузок и кеширования.

---

## Подготовка VPS

### Шаг 1: Подключение к серверу

```bash
ssh root@your-vps-ip
# или
ssh your-user@your-vps-ip
```

### Шаг 2: Обновление системы

```bash
sudo apt update && sudo apt upgrade -y
```

### Шаг 3: Установка Docker и Docker Compose

```bash
# Установка зависимостей
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

# Добавление официального GPG ключа Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Добавление репозитория Docker
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Установка Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER

# Перезагрузка сессии
newgrp docker
```

**Проверка установки:**

```bash
docker --version
docker compose version
```

Ожидаемый вывод:
```
Docker version 24.x.x
Docker Compose version v2.x.x
```

### Шаг 4: Настройка Firewall

```bash
# Установка UFW (если не установлен)
sudo apt install -y ufw

# Разрешение SSH (важно сделать первым!)
sudo ufw allow 22/tcp

# Разрешение HTTP и HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Включение firewall
sudo ufw enable

# Проверка статуса
sudo ufw status
```

**Важно:** Не закрывайте SSH доступ (порт 22), иначе потеряете доступ к серверу!

### Шаг 5: Клонирование репозитория

```bash
# Переход в домашнюю директорию
cd ~

# Клонирование репозитория (замените на ваш URL)
git clone https://github.com/your-username/resto-hub.git

# Переход в директорию проекта
cd resto-hub
```

---

## Настройка доменов и DNS

### Шаг 1: Получение IP адреса сервера

```bash
# Узнать внешний IP адрес сервера
curl ifconfig.me
# или
hostname -I
```

Запишите этот IP адрес - он понадобится для настройки DNS.

### Шаг 2: Настройка DNS записей

Вам нужно настроить DNS записи у вашего регистратора домена (например, у ps.kz или другого провайдера).

**Необходимые DNS записи:**

| Тип | Имя | Значение | TTL |
|-----|-----|----------|-----|
| A | @ | IP_ВАШЕГО_VPS | 3600 |
| A | partner | IP_ВАШЕГО_VPS | 3600 |
| A | api | IP_ВАШЕГО_VPS | 3600 |

**Пример для домена `restohub.kz`:**

```
A запись: restohub.kz -> 123.45.67.89
A запись: partner.restohub.kz -> 123.45.67.89
A запись: api.restohub.kz -> 123.45.67.89
```

**Где настраивать DNS:**
- В панели управления вашего регистратора домена
- Или у вашего DNS провайдера (Cloudflare, Route53 и т.д.)

### Шаг 3: Проверка DNS записей

После настройки DNS подождите 5-15 минут для распространения записей, затем проверьте:

```bash
# Проверка DNS записей
dig restohub.kz +short
dig partner.restohub.kz +short
dig api.restohub.kz +short

# Или используя nslookup
nslookup restohub.kz
nslookup partner.restohub.kz
nslookup api.restohub.kz
```

Все записи должны возвращать IP адрес вашего VPS.

---

## Настройка SSL сертификатов

### Шаг 1: Установка Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### Шаг 2: Получение SSL сертификатов

**Важно:** Перед получением сертификатов убедитесь, что:
1. DNS записи настроены и распространились
2. Порты 80 и 443 открыты в firewall
3. Домены указывают на IP вашего VPS

```bash
# Получение сертификатов для всех доменов
sudo certbot certonly --standalone \
  -d restohub.kz \
  -d partner.restohub.kz \
  -d api.restohub.kz \
  --email your-email@example.com \
  --agree-tos \
  --non-interactive
```

**Где будут сохранены сертификаты:**
- `/etc/letsencrypt/live/restohub.kz/fullchain.pem` - сертификат
- `/etc/letsencrypt/live/restohub.kz/privkey.pem` - приватный ключ

### Шаг 3: Настройка автоматического обновления

Сертификаты Let's Encrypt действительны 90 дней. Настройте автоматическое обновление:

```bash
# Тест обновления
sudo certbot renew --dry-run

# Certbot автоматически создает cron задачу для обновления
# Проверьте её наличие:
sudo systemctl list-timers | grep certbot
```

### Шаг 4: Копирование сертификатов для Docker

Создайте директорию для SSL сертификатов в проекте:

```bash
# Переход в директорию проекта
cd ~/resto-hub/prod-deployment

# Создание директории для SSL
mkdir -p ssl

# Копирование сертификатов (замените restohub.kz на ваш домен)
sudo cp /etc/letsencrypt/live/restohub.kz/fullchain.pem ssl/
sudo cp /etc/letsencrypt/live/restohub.kz/privkey.pem ssl/

# Установка прав доступа
sudo chown -R $USER:$USER ssl/
chmod 600 ssl/privkey.pem
chmod 644 ssl/fullchain.pem
```

**Примечание:** После обновления сертификатов нужно будет скопировать их заново или настроить автоматическое копирование.

---

## Настройка переменных окружения

### Шаг 1: Создание .env файла

```bash
# Переход в директорию prod-deployment
cd ~/resto-hub/prod-deployment

# Создание .env файла
nano .env
```

### Шаг 2: Заполнение переменных

Скопируйте и заполните следующий шаблон (замените значения на ваши):

```bash
# База данных
POSTGRES_PASSWORD=your_very_strong_password_here_min_16_chars

# Домены (замените на ваши реальные домены)
CLIENT_DOMAIN=restohub.kz
PARTNER_DOMAIN=partner.restohub.kz
API_DOMAIN=api.restohub.kz

# Frontend переменные (для сборки) - используйте HTTPS!
VITE_API_BASE_URL=https://api.restohub.kz
VITE_PARTNER_DOMAIN=https://partner.restohub.kz

# Backend переменные (для CORS) - используйте HTTPS!
ADMIN_WEB_URL=https://partner.restohub.kz
CLIENT_WEB_URL=https://restohub.kz

# JWT секрет (обязательно измените! Минимум 256 бит)
JWT_SECRET=your-very-long-and-secure-secret-key-at-least-256-bits-long-change-this-in-production

# API ключ для 1C интеграции (если используется)
API_KEY=your-api-key-here-change-in-production
```

**Важно:**
- Используйте **сильные пароли** (минимум 16 символов, буквы, цифры, спецсимволы)
- Используйте **HTTPS** в URL для продакшена
- **Обязательно измените** JWT_SECRET на уникальный ключ
- Сохраните `.env` файл в безопасном месте (не коммитьте в git!)

### Шаг 3: Проверка .env файла

```bash
# Проверка содержимого (пароли не отобразятся полностью)
cat .env | grep -v PASSWORD
```

---

## Настройка nginx

### Шаг 1: Обновление nginx конфигурации для SSL

Конфигурация nginx уже настроена в проекте, но нужно убедиться, что она поддерживает SSL.

Проверьте файл `../nginx/nginx.conf.template` - он должен содержать настройки для HTTPS.

Если нужно добавить SSL поддержку, создайте файл `nginx-ssl.conf.template`:

```bash
cd ~/resto-hub/prod-deployment
nano nginx-ssl.conf.template
```

Вставьте следующую конфигурацию (замените `restohub.kz` на ваш домен):

```nginx
# Редирект HTTP на HTTPS
server {
    listen 80;
    server_name restohub.kz partner.restohub.kz api.restohub.kz;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    
    location / {
        return 301 https://$host$request_uri;
    }
}

# Клиентское приложение (HTTPS)
server {
    listen 443 ssl http2;
    server_name restohub.kz;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://client-web:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Админ-панель (HTTPS)
server {
    listen 443 ssl http2;
    server_name partner.restohub.kz;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://admin-web:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# API endpoints (HTTPS)
server {
    listen 443 ssl http2;
    server_name api.restohub.kz;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Client API
    location /client-api {
        proxy_pass http://client-api:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Admin API
    location /admin-api {
        proxy_pass http://admin-api:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Если вы создали отдельный файл, обновите `docker-compose.prod.yml`:

```yaml
nginx:
  volumes:
    - ./nginx-ssl.conf.template:/etc/nginx/templates/default.conf.template:ro
    - ./ssl:/etc/nginx/ssl:ro
```

---

## Деплой приложения

### Шаг 1: Переход в директорию проекта

```bash
cd ~/resto-hub/prod-deployment
```

### Шаг 2: Проверка конфигурации

```bash
# Проверка синтаксиса docker-compose
docker compose -f docker-compose.prod.yml config
```

### Шаг 3: Сборка и запуск

```bash
# Сборка и запуск всех сервисов в фоновом режиме
docker compose -f docker-compose.prod.yml up -d --build
```

Этот процесс может занять 5-15 минут при первой сборке, так как:
- Загружаются базовые образы (PostgreSQL, Java, Node.js, nginx)
- Скачиваются зависимости Maven и npm
- Компилируются Java приложения
- Собираются React приложения

### Шаг 4: Просмотр логов

```bash
# Просмотр логов всех сервисов
docker compose -f docker-compose.prod.yml logs -f

# Просмотр логов конкретного сервиса
docker compose -f docker-compose.prod.yml logs -f admin-api
docker compose -f docker-compose.prod.yml logs -f postgres
docker compose -f docker-compose.prod.yml logs -f nginx
```

### Шаг 5: Проверка статуса сервисов

```bash
# Статус всех контейнеров
docker compose -f docker-compose.prod.yml ps

# Детальная информация
docker compose -f docker-compose.prod.yml ps -a
```

Все сервисы должны быть в статусе `Up` и `healthy`.

---

## Проверка работоспособности

### Шаг 1: Проверка здоровья API

```bash
# Проверка через localhost
curl http://localhost:8081/client-api/actuator/health
curl http://localhost:8082/admin-api/actuator/health

# Проверка через домен (если DNS настроен)
curl https://api.restohub.kz/client-api/actuator/health
curl https://api.restohub.kz/admin-api/actuator/health
```

Ожидаемый ответ:
```json
{"status":"UP"}
```

### Шаг 2: Проверка веб-интерфейсов

Откройте в браузере:
- `https://restohub.kz` - клиентское приложение
- `https://partner.restohub.kz` - админ-панель
- `https://api.restohub.kz/client-api/actuator/health` - API health check

### Шаг 3: Проверка использования ресурсов

```bash
# Использование ресурсов контейнерами
docker stats

# Использование памяти системой
free -h

# Использование диска
df -h
```

---

## Мониторинг и обслуживание

### Ежедневные задачи

**Проверка логов:**
```bash
docker compose -f docker-compose.prod.yml logs --tail=100
```

**Проверка использования ресурсов:**
```bash
docker stats --no-stream
```

### Еженедельные задачи

**Обновление системы:**
```bash
sudo apt update && sudo apt upgrade -y
```

**Очистка неиспользуемых Docker образов:**
```bash
docker system prune -a --volumes
```

**Проверка обновлений сертификатов:**
```bash
sudo certbot renew --dry-run
```

### Резервное копирование базы данных

**Создание бэкапа:**
```bash
cd ~/resto-hub/prod-deployment
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U restohub restohub > backup_$(date +%Y%m%d_%H%M%S).sql
```

**Восстановление из бэкапа:**
```bash
docker compose -f docker-compose.prod.yml exec -T postgres psql -U restohub restohub < backup_20250101_120000.sql
```

**Автоматическое резервное копирование (cron):**

Добавьте в crontab (`crontab -e`):
```bash
# Ежедневный бэкап в 2:00 ночи
0 2 * * * cd /home/user/resto-hub/prod-deployment && docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U restohub restohub > /backups/restohub_$(date +\%Y\%m\%d).sql
```

### Обновление приложения

```bash
cd ~/resto-hub/prod-deployment

# Остановка сервисов
docker compose -f docker-compose.prod.yml down

# Получение последних изменений
cd ..
git pull
cd prod-deployment

# Пересборка и запуск
docker compose -f docker-compose.prod.yml up -d --build

# Проверка логов
docker compose -f docker-compose.prod.yml logs -f
```

---

## Решение проблем

### Проблема: Контейнеры не запускаются

**Решение:**
```bash
# Проверка логов
docker compose -f docker-compose.prod.yml logs

# Проверка статуса
docker compose -f docker-compose.prod.yml ps

# Перезапуск сервисов
docker compose -f docker-compose.prod.yml restart
```

### Проблема: Нехватка памяти

**Решение:**
```bash
# Проверка использования памяти
docker stats
free -h

# Если нехватка памяти, уменьшите лимиты в docker-compose.prod.yml
# или рассмотрите увеличение RAM VPS
```

### Проблема: Домены не работают

**Решение:**
```bash
# Проверка DNS
dig restohub.kz
nslookup restohub.kz

# Проверка nginx
docker compose -f docker-compose.prod.yml logs nginx

# Проверка конфигурации nginx
docker compose -f docker-compose.prod.yml exec nginx nginx -t
```

### Проблема: SSL сертификаты не работают

**Решение:**
```bash
# Проверка сертификатов
sudo certbot certificates

# Обновление сертификатов
sudo certbot renew

# Копирование обновленных сертификатов
sudo cp /etc/letsencrypt/live/restohub.kz/fullchain.pem ~/resto-hub/prod-deployment/ssl/
sudo cp /etc/letsencrypt/live/restohub.kz/privkey.pem ~/resto-hub/prod-deployment/ssl/

# Перезапуск nginx
docker compose -f docker-compose.prod.yml restart nginx
```

### Проблема: База данных не подключается

**Решение:**
```bash
# Проверка статуса PostgreSQL
docker compose -f docker-compose.prod.yml logs postgres

# Проверка подключения
docker compose -f docker-compose.prod.yml exec postgres psql -U restohub -d restohub -c "SELECT version();"

# Проверка переменных окружения
docker compose -f docker-compose.prod.yml exec admin-api env | grep DB
```

### Проблема: Порты заняты

**Решение:**
```bash
# Проверка занятых портов
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :443

# Остановка конфликтующих сервисов
sudo systemctl stop apache2  # если установлен
sudo systemctl stop nginx    # если установлен системный nginx
```

---

## Безопасность

### Рекомендации:

1. **Измените все пароли по умолчанию** в `.env`
2. **Используйте сильные JWT секреты** (минимум 256 бит)
3. **Не коммитьте `.env` файл** в git (добавьте в `.gitignore`)
4. **Регулярно обновляйте систему:**
   ```bash
   sudo apt update && sudo apt upgrade -y
   ```
5. **Ограничьте доступ к портам базы данных** (не открывайте 5432 наружу)
6. **Настройте fail2ban** для защиты от брутфорса:
   ```bash
   sudo apt install -y fail2ban
   sudo systemctl enable fail2ban
   sudo systemctl start fail2ban
   ```
7. **Используйте SSH ключи** вместо паролей для SSH доступа
8. **Регулярно проверяйте логи** на подозрительную активность

---

## Масштабирование

При росте нагрузки рассмотрите:

1. **Увеличение ресурсов VPS** (больше RAM, CPU)
2. **Горизонтальное масштабирование** (несколько инстансов API)
3. **Использование CDN** для статических файлов
4. **Кеширование** (Redis для сессий и кеша)
5. **Отдельный сервер для базы данных**
6. **Load balancer** для распределения нагрузки

---

## Полезные команды

```bash
# Просмотр всех контейнеров
docker ps -a

# Просмотр использования ресурсов
docker stats

# Просмотр логов конкретного сервиса
docker compose -f docker-compose.prod.yml logs -f [service-name]

# Перезапуск сервиса
docker compose -f docker-compose.prod.yml restart [service-name]

# Остановка всех сервисов
docker compose -f docker-compose.prod.yml down

# Остановка с удалением volumes (ОСТОРОЖНО!)
docker compose -f docker-compose.prod.yml down -v

# Просмотр конфигурации
docker compose -f docker-compose.prod.yml config

# Выполнение команды в контейнере
docker compose -f docker-compose.prod.yml exec [service-name] [command]
```

---

## Поддержка

При возникновении проблем:

1. Проверьте логи: `docker compose -f docker-compose.prod.yml logs`
2. Проверьте использование ресурсов: `docker stats`
3. Проверьте статус сервисов: `docker compose -f docker-compose.prod.yml ps`
4. Проверьте конфигурацию: `docker compose -f docker-compose.prod.yml config`

---

## Чеклист деплоя

- [ ] VPS подготовлен и обновлен
- [ ] Docker и Docker Compose установлены
- [ ] Firewall настроен (порты 22, 80, 443 открыты)
- [ ] DNS записи настроены и распространились
- [ ] SSL сертификаты получены и скопированы
- [ ] `.env` файл создан и заполнен
- [ ] nginx конфигурация обновлена для SSL
- [ ] Приложение собрано и запущено
- [ ] Все сервисы работают (проверка через `docker compose ps`)
- [ ] API доступны через домены
- [ ] Веб-интерфейсы открываются в браузере
- [ ] Резервное копирование настроено
- [ ] Мониторинг настроен

**Поздравляем! Ваше приложение развернуто в продакшене! 🎉**

