# Congen API Documentation

This documentation is automatically generated from the OpenAPI specification.

## Overview

**Congen API** - # Conjugate Workout Generator API

A comprehensive REST API for managing workout programs, exercises, users, and preferences
using the conjugate method. This API provides endpoints for creating personalized
workout programs based on user preferences and available equipment.

## Key Features

- **User Management**: Create and manage user profiles with fitness preferences
- **Exercise Library**: Comprehensive database of exercises with equipment and muscle targeting
- **Program Generation**: Generate personalized workout programs using the conjugate method
- **Preference Management**: Store and retrieve user preferences for programs and exercises
- **Equipment Tracking**: Manage available equipment for users

## API Versioning

This API uses versioned endpoints with the base path `/api/v1/`. All endpoints
are automatically prefixed with this version path to ensure backward compatibility
and clear API versioning.

## Authentication

Currently, the API does not require authentication. All endpoints are publicly accessible.

## Rate Limiting

API requests are rate-limited to prevent DDoS attacks and ensure fair usage:
- **IP-based limits**: 100 requests per minute per IP address
- **User-based limits**: 50 requests per minute per authenticated user
- **Payload limits**: Maximum 1MB request size
- **Request timeouts**: 10-second request timeout

Please respect the rate limits and implement appropriate retry logic with exponential backoff.

## Error Handling

The API uses standard HTTP status codes and returns detailed error messages
in JSON format for validation and processing errors.

## Data Validation

All input data is validated according to business rules:
- Program days per week: 2, 3, or 4 days
- Exercise parameters: Valid ranges for reps, sets, weights, etc.

For detailed validation rules, see the individual endpoint documentation.
**Version:** 1.0.0

## Interactive Documentation

- **Swagger UI**: [http://localhost:8888/api/v1/swagger-ui.html](http://localhost:8888/api/v1/swagger-ui.html)
- **OpenAPI JSON**: [openapi.json](openapi.json)
- **OpenAPI YAML**: [openapi.yaml](openapi.yaml)

## API Endpoints

### /api/v1/user_weight_unit_preference/

#### PUT put
- **Path**: `put`
- **Description**: Creates a new user weight unit preference or updates an existing one (upsert operation).

### /api/v1/user_one_rep_max/

#### PUT put
- **Path**: `put`
- **Description**: No description available

### /api/v1/user_exercise_preference/

#### PUT put
- **Path**: `put`
- **Description**: Creates a new user exercise preference or updates an existing one (upsert operation).

### /api/v1/user_exercise_preference/

#### DELETE delete
- **Path**: `delete`
- **Description**: Deletes a user exercise preference relationship.

### /api/v1/performance/weekly_test

#### GET get
- **Path**: `get`
- **Description**: Retrieve all weekly tests within the specified date range.

### /api/v1/performance/weekly_test

#### PUT put
- **Path**: `put`
- **Description**: Submit results from the weekly testing protocol. Automatically integrates into performance tracking.

### /api/v1/performance/metrics

#### GET get
- **Path**: `get`
- **Description**: Retrieve raw performance test results and wearable data.

### /api/v1/performance/metrics

#### PUT put
- **Path**: `put`
- **Description**: Submit performance test results and wearable data. Automatically calculates updated scores.

### /api/v1/admin/data_retention/policies

#### GET get
- **Path**: `get`
- **Description**: Retrieves current TTL policies for all data types including audit logs, consent records, etc.

### /api/v1/admin/data_retention/policies

#### PUT put
- **Path**: `put`
- **Description**: Updates the TTL policy for a specific data type. Changes take effect on the next scheduled cleanup.

### /api/v1/workout_stage/

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/workout_stage/

#### POST post
- **Path**: `post`
- **Description**: No description available

### /api/v1/workout_stage/

#### PATCH patch
- **Path**: `patch`
- **Description**: No description available

### /api/v1/user_weak_muscle/

#### POST post
- **Path**: `post`
- **Description**: Adds a weak muscle group for a user.

### /api/v1/user_weak_muscle/

#### DELETE delete
- **Path**: `delete`
- **Description**: Deletes a weak muscle group for a user.

### /api/v1/user_equipment/

#### POST post
- **Path**: `post`
- **Description**: Creates a new user-equipment relationship.

### /api/v1/user_equipment/

#### DELETE delete
- **Path**: `delete`
- **Description**: Deletes a user-equipment relationship.

### /api/v1/user_equipment/bulk

#### POST post
- **Path**: `post`
- **Description**: Creates multiple user-equipment relationships in a single request.

### /api/v1/user/

#### POST post
- **Path**: `post`
- **Description**: Creates a user profile using information automatically extracted from the JWT token. The user must be authenticated and the profile will be linked to their Keycloak user ID.

### /api/v1/set_scheme/

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/set_scheme/

#### POST post
- **Path**: `post`
- **Description**: No description available

### /api/v1/programmed_workout/

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_workout/

#### POST post
- **Path**: `post`
- **Description**: No description available

### /api/v1/programmed_exercise/

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_exercise/

#### POST post
- **Path**: `post`
- **Description**: No description available

### /api/v1/program/

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all programs.

### /api/v1/program/

#### POST post
- **Path**: `post`
- **Description**: Creates a new program for a user.

### /api/v1/muscle/

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all muscles.

### /api/v1/muscle/

#### POST post
- **Path**: `post`
- **Description**: Creates a new muscle entry.

### /api/v1/gdpr/consent

#### GET get
- **Path**: `get`
- **Description**: Retrieves the current consent status for data processing for the authenticated user.

### /api/v1/gdpr/consent

#### POST post
- **Path**: `post`
- **Description**: Records user consent for data processing under GDPR Article 6. Consent must be freely given, specific, informed, and unambiguous.

### /api/v1/exercise_workout_type/

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercise-workout type relationships.

### /api/v1/exercise_workout_type/

#### POST post
- **Path**: `post`
- **Description**: Creates a new exercise-workout type relationship.

### /api/v1/exercise_muscle/

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercise-muscle relationships.

### /api/v1/exercise_muscle/

#### POST post
- **Path**: `post`
- **Description**: Creates a new exercise-muscle relationship.

### /api/v1/exercise_equipment/

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercise-equipment relationships.

### /api/v1/exercise_equipment/

#### POST post
- **Path**: `post`
- **Description**: Creates a new exercise-equipment relationship.

### /api/v1/exercise/

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all exercises.

### /api/v1/exercise/

#### POST post
- **Path**: `post`
- **Description**: Creates a new exercise entry.

### /api/v1/equipment/

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all equipment.

### /api/v1/equipment/

#### POST post
- **Path**: `post`
- **Description**: Creates a new equipment entry.

### /api/v1/conjugate_workout_generator/{program_id}

#### POST post
- **Path**: `post`
- **Description**: Generates the next week of workouts for an existing conjugate powerlifting program. The week number is automatically determined from the program's current week number.

### /api/v1/admin/data_retention/cleanup

#### POST post
- **Path**: `post`
- **Description**: Immediately executes data cleanup according to retention policies. This permanently deletes expired data.

### /api/v1/user/me

#### GET get
- **Path**: `get`
- **Description**: Retrieves the current authenticated user's profile. The user must be authenticated and can only access their own profile.

### /api/v1/user/me

#### PATCH patch
- **Path**: `patch`
- **Description**: Updates the current authenticated user's profile. The user must be authenticated and can only update their own profile. All personal data is encrypted at rest for GDPR compliance.

### /api/v1/set_scheme/{id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/set_scheme/{id}

#### DELETE delete
- **Path**: `delete`
- **Description**: No description available

### /api/v1/set_scheme/{id}

#### PATCH patch
- **Path**: `patch`
- **Description**: No description available

### /api/v1/programmed_workout/{id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_workout/{id}

#### DELETE delete
- **Path**: `delete`
- **Description**: No description available

### /api/v1/programmed_workout/{id}

#### PATCH patch
- **Path**: `patch`
- **Description**: No description available

### /api/v1/programmed_exercise/{id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_exercise/{id}

#### DELETE delete
- **Path**: `delete`
- **Description**: No description available

### /api/v1/programmed_exercise/{id}

#### PATCH patch
- **Path**: `patch`
- **Description**: No description available

### /api/v1/program_preferences/

#### PATCH patch
- **Path**: `patch`
- **Description**: Updates existing program preferences (session time only).

### /api/v1/program/{id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves a specific program by its unique identifier.

### /api/v1/program/{id}

#### DELETE delete
- **Path**: `delete`
- **Description**: Deletes a program by its unique identifier.

### /api/v1/program/{id}

#### PATCH patch
- **Path**: `patch`
- **Description**: Updates an existing program with the specified ID.

### /api/v1/conjugate_workout_generator/{program_id}/update_with_1rm

#### PATCH patch
- **Path**: `patch`
- **Description**: Updates a generated workout with user's 1RM data to tailor weights appropriately

### /api/v1/workout_stage_type/

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all workout stage types.

### /api/v1/workout_stage_type/{id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves a workout stage type by its unique identifier.

### /api/v1/workout_stage_type/name/{name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves a workout stage type by its name.

### /api/v1/workout_stage/{id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/workout_stage/{id}

#### DELETE delete
- **Path**: `delete`
- **Description**: No description available

### /api/v1/workout_stage/workout/{programmed_workout_id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/user_weight_unit_preference/{user_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all weight unit preferences for a given user.

### /api/v1/user_weight_unit_preference/{user_id}/{exercise_name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves a specific weight unit preference for a given user and exercise.

### /api/v1/user_weight_unit_preference/{user_id}/{exercise_name}

#### DELETE delete
- **Path**: `delete`
- **Description**: Deletes a user weight unit preference for a given user and exercise.

### /api/v1/user_weak_muscle/{user_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all weak muscle groups for a user.

### /api/v1/user_one_rep_max/user/{user_id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/user_one_rep_max/user/{user_id}/exercise/{exercise_name}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/user_one_rep_max/user/{user_id}/exercise/{exercise_name}

#### DELETE delete
- **Path**: `delete`
- **Description**: No description available

### /api/v1/user_exercise_preference/{user_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercise preferences associated with a given user.

### /api/v1/user_equipment/{user_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all equipment associated with a given user.

### /api/v1/set_scheme/exercise/{programmed_exercise_id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_workout/program/{program_id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/programmed_exercise/stage/{workout_stage_id}

#### GET get
- **Path**: `get`
- **Description**: No description available

### /api/v1/program_preferences/{program_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves user program preferences for a given program.

### /api/v1/program/with-preferences

#### GET get
- **Path**: `get`
- **Description**: Retrieves a list of all programs with their associated preferences.

### /api/v1/program/user/{user_id}

#### GET get
- **Path**: `get`
- **Description**: Retrieves programs for a specific user, optionally filtered by active status.

### /api/v1/performance/test_protocols

#### GET get
- **Path**: `get`
- **Description**: Retrieve the current weekly test protocol configuration.

### /api/v1/performance/scores

#### GET get
- **Path**: `get`
- **Description**: Retrieve calculated HP/MP/Fatigue values, athleticism level, and skills.

### /api/v1/performance/scores/history

#### GET get
- **Path**: `get`
- **Description**: Retrieve historical performance scores within a date range for trend analysis and progression tracking.

### /api/v1/performance/metrics/range

#### GET get
- **Path**: `get`
- **Description**: Retrieve historical performance metrics within the specified date range for trend analysis.

### /api/v1/muscle/{name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves muscle details by name.

### /api/v1/muscle/{muscle_name}/exercise

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercises that target a specific muscle.

### /api/v1/health/

#### GET get
- **Path**: `get`
- **Description**: Performs a health check of the application and its dependencies.

### /api/v1/gdpr/privacy_policy

#### GET get
- **Path**: `get`
- **Description**: Provides information about data processing activities, legal basis, retention periods, and user rights under GDPR.

### /api/v1/gdpr/export

#### GET get
- **Path**: `get`
- **Description**: Exports all personal data for the authenticated user in compliance with GDPR Article 20 (Right to Data Portability). Data is returned in JSON format.

### /api/v1/exercise_workout_type/movement_type/{movement_type}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all workout types associated with a given movement type.

### /api/v1/exercise_workout_type/exercise/{exercise_name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves all workout types associated with a given exercise.

### /api/v1/exercise/{name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves exercise details by name.

### /api/v1/exercise/{name}/muscle

#### GET get
- **Path**: `get`
- **Description**: Retrieves all muscles associated with a given exercise.

### /api/v1/exercise/{name}/equipment

#### GET get
- **Path**: `get`
- **Description**: Retrieves all equipment associated with a given exercise.

### /api/v1/equipment/{name}

#### GET get
- **Path**: `get`
- **Description**: Retrieves equipment details by name.

### /api/v1/equipment/{name}/exercise

#### GET get
- **Path**: `get`
- **Description**: Retrieves all exercises associated with a given equipment.

### /api/v1/conjugate_workout_generator/exercise_pool

#### GET get
- **Path**: `get`
- **Description**: Returns the user's available exercise pool based on preferences, equipment, and previous usage

### /api/v1/admin/data_retention/cleanup_estimate

#### GET get
- **Path**: `get`
- **Description**: Estimates how many records would be deleted by the cleanup process without actually performing the deletion.

### /api/v1/gdpr/delete_all_data

#### DELETE delete
- **Path**: `delete`
- **Description**: Permanently deletes all personal data for the authenticated user in compliance with GDPR Article 17 (Right to Erasure). This operation is irreversible and requires confirmation.


## Data Models

### UserOneRepMax

```json
{"required":["exercise_name","one_rep_max","updated_at","user_id"],"type":"object","properties":{"user_id":{"type":"string","description":"ID of the user (Keycloak ID)","example":"123e4567-e89b-12d3-a456-426614174000"},"exercise_name":{"type":"string","description":"Name of the exercise","example":"Bench Press"},"one_rep_max":{"type":"number","description":"The one rep max weight in kilograms","example":100.0},"updated_at":{"type":"string","description":"Timestamp when the 1RM was last updated","format":"date-time","example":"2024-01-01T00:00:00Z"}},"description":"Represents a user's one rep max for a specific exercise."}
```

### UserTestResult

```json
{"required":["created_at","keycloak_id","status","test_name","updated_at","week_start_timestamp"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the test result","format":"int32","readOnly":true,"example":1},"keycloak_id":{"type":"string","description":"Unique Keycloak identifier for the user","example":"123e4567-e89b-12d3-a456-426614174000"},"week_start_timestamp":{"type":"string","description":"Start timestamp of the week (Monday)","format":"date-time","example":"2023-08-07T00:00:00Z"},"test_name":{"type":"string","description":"Name of the test protocol","example":"vertical_jump"},"status":{"type":"string","description":"Status of a weekly test","example":"COMPLETED","enum":["PENDING","COMPLETED","SKIPPED","PENDING","COMPLETED","SKIPPED"]},"result_value":{"minimum":0,"type":"number","description":"The actual test result value (if completed)","format":"double","example":52.0},"created_at":{"type":"string","description":"Timestamp when the test result was created","format":"date-time","readOnly":true,"example":"2023-01-15T08:30:00Z"},"updated_at":{"type":"string","description":"Timestamp when the test result was last updated","format":"date-time","readOnly":true,"example":"2023-08-09T09:45:30Z"}},"description":"Individual test result for a user","example":"UserTestResult(id=1, keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", testName=\"vertical_jump\", status=\"COMPLETED\", resultValue=52.0)"}
```

### UserPerformanceScores

```json
{"required":["created_at","fatigue","fatigue_loss","hp","hp_loss","keycloak_id","level","mp","mp_loss","skills"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for this score calculation record","format":"int32","readOnly":true,"example":1},"keycloak_id":{"type":"string","description":"Unique Keycloak identifier for the user","example":"123e4567-e89b-12d3-a456-426614174000"},"explosiveness_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual explosiveness score (0-100)","format":"double","example":68.2},"aerobic_capacity_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual aerobic capacity score (0-100)","format":"double","example":71.8},"recovery_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual recovery score (0-100)","format":"double","example":82.4},"reaction_time_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual reaction time score (0-100)","format":"double","example":73.7},"mobility_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual mobility score (0-100)","format":"double","example":68.5},"strength_score":{"maximum":100,"minimum":0,"type":"number","description":"Individual strength score (0-100) based on Wilks score","format":"double","example":75.2},"wilks_score":{"minimum":0,"type":"number","description":"Raw Wilks score calculated from big three lifts and body weight","format":"double","example":312.5},"level":{"maximum":100,"minimum":1,"type":"integer","description":"Athleticism level with tanh scaling (1-100)","format":"int32","example":73},"level_change_reason":{"type":"string","description":"Reason for this score calculation","example":"weekly_test_completed","enum":["weekly_test_completed","daily_metrics_updated","initial_calculation"]},"hp":{"maximum":100,"minimum":0,"type":"number","description":"Health Points - physical resilience (0-100)","format":"double","example":82.0},"hp_loss":{"maximum":100,"minimum":0,"type":"number","description":"HP Loss - current HP reduction from daily stress factors (0-100)","format":"double","example":15.0},"mp":{"maximum":100,"minimum":0,"type":"number","description":"Magic Points - neural readiness (0-100)","format":"double","example":76.0},"mp_loss":{"maximum":100,"minimum":0,"type":"number","description":"MP Loss - current MP reduction from daily stress factors (0-100)","format":"double","example":12.0},"fatigue":{"maximum":100,"minimum":0,"type":"number","description":"Current fatigue level (0-100)","format":"double","example":42.0},"fatigue_loss":{"maximum":100,"minimum":0,"type":"number","description":"Fatigue Loss - current fatigue increase from daily stress factors (0-100)","format":"double","example":25.0},"skills":{"type":"array","description":"List of auto-generated skills","example":["Explosive Power","Iron Lungs","Lightning Reflexes"],"items":{"type":"string","description":"List of auto-generated skills","example":"[\"Explosive Power\",\"Iron Lungs\",\"Lightning Reflexes\"]"}},"created_at":{"type":"string","description":"Timestamp when the scores were calculated","format":"date-time","readOnly":true,"example":"2023-01-15T08:30:00Z"}},"description":"Calculated performance scores and gamified metrics","example":"UserPerformanceScores(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", athleticismScore=73.5, level=12, hp=82.0, mp=76.0, fatigue=42.0)"}
```

### DataRetentionPolicy

```json
{"required":["data_type","retention_period_days"],"type":"object","properties":{"data_type":{"type":"string","description":"The type of data the policy applies to"},"retention_period_days":{"type":"integer","description":"How long to retain the data in days","format":"int32"},"description":{"type":"string","description":"Optional description of the policy"}},"description":"Data retention policy"}
```

### WorkoutStage

```json
{"required":["created_at","id","name","position","programmed_workout_id","stage_type_id","updated_at"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the workout stage","format":"int64","readOnly":true,"example":1},"programmed_workout_id":{"type":"integer","description":"ID of the programmed workout this stage belongs to","format":"int64","example":5},"stage_type_id":{"type":"integer","description":"ID of the workout stage type (warm-up, main, cool-down, etc.)","format":"int32","example":1},"position":{"minimum":1,"type":"integer","description":"Order of this stage within the workout (1-based)","format":"int32","example":1},"name":{"type":"string","description":"Name of the workout stage","example":"Warm-up"},"created_at":{"type":"string","description":"Created at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"updated_at":{"type":"string","description":"Updated at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"A stage within a programmed workout","example":"WorkoutStage(id=1, programmedWorkoutId=5, stageTypeId=1, position=1)"}
```

### User

```json
{"required":["created_at","keycloak_id","name","updated_at"],"type":"object","properties":{"keycloak_id":{"type":"string","description":"Unique Keycloak identifier for the user","readOnly":true,"example":"123e4567-e89b-12d3-a456-426614174000"},"name":{"maxLength":255,"minLength":1,"type":"string","description":"User's full name","example":"John Doe"},"age":{"minimum":1,"type":"integer","description":"User's age in years","format":"int32","example":30},"weight":{"minimum":1,"type":"integer","description":"User's weight in pounds","format":"int32","example":180},"height":{"minimum":1,"type":"integer","description":"User's height in inches","format":"int32","example":72},"gender":{"type":"string","description":"User's gender","example":"male","enum":["male","female"]},"created_at":{"type":"string","description":"Timestamp when the user was created","format":"date-time","example":"2024-01-01T00:00:00Z"},"updated_at":{"type":"string","description":"Timestamp when the user was last updated","format":"date-time","example":"2024-01-01T00:00:00Z"}},"description":"A user profile in the workout generation system","example":"User(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", name=\"John Doe\", age=30, weight=180, height=72, gender=\"male\")"}
```

### SetScheme

```json
{"required":["created_at","id","is_amrap","is_emom","programmed_exercise_id","set_number","updated_at","use_tempo"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the set scheme","format":"int64","readOnly":true,"example":1},"programmed_exercise_id":{"type":"integer","description":"ID of the programmed exercise this set belongs to","format":"int64","example":5},"set_number":{"type":"integer","description":"Order of this set within the exercise (1-based)","format":"int32","example":1},"is_amrap":{"type":"boolean","description":"Whether this set scheme is AMRAP","example":false},"is_emom":{"type":"boolean","description":"Whether this set scheme is EMOM","example":false},"use_tempo":{"type":"boolean","description":"Whether tempo should be used for this set scheme","example":true},"eccentric_tempo":{"pattern":"^[0-9]$","type":"string","description":"Eccentric phase tempo (0-9 seconds)","example":"3"},"isometric_tempo":{"pattern":"^[0-9]$","type":"string","description":"Isometric phase tempo (0-9 seconds)","example":"1"},"concentric_tempo":{"pattern":"^[0-9]$","type":"string","description":"Concentric phase tempo (0-9 seconds)","example":"1"},"target_weight":{"minimum":0.01,"type":"number","description":"Target weight for the set in kg","example":100.0},"performed_weight":{"minimum":0.01,"type":"number","description":"Actual weight used in kg","example":100.0},"target_rep_count":{"maximum":1000,"minimum":1,"type":"integer","description":"Target number of repetitions","format":"int32","example":5},"performed_rep_count":{"maximum":1000,"minimum":1,"type":"integer","description":"Actual number of repetitions completed","format":"int32","example":5},"rest_seconds":{"maximum":3600,"minimum":0,"type":"integer","description":"Rest period after the set in seconds","format":"int32","example":180},"created_at":{"type":"string","description":"Timestamp when the set scheme was created","format":"date-time","readOnly":true,"example":"2024-01-01T12:00:00Z"},"updated_at":{"type":"string","description":"Timestamp when the set scheme was last updated","format":"date-time","readOnly":true,"example":"2024-01-01T12:00:00Z"},"band_weight_lbs":{"type":"number"}},"description":"A set within a programmed exercise with performance parameters","example":"SetScheme(id=1, programmedExerciseId=5, setNumber=1, isAmrap=false, isEmom=false, useTempo=true, eccentricTempo=\"3\", isometricTempo=\"1\", concentricTempo=\"1\", targetWeight=100.0, performedWeight=100.0, targetRepCount=5, performedRepCount=5, restSeconds=180)"}
```

### ProgrammedWorkout

```json
{"required":["created_at","day_number","id","name","program_id","updated_at"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the programmed workout","format":"int64","readOnly":true,"example":1},"program_id":{"type":"integer","description":"ID of the training program this workout belongs to","format":"int64","example":5},"day_number":{"type":"integer","description":"Day number within the program (1-365)","format":"int32","example":1},"name":{"type":"string","description":"Name for the workout","example":"Upper Body Strength"},"created_at":{"type":"string","description":"Created at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"updated_at":{"type":"string","description":"Updated at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"A scheduled workout within a training program","example":"ProgrammedWorkout(id=1, programId=5, dayNumber=1, name=\"Upper Body Strength\")"}
```

### ProgrammedExercise

```json
{"required":["created_at","exercise_name","id","position","updated_at","workout_stage_id"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the programmed exercise","format":"int64","readOnly":true,"example":1},"workout_stage_id":{"type":"integer","description":"ID of the workout stage this exercise belongs to","format":"int64","example":5},"exercise_name":{"type":"string","description":"Name of the exercise to be performed","example":"Bench Press"},"position":{"type":"integer","description":"Position of the exercise within the stage","format":"int32","example":1},"notes":{"type":"string","description":"Optional notes or instructions for the exercise","example":"Focus on controlled descent"},"created_at":{"type":"string","description":"Created at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"updated_at":{"type":"string","description":"Updated at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"An exercise assigned to a specific workout stage","example":"ProgrammedExercise(id=1, workoutStageId=5, exerciseName=\"Bench Press\", notes=\"Focus on controlled descent\")"}
```

### UserConsent

```json
{"required":["created_at","data_processing_consent","keycloak_id","updated_at"],"type":"object","properties":{"keycloak_id":{"type":"string","description":"User's Keycloak ID","example":"123e4567-e89b-12d3-a456-426614174000"},"data_processing_consent":{"type":"boolean","description":"Whether consent has been given for data processing","example":true},"consent_timestamp":{"type":"string","description":"Timestamp when consent was last given or withdrawn","format":"date-time","example":"2023-08-09T10:15:30Z"},"created_at":{"type":"string","description":"Timestamp when the consent record was created","format":"date-time","example":"2023-08-09T10:15:30Z"},"updated_at":{"type":"string","description":"Timestamp when the consent record was last updated","format":"date-time","example":"2023-08-09T10:15:30Z"}},"description":"User consent record for GDPR data processing","example":"UserConsent(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", dataProcessingConsent=true, consentTimestamp=\"2023-08-09T10:15:30Z\", updatedAt=\"2023-08-09T10:15:30Z\")"}
```

### Program

```json
{"required":["created_at","current_week_number","id","is_active","name","updated_at","user_id"],"type":"object","properties":{"id":{"type":"integer","description":"Unique identifier for the program","format":"int64","readOnly":true,"example":1},"user_id":{"type":"string","description":"ID of the user who owns this program (Keycloak ID)","example":"123e4567-e89b-12d3-a456-426614174000"},"name":{"type":"string","description":"Human-readable name of the program","example":"Beginner Strength Program"},"current_week_number":{"type":"integer","description":"Current week number","format":"int32","example":1},"created_at":{"type":"string","description":"Created at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"updated_at":{"type":"string","description":"Updated at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"is_active":{"type":"boolean","description":"Whether this program is currently active","example":true}},"description":"A training program containing multiple workouts","example":"Program(id=1, userId=1, name=\"Beginner Strength Program\", description=\"A 3-day strength program for beginners\")"}
```

### CleanupSummary

```json
{"required":["data_types_processed","execution_time","total_deleted"],"type":"object","properties":{"total_deleted":{"type":"integer","description":"Total number of records deleted across all data types","format":"int32"},"data_types_processed":{"type":"integer","description":"Number of data types that were processed","format":"int32"},"execution_time":{"type":"string","description":"When the cleanup was executed","format":"date-time"}},"description":"Summary information about a cleanup operation"}
```

### DataCleanupResult

```json
{"required":["count","data_type","operation_type"],"type":"object","properties":{"data_type":{"type":"string","description":"The type of data that was processed"},"count":{"type":"integer","description":"Number of records (deleted for cleanup, estimated for estimation)","format":"int32"},"operation_type":{"type":"string","description":"Type of operation performed"}},"description":"Result of a cleanup operation or estimation"}
```

### ManualCleanupResponse

```json
{"required":["cleanup_results","summary"],"type":"object","properties":{"cleanup_results":{"type":"array","description":"List of cleanup results by data type","items":{"$ref":"#/components/schemas/DataCleanupResult"}},"summary":{"$ref":"#/components/schemas/CleanupSummary"}},"description":"Response for manual cleanup operations"}
```

### TestProtocol

```json
{"required":["description","display_name","display_order","icon_name","is_required","radar_chart_color","radar_chart_enabled","test_name","unit"],"type":"object","properties":{"test_name":{"type":"string","description":"Name of the test","example":"vertical_jump"},"display_name":{"type":"string","description":"Display name for the test","example":"Vertical Jump"},"description":{"type":"string","description":"Detailed description of the test","example":"Measure explosive power using MyJump2 app"},"unit":{"type":"string","description":"Unit of measurement for the test result","example":"cm"},"icon_name":{"type":"string","description":"Icon identifier for UI display","example":"fitness_center"},"is_required":{"type":"boolean","description":"Whether this test is required for the protocol","example":true},"display_order":{"type":"integer","description":"Order for displaying tests in UI","format":"int32","example":1},"radar_chart_color":{"type":"string","description":"Color for radar chart display","example":"#FF6B6B"},"radar_chart_enabled":{"type":"boolean","description":"Whether this test should appear in radar chart","example":true}},"description":"Test protocol configuration for weekly testing","example":"TestProtocol(day=\"Monday\", testName=\"Vertical Jump\", description=\"Measure explosive power using MyJump2 app\")"}
```

### UserPerformanceMetrics

```json
{"required":["created_at","keycloak_id","updated_at"],"type":"object","properties":{"keycloak_id":{"type":"string","description":"Unique Keycloak identifier for the user","readOnly":true,"example":"123e4567-e89b-12d3-a456-426614174000"},"vo2_max":{"minimum":0,"type":"number","description":"VO₂ max estimate in ml/kg/min","format":"double","example":48},"strain":{"maximum":21,"minimum":0,"type":"number","description":"Daily strain score from wearables (e.g., Whoop). If not provided, system will rely more heavily on subjective tiredness.","format":"double","example":15.2},"recovery":{"maximum":100,"minimum":0,"type":"number","description":"Daily recovery score","format":"double","example":78},"hrv":{"minimum":0,"type":"number","description":"Heart rate variability","format":"double","example":52},"sleep_score":{"maximum":100,"minimum":0,"type":"number","description":"Sleep score","format":"double","example":84},"rem_sleep_minutes":{"minimum":0,"type":"number","description":"REM sleep duration in minutes","format":"double","example":80},"deep_sleep_minutes":{"minimum":0,"type":"number","description":"Deep sleep duration in minutes","format":"double","example":120},"subjective_tiredness":{"maximum":5,"minimum":1,"type":"integer","description":"Subjective tiredness rating (1=fresh, 5=exhausted). Has higher impact on HP and fatigue calculations when strain data is not available.","format":"int32","example":3},"created_at":{"type":"string","description":"Timestamp when the metrics were created","format":"date-time","readOnly":true,"example":"2023-01-15T08:30:00Z"},"updated_at":{"type":"string","description":"Timestamp when the metrics were last updated","format":"date-time","readOnly":true,"example":"2023-08-09T09:45:30Z"}},"description":"User performance metrics for gamified tracking","example":"UserPerformanceMetrics(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", relativeStrength=350, verticalJumpCm=50, vo2Max=48, hrRecovery=40, muscularEndurance=40, reactionTimeMs=350)"}
```

### HealthCheck

```json
{"required":["affected_endpoints","links","status","time"],"type":"object","properties":{"component_id":{"type":"string","description":"Component identifier."},"component_type":{"type":"string","description":"Type of component."},"observed_value":{"type":"object","description":"Observed value."},"observed_unit":{"type":"string","description":"Observed unit."},"status":{"type":"string","description":"Possible health status values.","enum":["pass","warn","fail"]},"affected_endpoints":{"type":"array","description":"Endpoints affected by this component.","items":{"type":"string","description":"Endpoints affected by this component."}},"time":{"type":"string","description":"Timestamp of the check.","format":"date-time"},"output":{"type":"string","description":"Output message."},"links":{"type":"object","additionalProperties":{"type":"string","description":"Related links."},"description":"Related links."}},"description":"Represents the health check for a specific component."}
```

### HealthCheckResponse

```json
{"required":["checks","description","links","notes","release_id","service_id","status","version"],"type":"object","properties":{"status":{"type":"string","description":"Possible health status values.","enum":["pass","warn","fail"]},"version":{"type":"string","description":"API version."},"release_id":{"type":"string","description":"Release identifier."},"notes":{"type":"array","description":"Additional notes.","items":{"type":"string","description":"Additional notes."}},"output":{"type":"string","description":"Output message."},"checks":{"type":"object","additionalProperties":{"type":"array","description":"Map of checks by component.","items":{"$ref":"#/components/schemas/HealthCheck"}},"description":"Map of checks by component."},"links":{"type":"object","additionalProperties":{"type":"string","description":"Related links."},"description":"Related links."},"service_id":{"type":"string","description":"Service identifier."},"description":{"type":"string","description":"Description of the health check response."}},"description":"Top-level health check response object."}
```

### DataController

```json
{"required":["contact","name"],"type":"object","properties":{"name":{"type":"string","description":"Name of the data controller","example":"Congen Fitness Application"},"contact":{"type":"string","description":"Contact email for privacy inquiries","example":"privacy@congen.com"},"dpo":{"type":"string","description":"Data Protection Officer contact","example":"dpo@congen.com"}},"description":"Data controller information"}
```

### DataProcessing

```json
{"required":["data_types","legal_basis","purposes","retention_periods"],"type":"object","properties":{"purposes":{"type":"array","description":"Purposes for data processing","items":{"type":"string","description":"Purposes for data processing"}},"legal_basis":{"type":"array","description":"Legal basis for processing","items":{"type":"string","description":"Legal basis for processing"}},"data_types":{"type":"array","description":"Types of data collected","items":{"type":"string","description":"Types of data collected"}},"retention_periods":{"type":"object","additionalProperties":{"type":"string","description":"Data retention periods by data type"},"description":"Data retention periods by data type"}},"description":"Data processing information"}
```

### PrivacyPolicy

```json
{"required":["data_controller","data_processing","last_updated","user_rights","version"],"type":"object","properties":{"data_controller":{"$ref":"#/components/schemas/DataController"},"data_processing":{"$ref":"#/components/schemas/DataProcessing"},"user_rights":{"$ref":"#/components/schemas/UserRights"},"last_updated":{"type":"string","description":"When the privacy policy was last updated","example":"2025-08-08T00:00:00Z"},"version":{"type":"string","description":"Privacy policy version","example":"1.0.0"}},"description":"Complete privacy policy and GDPR compliance information"}
```

### UserRights

```json
{"required":["access","complaint","erasure","objection","portability","rectification"],"type":"object","properties":{"access":{"type":"string","description":"Right of access information"},"rectification":{"type":"string","description":"Right to rectification information"},"erasure":{"type":"string","description":"Right to erasure information"},"portability":{"type":"string","description":"Right to data portability information"},"objection":{"type":"string","description":"Right to object information"},"complaint":{"type":"string","description":"Right to file a complaint information"}},"description":"User rights under GDPR"}
```

### ProgramPreferences

```json
{"required":["created_at","program_days_per_week","program_id","session_time_length_in_minutes","updated_at"],"type":"object","properties":{"program_id":{"type":"integer","description":"ID of the program these preferences belong to","format":"int64","example":1},"program_days_per_week":{"type":"integer","description":"Number of workout days per week","format":"int32","example":4},"session_time_length_in_minutes":{"type":"integer","description":"Length of each workout session in minutes","format":"int32","example":60},"created_at":{"type":"string","description":"Created at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"},"updated_at":{"type":"string","description":"Updated at timestamp","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"Program preferences for workout frequency and duration","example":"ProgramPreferences(programId=1, programDaysPerWeek=3, sessionTimeLengthInMinutes=60)"}
```

### ProgramWithWorkouts

```json
{"required":["program","program_preferences","workouts"],"type":"object","properties":{"program":{"$ref":"#/components/schemas/Program"},"program_preferences":{"$ref":"#/components/schemas/ProgramPreferences"},"workouts":{"type":"array","items":{"$ref":"#/components/schemas/ProgrammedWorkoutWithStages"}}},"description":"A training program with complete workout hierarchy for export","example":"ProgramWithWorkouts(id=1, name=\"Beginner Strength Program\", workouts=[...])"}
```

### ProgrammedExerciseWithSetSchemes

```json
{"required":["exercise","set_schemes"],"type":"object","properties":{"exercise":{"$ref":"#/components/schemas/ProgrammedExercise"},"set_schemes":{"type":"array","items":{"$ref":"#/components/schemas/SetScheme"}}},"description":"A programmed exercise with complete set scheme hierarchy for export","example":"ProgrammedExerciseWithSetSchemes(id=1, exerciseName=\"Bench Press\", setSchemes=[...])"}
```

### ProgrammedWorkoutWithStages

```json
{"required":["stages","workout"],"type":"object","properties":{"workout":{"$ref":"#/components/schemas/ProgrammedWorkout"},"stages":{"type":"array","items":{"$ref":"#/components/schemas/WorkoutStageWithExercises"}}},"description":"A programmed workout with complete stage hierarchy for export","example":"ProgrammedWorkoutWithStages(id=1, name=\"Upper Body Strength\", stages=[...])"}
```

### UserDataExport

```json
{"required":["audit_logs","created_at","data_processing_consent","data_retention_policies","export_timestamp","keycloak_id","name","training_programs","updated_at","user_equipment","user_exercise_preferences","user_one_rep_max","user_performance_metrics","user_performance_scores","user_test_results","user_weak_muscles","user_weight_unit_preferences"],"type":"object","properties":{"keycloak_id":{"type":"string","description":"User's Keycloak ID","example":"123e4567-e89b-12d3-a456-426614174000"},"name":{"type":"string","description":"User's full name","example":"John Doe"},"age":{"type":"integer","description":"User's age in years","format":"int32","example":30},"weight":{"type":"integer","description":"User's weight in pounds","format":"int32","example":180},"height":{"type":"integer","description":"User's height in inches","format":"int32","example":72},"gender":{"type":"string","description":"User's gender","example":"male","enum":["male","female"]},"created_at":{"type":"string","description":"Timestamp when the user account was created","format":"date-time","example":"2023-01-15T08:30:00Z"},"updated_at":{"type":"string","description":"Timestamp when the user account was last updated","format":"date-time","example":"2023-08-09T09:45:30Z"},"data_processing_consent":{"type":"boolean","description":"Whether the user has given consent for data processing","example":true},"consent_timestamp":{"type":"string","description":"Timestamp when consent was last given or withdrawn","format":"date-time","example":"2023-08-09T10:00:00Z"},"export_timestamp":{"type":"string","description":"Timestamp when this data export was generated","format":"date-time","example":"2023-08-09T10:15:30Z"},"user_equipment":{"type":"array","items":{"type":"object"}},"user_exercise_preferences":{"type":"array","items":{"type":"object"}},"user_one_rep_max":{"type":"array","items":{"type":"object"}},"user_weight_unit_preferences":{"type":"array","items":{"type":"object"}},"user_performance_metrics":{"type":"array","items":{"type":"object"}},"user_performance_scores":{"type":"array","items":{"type":"object"}},"user_test_results":{"type":"array","items":{"type":"object"}},"user_weak_muscles":{"type":"array","items":{"type":"object"}},"training_programs":{"type":"array","items":{"$ref":"#/components/schemas/ProgramWithWorkouts"}},"audit_logs":{"type":"array","items":{"type":"object"}},"data_retention_policies":{"type":"array","items":{"type":"object"}}},"description":"Complete user data export for GDPR data portability requests","example":"UserDataExport(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", name=\"John Doe\", dataProcessingConsent=true, exportTimestamp=\"2023-08-09T10:15:30Z\")"}
```

### WorkoutStageWithExercises

```json
{"required":["exercises","stage"],"type":"object","properties":{"stage":{"$ref":"#/components/schemas/WorkoutStage"},"exercises":{"type":"array","items":{"$ref":"#/components/schemas/ProgrammedExerciseWithSetSchemes"}}},"description":"A workout stage with complete exercise hierarchy for export","example":"WorkoutStageWithExercises(id=1, name=\"Main\", exercises=[...])"}
```

### Exercise

```json
{"required":["description","is_accessory","is_unilateral","is_upper","movement_type","name"],"type":"object","properties":{"name":{"type":"string","description":"Name of the exercise","example":"Bench Press"},"description":{"type":"string","description":"Description of the exercise","example":"A compound upper body exercise."},"movement_type":{"type":"string","description":"Type of movement","example":"horizontal_push","enum":["horizontal_push","vertical_push","horizontal_pull","vertical_pull","squat","hinge","lunge","core","plyometric","carry","isolation"]},"is_unilateral":{"type":"boolean","description":"Whether the exercise is unilateral (one side at a time)","example":false},"is_upper":{"type":"boolean","description":"Whether the exercise targets upper body","example":true},"is_accessory":{"type":"boolean","description":"Whether the exercise is an accessory movement","example":false}},"description":"Represents an exercise that can be included in a workout."}
```

### UserEquipment

```json
{"required":["created_at","equipment_name","user_id"],"type":"object","properties":{"user_id":{"type":"string","description":"ID of the user (Keycloak ID)","example":"123e4567-e89b-12d3-a456-426614174000"},"equipment_name":{"type":"string","description":"Name of the equipment","example":"Barbell"},"created_at":{"type":"string","description":"Timestamp when the user equipment relationship was created","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"Represents the relationship between a user and a piece of equipment they have access to."}
```

### UserExercisePoolResponse

```json
{"required":["accessory_exercises","available_exercises","previously_used_exercises","primary_exercises","total_exercises","user_equipment","user_id","user_preferences"],"type":"object","properties":{"user_id":{"type":"string","description":"ID of the user (Keycloak ID)","example":"123e4567-e89b-12d3-a456-426614174000"},"total_exercises":{"type":"integer","description":"Total number of exercises in the system","format":"int32","example":150},"available_exercises":{"type":"integer","description":"Number of exercises available to the user","format":"int32","example":120},"primary_exercises":{"type":"array","description":"List of available primary exercises","items":{"$ref":"#/components/schemas/Exercise"}},"accessory_exercises":{"type":"array","description":"List of available accessory exercises","items":{"$ref":"#/components/schemas/Exercise"}},"user_equipment":{"type":"array","description":"User's available equipment","items":{"$ref":"#/components/schemas/UserEquipment"}},"user_preferences":{"type":"array","description":"User's exercise preferences","items":{"$ref":"#/components/schemas/UserExercisePreference"}},"previously_used_exercises":{"type":"array","description":"List of exercises used in previous weeks","items":{"type":"string","description":"List of exercises used in previous weeks"}}},"description":"User's exercise pool with available exercises and metadata"}
```

### UserExercisePreference

```json
{"required":["created_at","exercise_name","should_avoid","user_id"],"type":"object","properties":{"user_id":{"type":"string","description":"ID of the user (Keycloak ID)","example":"123e4567-e89b-12d3-a456-426614174000"},"exercise_name":{"type":"string","description":"The name of the exercise","example":"Bench Press"},"should_avoid":{"type":"boolean","description":"Whether the user should avoid this exercise","example":false},"created_at":{"type":"string","description":"Timestamp when the preference was created","format":"date-time","example":"2024-07-06T12:00:00Z"}},"description":"Represents a user's preference for a specific exercise."}
```

### CleanupEstimationResponse

```json
{"required":["estimated_deletions"],"type":"object","properties":{"estimated_deletions":{"type":"array","description":"List of estimated deletions by data type","items":{"$ref":"#/components/schemas/EstimatedDeletion"}}},"description":"Response for cleanup estimation"}
```

### EstimatedDeletion

```json
{"required":["data_type","estimated_records_to_delete"],"type":"object","properties":{"data_type":{"type":"string","description":"The type of data"},"estimated_records_to_delete":{"type":"integer","description":"Number of records estimated to be deleted","format":"int32"}},"description":"Individual estimated deletion for a data type"}
```


## Error Responses

The API uses standard HTTP status codes:

- **400 Bad Request**: Invalid request format or parameters
- **404 Not Found**: Resource not found
- **422 Unprocessable Entity**: Validation errors
- **500 Internal Server Error**: Server-side errors

---
*This documentation was automatically generated from OpenAPI specification on Thu Oct  9 09:36:00 EDT 2025*
