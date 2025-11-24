#!/bin/bash
# Универсальный скрипт для настройки hosts файла (Linux/macOS)

HOSTS_FILE="/etc/hosts"
DOMAINS=(
    "127.0.0.1 restohub.local"
    "127.0.0.1 partner.restohub.local"
    "127.0.0.1 api.restohub.local"
)

echo "Настройка локальных доменов для resto-hub..."

# Проверяем права root
if [ "$EUID" -ne 0 ]; then 
    echo "ОШИБКА: Скрипт должен быть запущен с правами root (sudo)"
    echo "Запустите: sudo ./setup-hosts.sh"
    exit 1
fi

# Создаем резервную копию
BACKUP_FILE="${HOSTS_FILE}.backup.$(date +%Y%m%d-%H%M%S)"
cp "$HOSTS_FILE" "$BACKUP_FILE"
echo "Создана резервная копия: $BACKUP_FILE"

# Добавляем домены
for domain in "${DOMAINS[@]}"; do
    domain_name=$(echo "$domain" | awk '{print $2}')
    
    # Проверяем, существует ли уже запись
    if grep -q "$domain_name" "$HOSTS_FILE"; then
        echo "Домен $domain_name уже настроен"
    else
        echo "$domain" >> "$HOSTS_FILE"
        echo "Добавлен домен: $domain_name"
    fi
done

echo ""
echo "Настройка завершена!"
echo "Теперь вы можете использовать:"
echo "  - http://restohub.local"
echo "  - http://partner.restohub.local"
echo "  - http://api.restohub.local"

