#!/bin/bash -ex

VERSION=$(jq -r '.version' package.json)
HASH=$(git rev-parse --short HEAD)
TAG="${VERSION}.${HASH}"
IMAGE="congen-ui:${TAG}"

npm run build

docker build -t ${IMAGE} .
