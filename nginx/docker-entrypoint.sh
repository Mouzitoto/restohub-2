#!/bin/sh
set -e

# Подставляем переменные окружения в nginx конфигурацию
envsubst '${CLIENT_DOMAIN} ${PARTNER_DOMAIN} ${API_DOMAIN}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Запускаем nginx
exec nginx -g 'daemon off;'

