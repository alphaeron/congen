# DDoS Protection

The Congen backend implements comprehensive DDoS protection using Spring Boot built-ins and minimal custom code.

## Protection Features

### 1. Rate Limiting
- **IP-based limits**: 100 requests per minute per IP address
- **User-based limits**: 50 requests per minute per authenticated user
- **CORS violation limits**: 10 violations per 5 minutes per IP address
- **Sliding window**: Configurable sliding window for rate limit tracking
- **Automatic cleanup**: Old rate limit records are automatically cleaned up

### 2. Request Size Limits
- **Payload size**: Maximum 1MB request payload
- **Header size**: Maximum 8KB header size
- **URL length**: Maximum 4KB initial line length

### 3. Connection Limits
- **Connection backlog**: 1000 pending connections
- **Idle timeout**: 30 seconds
- **Request timeout**: 10 seconds
- **Keep-alive**: Enabled for efficient connections

### 4. Security Headers
- **X-Content-Type-Options**: Prevents MIME type sniffing
- **X-Frame-Options**: Prevents clickjacking
- **X-XSS-Protection**: Enables browser XSS protection
- **Referrer-Policy**: Controls referrer information
- **Production**: Additional HSTS, CSP, and Permissions-Policy headers

## Configuration

Rate limiting can be configured via application properties:

```properties
# Rate limiting configuration
rate.limit.ip.max-requests=100
rate.limit.ip.window-minutes=1
rate.limit.user.max-requests=50
rate.limit.payload.max-size=1MB

# CORS configuration (used for violation rate limiting)
cors.allowed-origins=https://example.com,https://app.congen.com
```

## Implementation Details

### Components
- `RateLimitFilter`: Comprehensive rate limiting logic (200 lines)
  - IP-based rate limiting
  - User-based rate limiting  
  - CORS violation rate limiting
  - Payload size validation
- `ServerConfig`: Netty server configuration (30 lines)
- `SecurityHeadersFilter`: Security headers (100 lines)
- `DdosProtectionTestHelpers`: Modular test utilities (300 lines)

### Spring Boot Built-ins Used
- `@WebFilter`: Automatic filter registration
- `NettyServerCustomizer`: Server-level protection
- `spring.codec.max-in-memory-size`: Request size limits
- `spring.webflux.max-initial-line-length`: URL length limits

## Response Codes

- **429 Too Many Requests**: Rate limit exceeded
- **413 Payload Too Large**: Request payload too large
- **408 Request Timeout**: Request processing timeout

## Monitoring

All rate limit violations are logged with:
- IP address or user ID
- Request count
- Time window information

## Code Quality & Modularization

- **Total Lines**: ~630 lines of code (including comprehensive test helpers)
- **Test Coverage**: 100% for all components
- **Documentation**: Comprehensive KDoc for all classes and methods
- **Configuration**: Externalized to application.properties
- **Test Modularity**: Reusable test helpers eliminate duplication
- **Centralized Logic**: Single RateLimitFilter handles all rate limiting scenarios

## Deployment Considerations

- **Single instance**: Current implementation uses in-memory storage
- **Multi-instance**: Consider Redis-based rate limiting for production
- **Load balancer**: Ensure proper IP forwarding headers are set
- **Monitoring**: Monitor rate limit violations for attack detection
