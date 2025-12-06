#!/bin/bash

# Скрипт для сборки и пуша Docker образов в Docker Hub
# Использование: ./build-and-push.sh [version]
# Пример: ./build-and-push.sh 1.0.0

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Проверка переменных окружения
if [ -z "$DOCKER_HUB_USERNAME" ]; then
    echo -e "${RED}Ошибка: Установите переменную DOCKER_HUB_USERNAME${NC}"
    echo "Пример: export DOCKER_HUB_USERNAME=your-username"
    exit 1
fi

if [ -z "$DOCKER_HUB_REPO" ]; then
    echo -e "${RED}Ошибка: Установите переменную DOCKER_HUB_REPO${NC}"
    echo "Пример: export DOCKER_HUB_REPO=resto-hub-repo"
    exit 1
fi

# Версия образа (по умолчанию latest)
VERSION=${1:-latest}

IMAGE_BASE="$DOCKER_HUB_USERNAME/$DOCKER_HUB_REPO"

echo -e "${GREEN}=== Сборка и публикация образов Resto-Hub ===${NC}"
echo -e "Docker Hub пользователь: ${YELLOW}$DOCKER_HUB_USERNAME${NC}"
echo -e "Docker Hub репозиторий: ${YELLOW}$DOCKER_HUB_REPO${NC}"
echo -e "Версия: ${YELLOW}$VERSION${NC}"
echo ""

# Переход в корень проекта
cd "$(dirname "$0")/.."

# Проверка авторизации в Docker Hub
echo -e "${GREEN}Проверка авторизации в Docker Hub...${NC}"
if ! docker info | grep -q "Username"; then
    echo -e "${YELLOW}Требуется авторизация в Docker Hub${NC}"
    docker login
fi

# Сборка и публикация admin-api
echo -e "\n${GREEN}[1/5] Сборка admin-api...${NC}"
docker build -t ${IMAGE_BASE}:admin-api-${VERSION} -t ${IMAGE_BASE}:admin-api-latest ./admin-api
echo -e "${GREEN}[1/5] Публикация admin-api...${NC}"
docker push ${IMAGE_BASE}:admin-api-${VERSION}
docker push ${IMAGE_BASE}:admin-api-latest

# Сборка и публикация client-api
echo -e "\n${GREEN}[2/5] Сборка client-api...${NC}"
docker build -t ${IMAGE_BASE}:client-api-${VERSION} -t ${IMAGE_BASE}:client-api-latest ./client-api
echo -e "${GREEN}[2/5] Публикация client-api...${NC}"
docker push ${IMAGE_BASE}:client-api-${VERSION}
docker push ${IMAGE_BASE}:client-api-latest

# Сборка и публикация client-web
echo -e "\n${GREEN}[3/5] Сборка client-web...${NC}"
docker build \
    --build-arg VITE_API_BASE_URL=${VITE_API_BASE_URL:-https://api.restohub.kz} \
    --build-arg VITE_PARTNER_DOMAIN=${VITE_PARTNER_DOMAIN:-https://partner.restohub.kz} \
    -t ${IMAGE_BASE}:client-web-${VERSION} \
    -t ${IMAGE_BASE}:client-web-latest \
    ./client-web
echo -e "${GREEN}[3/5] Публикация client-web...${NC}"
docker push ${IMAGE_BASE}:client-web-${VERSION}
docker push ${IMAGE_BASE}:client-web-latest

# Сборка и публикация admin-web
echo -e "\n${GREEN}[4/5] Сборка admin-web...${NC}"
docker build \
    --build-arg VITE_API_BASE_URL=${VITE_API_BASE_URL:-https://api.restohub.kz} \
    -t ${IMAGE_BASE}:admin-web-${VERSION} \
    -t ${IMAGE_BASE}:admin-web-latest \
    ./admin-web
echo -e "${GREEN}[4/5] Публикация admin-web...${NC}"
docker push ${IMAGE_BASE}:admin-web-${VERSION}
docker push ${IMAGE_BASE}:admin-web-latest

# Сборка и публикация nginx
echo -e "\n${GREEN}[5/5] Сборка nginx...${NC}"
docker build -t ${IMAGE_BASE}:nginx-${VERSION} -t ${IMAGE_BASE}:nginx-latest ./nginx
echo -e "${GREEN}[5/5] Публикация nginx...${NC}"
docker push ${IMAGE_BASE}:nginx-${VERSION}
docker push ${IMAGE_BASE}:nginx-latest

echo -e "\n${GREEN}=== Все образы успешно собраны и опубликованы! ===${NC}"
echo -e "Версия: ${YELLOW}$VERSION${NC}"
echo -e "Docker Hub: ${YELLOW}https://hub.docker.com/r/$IMAGE_BASE${NC}"

