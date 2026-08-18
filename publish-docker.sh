#!/usr/bin/env bash
set -e

# Read version from gradle.properties or command line argument
VERSION=${1:-$(grep "app.version" gradle.properties | cut -d'=' -f2 | tr -d ' ')}
DOCKER_USER=${DOCKER_USER:-""}

if [ -z "$DOCKER_USER" ]; then
  read -p "Enter your Docker Hub username: " DOCKER_USER
fi

if [ -z "$VERSION" ]; then
  echo "Error: Version could not be determined."
  exit 1
fi

IMAGE_VERSION="${DOCKER_USER}/memebattle-web:${VERSION}"
IMAGE_CURRENT="${DOCKER_USER}/memebattle-web:current"

echo "=================================================="
echo " Building MemeBattle Web Docker Image"
echo " Version: ${VERSION}"
echo " Image Tags:"
echo "   - ${IMAGE_VERSION}"
echo "   - ${IMAGE_CURRENT}"
echo "=================================================="

# Build Docker image with both version tag and 'current' tag
docker build -t "${IMAGE_VERSION}" -t "${IMAGE_CURRENT}" .

echo ""
echo "Pushing images to Docker Hub..."
docker push "${IMAGE_VERSION}"
docker push "${IMAGE_CURRENT}"

echo ""
echo "Successfully published:"
echo "  - ${IMAGE_VERSION}"
echo "  - ${IMAGE_CURRENT}"
