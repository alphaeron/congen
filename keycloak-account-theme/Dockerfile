FROM alpine:latest

# Install unzip for extracting the JAR
RUN apk add --no-cache unzip

# Copy the theme JAR file
COPY build_keycloak/keycloak-theme-for-kc-all-other-versions.jar /theme/congen-account-theme.jar

# Set working directory
WORKDIR /theme

# Default command (will be overridden by the init container)
CMD ["sh", "-c", "echo 'Theme JAR ready for extraction'"]
