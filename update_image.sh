#!/bin/bash
# Небольшой скриптик для обновления образа в harbor требуется export $HARBOR_USERNAME и $HARBOR_HOST
set -e

HARBOR_HOST="sshubuntu.ru"
HARBOR_PROJECT="web-lab4"
IMAGE_NAME="wildfly-app"

if [ -d .git ]; then
    TAG=$(git rev-parse --short HEAD)
else
    TAG=$(date +%Y%m%d-%H%M%S)
fi

FULL_IMAGE="$HARBOR_HOST/$HARBOR_PROJECT/$IMAGE_NAME:$TAG"

echo " Building and pushing image: $FULL_IMAGE"

echo "$HARBOR_PASSWORD" | docker login "$HARBOR_HOST" -u "$HARBOR_USERNAME" --password-stdin


docker build -t "$FULL_IMAGE" .
docker push "$FULL_IMAGE"

echo "Updating Kubernetes deployment..."
kubectl set image deployment/wildfly "*=$FULL_IMAGE" -n wildfly-app
echo "Done! Deployed $FULL_IMAGE"