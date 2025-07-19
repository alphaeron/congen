# Congen API Documentation

This documentation is automatically generated from the Spring Boot application source code.

## Overview

The Congen API provides endpoints for managing workout programs, exercises, users, and preferences using the conjugate method.

## Interactive Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [api-docs.json](api-docs.json)
- **OpenAPI YAML**: [api-docs.yaml](api-docs.yaml)

## API Endpoints

### User Management

#### Create User
- **POST** `/user/`
- **Description**: Creates a new user profile
- **Request Body**: User object with name, age, height, weight
- **Response**: Created user with ID

#### Get User
- **GET** `/user/{id}`
- **Description**: Retrieves a user by ID
- **Parameters**: `id` (path parameter)
- **Response**: User object

#### Get All Users
- **GET** `/user/`
- **Description**: Retrieves all users
- **Response**: Array of user objects

#### Update User
- **PUT** `/user/{id}`
- **Description**: Updates an existing user
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated user object
- **Response**: Updated user object

#### Delete User
- **DELETE** `/user/{id}`
- **Description**: Deletes a user
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### Program Management

#### Create Program
- **POST** `/program/`
- **Description**: Creates a new workout program
- **Request Body**: Program object
- **Response**: Created program with ID

#### Get Program
- **GET** `/program/{id}`
- **Description**: Retrieves a program by ID
- **Parameters**: `id` (path parameter)
- **Response**: Program object

#### Get All Programs
- **GET** `/program/`
- **Description**: Retrieves all programs
- **Response**: Array of program objects

#### Update Program
- **PUT** `/program/{id}`
- **Description**: Updates an existing program
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated program object
- **Response**: Updated program object

#### Delete Program
- **DELETE** `/program/{id}`
- **Description**: Deletes a program
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### Exercise Management

#### Create Exercise
- **POST** `/exercise/`
- **Description**: Creates a new exercise
- **Request Body**: Exercise object
- **Response**: Created exercise with ID

#### Get Exercise
- **GET** `/exercise/{id}`
- **Description**: Retrieves an exercise by ID
- **Parameters**: `id` (path parameter)
- **Response**: Exercise object

#### Get All Exercises
- **GET** `/exercise/`
- **Description**: Retrieves all exercises
- **Response**: Array of exercise objects

#### Update Exercise
- **PUT** `/exercise/{id}`
- **Description**: Updates an existing exercise
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated exercise object
- **Response**: Updated exercise object

#### Delete Exercise
- **DELETE** `/exercise/{id}`
- **Description**: Deletes an exercise
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### User Program Preferences

#### Create User Program Preferences
- **POST** `/user-program-preferences/`
- **Description**: Creates user program preferences
- **Request Body**: UserProgramPreferences object
- **Response**: Created preferences with ID

#### Get User Program Preferences
- **GET** `/user-program-preferences/{userId}`
- **Description**: Retrieves user program preferences
- **Parameters**: `userId` (path parameter)
- **Response**: UserProgramPreferences object

#### Update User Program Preferences
- **PUT** `/user-program-preferences/{id}`
- **Description**: Updates user program preferences
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated preferences object
- **Response**: Updated preferences object

#### Delete User Program Preferences
- **DELETE** `/user-program-preferences/{id}`
- **Description**: Deletes user program preferences
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

## Data Models

### User
```json
{
  "id": 1,
  "name": "John Doe",
  "age": 30,
  "height": 175.5,
  "weight": 80.0,
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### Program
```json
{
  "id": 1,
  "name": "Conjugate Powerlifting Program",
  "description": "A comprehensive powerlifting program using the conjugate method",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### Exercise
```json
{
  "id": 1,
  "name": "Bench Press",
  "description": "Compound upper body pushing exercise",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### UserProgramPreferences
```json
{
  "id": 1,
  "user_id": 1,
  "program_days_per_week": 3,
  "session_time_length": 60,
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

## Error Responses

### Validation Error (422)
```json
{
  "error": "Validation failed",
  "message": "User age must be between 1 and 150, got: 0",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Not Found Error (404)
```json
{
  "error": "Not Found",
  "message": "User not found",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Internal Server Error (500)
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Validation Rules

### User Validation
- **Name**: Required, non-empty string, max 255 characters
- **Age**: Required, integer between 1 and 150
- **Height**: Required, decimal between 0.01 and 300.0 cm
- **Weight**: Required, decimal between 0.01 and 1000.0 kg

### Program Validation
- **Name**: Required, non-empty string, max 255 characters
- **Description**: Optional string, max 1000 characters

### Exercise Validation
- **Name**: Required, non-empty string, max 255 characters
- **Description**: Optional string, max 1000 characters

### UserProgramPreferences Validation
- **User ID**: Required, must reference existing user
- **Program Days Per Week**: Required, must be 2, 3, or 4
- **Session Time Length**: Required, integer between 15 and 300 minutes

## Rate Limiting

API requests are rate-limited to ensure fair usage. Please implement appropriate retry logic in your applications.

## Authentication

Currently, the API does not require authentication. All endpoints are publicly accessible.

---

*This documentation was automatically generated on $(date)*
