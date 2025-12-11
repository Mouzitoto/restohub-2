FROM nginx:alpine

# Устанавливаем gettext для envsubst и wget для healthcheck
RUN apk add --no-cache gettext wget

# Копируем entrypoint скрипт
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

