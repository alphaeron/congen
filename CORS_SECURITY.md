# CORS Security Configuration

## Overview
This document outlines the production-ready CORS (Cross-Origin Resource Sharing) configuration for the Congen application.

## Security Features Implemented

### 1. Environment-Based Configuration
- **Local Development**: Allows HTTP origins for development
- **Staging**: Allows both HTTP (localhost) and HTTPS origins
- **Production**: Enforces HTTPS-only origins

### 2. Origin Validation
- Whitelist-based origin validation
- Automatic HTTPS enforcement in production
- Detailed logging of CORS violations with User-Agent information

### 3. Rate Limiting
- Rate limiting for CORS violations (10 violations per IP per 5 minutes)
- Automatic cleanup of old violation records
- 429 Too Many Requests response for rate limit violations

### 4. Security Headers
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Referrer-Policy: strict-origin-when-cross-origin
- HSTS (production only)
- Content Security Policy (CSP)
- Permissions Policy (production only)

## Configuration

### Environment Variables
```bash
# CORS Configuration
CORS_ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Content-Type,Authorization,X-Requested-With
CORS_EXPOSED_HEADERS=Content-Type,Content-Range
CORS_MAX_AGE=3600

# Environment
SPRING_PROFILES_ACTIVE=prod
```

### Production Configuration
```properties
# application-prod.properties
cors.allowed-origins=https://your-production-domain.com,https://www.your-production-domain.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=Content-Type,Authorization,X-Requested-With
cors.exposed-headers=Content-Type,Content-Range
cors.max-age=3600
congen.postgres.ssl-mode=true
```

### Staging Configuration
```properties
# application-staging.properties
cors.allowed-origins=https://staging.your-domain.com,http://localhost:3000
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=Content-Type,Authorization,X-Requested-With
cors.exposed-headers=Content-Type,Content-Range
cors.max-age=7200
congen.postgres.ssl-mode=true
```

## Deployment Checklist

### Pre-Production
- [ ] Update `cors.allowed-origins` with actual production domains
- [ ] Ensure all production domains use HTTPS
- [ ] Test CORS configuration in staging environment
- [ ] Verify rate limiting behavior
- [ ] Review security headers in browser dev tools

### Production Deployment
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure environment variables
- [ ] Monitor CORS violation logs
- [ ] Set up alerts for rate limit violations
- [ ] Verify HTTPS enforcement

### Monitoring
- [ ] Monitor CORS violation logs
- [ ] Track rate limit violations
- [ ] Monitor security header compliance
- [ ] Set up alerts for suspicious CORS activity

## Security Considerations

### HTTPS Enforcement
- Production environments automatically reject HTTP origins
- HSTS header prevents downgrade attacks
- SSL/TLS termination should be configured at load balancer

### Origin Validation
- Only whitelisted origins are allowed
- No wildcard origins in production
- Subdomain validation if needed

### Rate Limiting
- Prevents CORS-based DoS attacks
- Configurable limits per environment
- Automatic cleanup prevents memory leaks

### Headers Security
- Minimal exposed headers
- Strict Content Security Policy
- Frame-ancestors: 'none' prevents clickjacking

## Troubleshooting

### Common Issues
1. **CORS violations in logs**: Check if frontend origin is in allowed list
2. **Rate limit errors**: Check for malicious requests or misconfigured frontend
3. **HTTPS errors in production**: Ensure all origins use HTTPS

### Debug Mode
Enable debug logging for CORS:
```properties
logging.level.com.congen.components.CorsFilter=DEBUG
logging.level.com.congen.components.CorsRateLimitFilter=DEBUG
```

## Best Practices

1. **Principle of Least Privilege**: Only allow necessary origins, methods, and headers
2. **Environment Separation**: Different configurations for dev/staging/prod
3. **Monitoring**: Log and monitor CORS violations
4. **Regular Review**: Periodically review allowed origins and security settings
5. **Documentation**: Keep this document updated with any changes 