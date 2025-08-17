# DAL Caching Guide

This guide explains how to use the Spring AOP-based caching system for Data Access Layers (DALs) in the Congen application.

## Overview

The caching system provides transparent caching for DAL methods using Spring AOP with minimal impact on existing code. It integrates with the existing Memcached infrastructure and provides declarative caching through annotations.

## Key Features

- **Transparent Operation**: Services work without any caching-related code changes
- **Declarative Configuration**: Use annotations to mark cached methods
- **Automatic Invalidation**: Cache entries are invalidated on write operations
- **Flexible TTL**: Different TTL values for different types of data
- **Custom Key Strategies**: Various key generation strategies for different use cases
- **Error Handling**: Graceful handling of cache misses and errors
- **Fully Reactive**: No blocking operations, compatible with Spring WebFlux

## Architecture

```
Service Layer
    ↓ (calls DAL methods)
DAL Layer (with @Cacheable/@CacheEvict annotations)
    ↓ (intercepted by AOP)
DALCachingAspect
    ↓ (uses)
ReactiveMemcachedCache
    ↓ (stores in)
Memcached
```

## Annotations

### @Cacheable

Use this annotation on read operations to cache the results.

```kotlin
@Cacheable(
    ttl = CacheTTL.LONG_TERM,
    keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
    entityName = "exercise"
)
fun selectExerciseByName(exerciseName: String): Mono<Exercise>
```

**Parameters:**
- `ttl`: Time-to-live duration (see TTL values below)
- `keyStrategy`: Strategy for generating cache keys
- `invalidationStrategy`: Strategy for invalidating related cache entries
- `entityName`: Name of the entity (optional, auto-detected if not provided)

### @CacheEvict

Use this annotation on write operations to invalidate related cache entries.

```kotlin
@CacheEvict(
    invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
    entityName = "exercise"
)
fun insertExercise(name: String, ...): Mono<Exercise>
```

**Parameters:**
- `invalidationStrategy`: Strategy for invalidating related cache entries
- `entityName`: Name of the entity (optional, auto-detected if not provided)

## TTL Values

| TTL | Duration | Use Case |
|-----|----------|----------|
| `LONG_TERM` | 24 hours | Reference data (exercises, equipment, muscles) |
| `MEDIUM_TERM` | 1 hour | Relationship data and frequently accessed lists |
| `SHORT_TERM` | 30 minutes | Individual records and moderate-frequency queries |
| `VERY_SHORT_TERM` | 5 minutes | High-frequency list queries |
| `USER_DATA` | 30 minutes | User-specific data that changes frequently |

## Key Strategies

### CacheKeyStrategy

| Strategy | Pattern | Use Case |
|----------|---------|----------|
| `STANDARD` | `entityName:methodName:param1:param2` | General purpose |
| `ENTITY_BY_NAME` | `entityName:byName:entityName` | Entities with name-based primary keys |
| `USER_SPECIFIC` | `entityName:user:userId:methodName:params` | User-specific data |
| `RELATIONSHIP` | `entityName:methodName:param1:param2` | Relationship tables |
| `LIST_QUERY` | `entityName:list:methodName:params` | List queries |

### CacheInvalidationStrategy

| Strategy | Invalidation Pattern | Use Case |
|----------|---------------------|----------|
| `STANDARD` | `entityName:*` | General entity invalidation |
| `ENTITY_BY_NAME` | `entityName:byName:entityName`, `entityName:*` | Name-based entities |
| `USER_DATA` | `entityName:user:userId:*`, `entityName:*` | User-specific data |
| `RELATIONSHIP` | `entityName:*`, `entityName:*:params` | Relationship tables |
| `LIST_QUERIES` | `entityName:list:*`, `entityName:*` | List query invalidation |

## Usage Examples

### Exercise DAL

```kotlin
@Component
class ExerciseDAL(private val postgresClient: PostgresClient) {
    
    // Cache individual exercises for 24 hours
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun selectExerciseByName(exerciseName: String): Mono<Exercise> {
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise WHERE name=$1",
            exerciseName
        )
    }
    
    // Cache all exercises list for 24 hours
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "exercise"
    )
    fun selectExercises(): Mono<List<Exercise>> {
        return postgresClient.select("SELECT * FROM exercise")
    }
    
    // Invalidate cache when inserting new exercise
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun insertExercise(name: String, ...): Mono<Exercise> {
        // Implementation
    }
}
```

### User DAL

```kotlin
@Component
class UserDAL(private val postgresClient: PostgresClient) {
    
    // Cache user data for 30 minutes (user-specific)
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user"
    )
    fun selectUserByKeycloakId(keycloakId: String): Mono<User> {
        return postgresClient.selectIndividual(
            "SELECT * FROM user WHERE keycloak_id=$1",
            keycloakId
        )
    }
    
    // Invalidate user cache when updating user
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user"
    )
    fun updateUser(keycloakId: String, ...): Mono<User> {
        // Implementation
    }
}
```

### Relationship DAL

```kotlin
@Component
class ExerciseMuscleDAL(private val postgresClient: PostgresClient) {
    
    // Cache relationship data for 1 hour
    @Cacheable(
        ttl = CacheTTL.MEDIUM_TERM,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "exercise_muscle"
    )
    fun selectExerciseMuscleByExercise(exerciseName: String): Mono<List<ExerciseMuscle>> {
        return postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseName
        )
    }
    
    // Invalidate relationship cache when modifying relationships
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "exercise_muscle"
    )
    fun insertExerciseMuscle(exerciseName: String, muscleName: String): Mono<ExerciseMuscle> {
        // Implementation
    }
}
```

## Best Practices

### 1. Choose Appropriate TTL Values

- Use `LONG_TERM` for reference data that rarely changes
- Use `MEDIUM_TERM` for relationship data
- Use `SHORT_TERM` for individual records
- Use `USER_DATA` for user-specific information
- Use `VERY_SHORT_TERM` for high-frequency queries

### 2. Select the Right Key Strategy

- Use `ENTITY_BY_NAME` for entities with name-based primary keys
- Use `USER_SPECIFIC` for user-related data
- Use `RELATIONSHIP` for relationship tables
- Use `LIST_QUERY` for list operations
- Use `STANDARD` for general purpose

### 3. Configure Proper Invalidation

- Always add `@CacheEvict` to write operations
- Choose the right invalidation strategy to avoid cache inconsistencies
- Consider the scope of invalidation (specific vs. broad)

### 4. Monitor Cache Performance

- Check cache hit rates in logs
- Monitor cache memory usage
- Adjust TTL values based on access patterns

## Configuration

The caching system is automatically configured when you include the `CachingConfig` class:

```kotlin
@Configuration
@EnableAspectJAutoProxy
class CachingConfig
```

## Logging

The caching system provides comprehensive logging:

- `DEBUG`: Cache lookups, hits, misses, and operations
- `WARN`: Failed cache operations
- `ERROR`: Cache errors and exceptions

Enable debug logging to monitor cache behavior:

```properties
logging.level.com.congen.cache=DEBUG
```

## Error Handling

The caching system handles errors gracefully:

- **Cache Misses**: Automatically fall back to database queries
- **Cache Errors**: Log errors but don't fail the application
- **Network Issues**: Continue operation without caching

## Testing

The caching system includes comprehensive tests:

- `DALCachingAspectTest.kt` - Tests for the AOP aspect functionality
- `CachingComponentsTest.kt` - Unit tests for annotations and enums

These tests verify:

- Cache hits and misses
- TTL handling
- Key generation strategies
- Cache invalidation
- Error scenarios
- Annotation and enum functionality

## Migration Guide

To add caching to existing DALs:

1. **Add imports**:
   ```kotlin
   import com.congen.cache.annotation.Cacheable
   import com.congen.cache.annotation.CacheEvict
   import com.congen.cache.CacheTTL
   import com.congen.cache.CacheKeyStrategy
   import com.congen.cache.CacheInvalidationStrategy
   ```

2. **Add @Cacheable to read methods**:
   ```kotlin
   @Cacheable(ttl = CacheTTL.LONG_TERM, keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME)
   fun selectExerciseByName(exerciseName: String): Mono<Exercise>
   ```

3. **Add @CacheEvict to write methods**:
   ```kotlin
   @CacheEvict(invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME)
   fun insertExercise(name: String, ...): Mono<Exercise>
   ```

4. **Test thoroughly** to ensure cache behavior is correct

## Troubleshooting

### Common Issues

1. **Cache not working**: Ensure `@EnableAspectJAutoProxy` is configured
2. **Wrong cache keys**: Check the key strategy and entity name
3. **Stale data**: Verify invalidation strategies are correct
4. **Performance issues**: Adjust TTL values based on access patterns

### Debug Commands

To debug cache behavior, enable debug logging and check:

- Cache key generation
- Cache hit/miss rates
- TTL values
- Invalidation patterns

## Performance Considerations

- **Memory Usage**: Monitor Memcached memory consumption
- **Network Latency**: Consider cache locality for distributed systems
- **Cache Warming**: Pre-populate frequently accessed data
- **TTL Optimization**: Balance freshness vs. performance

## Security Considerations

- **Cache Keys**: Ensure no sensitive data in cache keys
- **User Data**: Use appropriate TTL for user-specific data
- **Access Control**: Verify cache doesn't bypass security checks
