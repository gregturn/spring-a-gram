#!/usr/bin/env bash

# delete apps
cf delete spring-a-gram -f
cf delete spring-a-gram-backend -f
cf delete spring-a-gram-mongodb-fileservice -f
cf delete spring-a-gram-s3-fileservice -f

# delete services
cf delete-service spring-a-gram-service-registry -f
cf delete-service spring-a-gram-circuit-breaker-dashboard -f
cf delete-service spring-a-gram-config-server -f
