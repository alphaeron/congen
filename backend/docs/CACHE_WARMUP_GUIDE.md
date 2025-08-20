# Cache Warmup Guide

This guide explains the cache warmup functionality in the Congen application, which pre-loads important cache data before the application starts serving requests.

## Overview

The cache warmup service improves application performance by pre-loading frequently accessed data into the cache during application startup. This reduces latency for the first requests after deployment and ensures a consistent user experience.

## Architecture

```
Application Startup
    ↓
CacheWarmupService (ApplicationRunner)
    ↓
DAL Method Calls (with @Cacheable annotations)
    ↓
DALCachingAspect (AOP)
    ↓
ReactiveMemcachedCache
    ↓
Memcached
```

## Features

- **Automatic Execution**: Runs automatically after application context is ready
- **Configurable**: Can be enabled/disabled and customized via properties
- **Non-blocking**: Uses reactive programming to avoid blocking startup
- **Error Resilient**: Continues warmup even if individual operations fail
- **Performance Focused**: Targets the most impactful data for user experience

## Warmup Strategy

The service warms up data in the following priority order:

### 1. Reference Data (LONG_TERM TTL)
- **Exercises**: Popular compound movements (Bench Press, Squat, Deadlift, etc.)
- **Equipment**: Common gym equipment (Barbell, Dumbbell, etc.)
- **Muscles**: Major muscle groups (Chest, Back, Legs, etc.)

### 2. Frequently Accessed Lists
- All exercises list
- All equipment list
- All muscles list
- Workout stage types list

### 3. Core Relationships
- Exercise-muscle relationships for popular exercises
- Exercise-equipment relationships for popular exercises
- Exercise-workout type relationships for popular exercises

### 4. Program Data
- Basic program information

## Configuration

### Properties

Configure the cache warmup behavior using the following properties:

```properties
# Enable/disable cache warmup
congen.cache.warmup.enabled=true

# Enable/disable specific warmup sections
congen.cache.warmup.warmup-reference-data=true
congen.cache.warmup.warmup-lists=true
congen.cache.warmup.warmup-relationships=true
congen.cache.warmup.warmup-programs=true

# Configure popular items to warm up
congen.cache.warmup.popular-exercises=Bench Press,Squat,Deadlift,Overhead Press,Pull-up
congen.cache.warmup.popular-equipment=Barbell,Dumbbell,Pull-up Bar,Bench,Squat Rack
congen.cache.warmup.popular-muscles=Chest,Back,Legs,Shoulders,Arms,Core
```

### Default Values

If no configuration is provided, the following defaults are used:

- **enabled**: `true`
- **warmup-reference-data**: `true`
- **warmup-lists**: `true`
- **warmup-relationships**: `true`
- **warmup-programs**: `true`
- **popular-exercises**: `["Bench Press", "Back Squat", "Deadlift", "Overhead Press", "Chin-Up", "TRX Push-Up", "Bent-Over Row", "Split Squat", "Front Squat", "Landmine Row"]`
- **popular-equipment**: `["power bar", "dumbbells", "pull-up bar", "bench", "power rack"]`
- **popular-muscles**: `["pec major", "lats", "quadriceps", "anterior deltoid", "biceps", "rectus abdominis"]`

## Usage Examples

### Basic Configuration

Enable cache warmup with default settings:

```properties
congen.cache.warmup.enabled=true
```

### Custom Popular Items

Configure specific exercises, equipment, and muscles to warm up:

```properties
congen.cache.warmup.popular-exercises=Bench Press,Squat,Deadlift,Overhead Press
congen.cache.warmup.popular-equipment=Barbell,Dumbbell
congen.cache.warmup.popular-muscles=Chest,Back,Legs
```

### Selective Warmup

Enable only specific warmup sections:

```properties
congen.cache.warmup.warmup-reference-data=true
congen.cache.warmup.warmup-lists=true
congen.cache.warmup.warmup-relationships=false
congen.cache.warmup.warmup-programs=false
```

### Disable Warmup

Completely disable cache warmup:

```properties
congen.cache.warmup.enabled=false
```

## Performance Impact

### Benefits

- **Reduced Latency**: First requests after deployment are faster
- **Consistent Performance**: Eliminates cold start performance issues
- **Better User Experience**: Users don't experience slow initial loads
- **Predictable Response Times**: Consistent performance across deployments

### Considerations

- **Startup Time**: Adds a small delay to application startup (typically 1-5 seconds)
- **Database Load**: Increases initial database load during startup
- **Cache Memory**: Uses additional cache memory for pre-loaded data
- **Network Traffic**: Generates additional network traffic to Memcached

### Monitoring

The service provides comprehensive logging:

```
INFO  - Starting cache warmup process
DEBUG - Warming up reference data (popular exercises, equipment, muscles)
DEBUG - Warmed up exercise: Bench Press
DEBUG - Warmed up equipment: Barbell
DEBUG - Warmed up muscle: Chest
INFO  - Warming up frequently accessed lists
DEBUG - Warmed up exercises list
INFO  - Warming up core relationship data
DEBUG - Warmed up exercise-muscle relationships for: Bench Press
INFO  - Warming up program data
DEBUG - Warmed up programs list
INFO  - Cache warmup completed successfully in 2345 ms
```

## Error Handling

The warmup service is designed to be resilient:

- **Individual Failures**: If a single warmup operation fails, others continue
- **Graceful Degradation**: Application starts normally even if warmup fails
- **Error Logging**: All errors are logged for monitoring and debugging
- **Non-blocking**: Startup is not blocked by warmup failures

## Testing

### Unit Tests

The service includes comprehensive unit tests:

- `CacheWarmupServiceTest`: Tests warmup functionality with mocked dependencies
- `CacheWarmupConfigTest`: Tests configuration property loading
- Error handling and configuration scenarios

### Integration Tests

Integration tests verify the service works with real application context:

- `CacheWarmupServiceIntegrationTest`: Tests with actual database and cache
- Verifies warmup completes successfully
- Tests error handling with missing data

## Troubleshooting

### Common Issues

1. **Warmup Takes Too Long**
   - Check database performance
   - Review popular items list (too many items)
   - Verify Memcached connectivity

2. **Warmup Fails**
   - Check database connectivity
   - Verify Memcached is running
   - Review application logs for specific errors

3. **No Warmup Logs**
   - Verify `congen.cache.warmup.enabled=true`
   - Check application startup logs
   - Ensure service is properly configured

### Debug Mode

Enable debug logging for detailed warmup information:

```properties
logging.level.com.congen.service.CacheWarmupService=DEBUG
```

## Best Practices

### 1. Choose Popular Items Wisely

- Focus on the most frequently accessed data
- Include compound movements and major muscle groups
- Consider user behavior patterns

### 2. Monitor Performance

- Track warmup completion times
- Monitor cache hit rates after warmup
- Watch for database performance impact

### 3. Environment-Specific Configuration

- Use different popular items for different environments
- Adjust warmup sections based on usage patterns
- Consider disabling in development for faster startup

### 4. Regular Review

- Periodically review popular items list
- Update based on actual usage analytics
- Monitor cache effectiveness

## Migration Guide

### From No Warmup

1. Add configuration properties
2. Deploy and monitor performance
3. Adjust popular items based on usage

### From Manual Warmup

1. Disable manual warmup scripts
2. Configure automatic warmup
3. Verify same data is being warmed up

## Future Enhancements

Potential improvements for the cache warmup system:

- **Analytics-Driven**: Use actual usage data to determine popular items
- **Dynamic Configuration**: Update popular items without restart
- **Progressive Warmup**: Warm up data in stages based on priority
- **Health Checks**: Verify warmup effectiveness and cache health
- **Metrics**: Expose warmup metrics for monitoring
