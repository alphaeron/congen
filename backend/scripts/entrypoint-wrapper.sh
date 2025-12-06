#!/bin/sh
set -e

# Source Vault secrets if they exist (written by Vault agent injector)
# These files contain exported environment variables that the application needs
if [ -f /vault/secrets/database ]; then
    . /vault/secrets/database
fi
if [ -f /vault/secrets/cache ]; then
    . /vault/secrets/cache
fi

# Execute the original Java command
# This preserves the original entrypoint behavior while allowing Vault secrets to be sourced first
exec java -XX:+UseG1GC \
    -Xms${JVM_HEAP_MIN:-256m} -Xmx${JVM_HEAP_MAX:-384m} \
    -XX:MetaspaceSize=${JVM_METASPACE_MIN:-64M} -XX:MaxMetaspaceSize=${JVM_METASPACE_MAX:-128M} \
    -XX:ActiveProcessorCount=2 \
    -Djava.security.edg=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} \
    -classpath /app/resources:/app/classes:/app/libs/* \
    com.congen.CongenApplicationKt "$@"
