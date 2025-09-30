#!/bin/bash -ex

docker pull postgres:15-alpine
docker pull mirror.gcr.io/busybox:1.35
docker pull memcached:1.6.39-alpine
docker pull stakater/reloader:v1.0.46
docker pull sfat/liquibase:4.11.0
docker pull quay.io/keycloak/keycloak:26.3.2
docker pull testcontainers/ryuk:0.6.0
docker pull gcr.io/k8s-minikube/kicbase:v0.0.45
docker pull amazoncorretto:17.0.11-alpine3.19
