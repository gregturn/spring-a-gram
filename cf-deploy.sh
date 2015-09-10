#!/usr/bin/env bash

# create services
cf create-service p-circuit-breaker-dashboard standard spring-a-gram-circuit-breaker-dashboard
cf create-service p-service-registry standard spring-a-gram-service-registry
cf create-service p-config-server standard spring-a-gram-config-server

# build apps
mvn clean package -f spring-a-gram-frontend/
mvn clean package -f spring-a-gram-backend/
mvn clean package -f spring-a-gram-mongodb-fileservice/
mvn clean package -f spring-a-gram-s3-fileservice/

# deploy apps via manifest.yml
cf push
