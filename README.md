# Conjugate Workout Generator (Congen)

A Kotlin-based Spring Boot application that generates personalized workout programs using the conjugate method. This application provides a RESTful API for managing exercises, programs, users, and their preferences.

## 🏗️ Architecture

The application follows a layered architecture pattern:

```
┌─────────────────┐
│   Controllers   │  ← REST API endpoints
├─────────────────┤
│    Services     │  ← Business logic
├─────────────────┤
│      DALs       │  ← Data access layer
├─────────────────┤
│   PostgreSQL    │  ← Database
└─────────────────┘
```

### Key Components

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic and orchestration
- **DALs (Data Access Layer)**: Manage database operations
- **Models**: Define data structures and validation
- **Utilities**: Shared validation and helper functions
- **Configuration**: Application and database configuration

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Gradle 8.0 or higher
- PostgreSQL 12 or higher
- Docker (optional, for containerized development)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd congen
   ```

2. **Set up PostgreSQL**
   
   **Option A: Using Docker (Recommended)**
   ```bash
   docker-compose up
   ```
   
   **Option B: Local PostgreSQL**
   - Install PostgreSQL
   - Create a database named `congen`
   - Update `application.properties` with your connection details

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Run tests**
   ```bash
   # Unit tests
   ./gradlew test
   
   # Integration tests (requires PostgreSQL)
   ./gradlew integrationTest
   
   # All tests
   ./gradlew check
   ```

### Configuration

The application supports multiple profiles:

- **Development**: `application.properties` (default)
- **Test**: `application-test.properties`
- **Staging**: `application-staging.properties`
- **Production**: `application-prod.properties`

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication
Currently, the API does not require authentication. All endpoints are publicly accessible.

### Common Response Formats

#### Success Response
```json
{
  "id": 1,
  "name": "Example",
  "created_at": "2024-01-01T00:00:00Z"
}
```

#### Error Response
```json
{
  "error": "Validation failed",
  "message": "User age must be between 1 and 150, got: 0",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### HTTP Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `404` - Not Found
- `409` - Conflict
- `422` - Unprocessable Entity
- `500` - Internal Server Error

### Core Endpoints

#### Users
- `GET /user/` - Get all users
- `GET /user/{id}` - Get user by ID
- `POST /user/` - Create user
- `PUT /user/{id}` - Update user
- `DELETE /user/{id}` - Delete user

#### Programs
- `GET /program/` - Get all programs
- `GET /program/{id}` - Get program by ID
- `POST /program/` - Create program
- `PUT /program/{id}` - Update program
- `DELETE /program/{id}` - Delete program

#### Exercises
- `GET /exercise/` - Get all exercises
- `GET /exercise/{id}` - Get exercise by ID
- `POST /exercise/` - Create exercise
- `PUT /exercise/{id}` - Update exercise
- `DELETE /exercise/{id}` - Delete exercise

#### User Program Preferences
- `GET /user-program-preferences/{userId}` - Get user's program preferences
- `POST /user-program-preferences/` - Create user program preferences
- `PUT /user-program-preferences/{id}` - Update user program preferences
- `DELETE /user-program-preferences/{id}` - Delete user program preferences

For detailed API documentation, see [API.md](docs/API.md).

## 🧪 Testing

### Test Structure

- **Unit Tests**: Located in `src/test/` - Test individual components in isolation
- **Integration Tests**: Located in `src/integrationTest/` - Test full application with real database

### Running Tests

```bash
# Run all tests
./gradlew check

# Run only unit tests
./gradlew test

# Run only integration tests
./gradlew integrationTest

# Run tests with coverage
./gradlew jacocoTestReport
```

### Test Database

Integration tests use a separate PostgreSQL database configured in `application-test.properties`. The test database is automatically created and cleaned between test runs.

## 🗄️ Database

### Schema Overview

The application uses PostgreSQL with the following main tables:

- `users` - User information and profiles
- `programs` - Workout programs
- `exercises` - Individual exercises
- `equipment` - Available equipment
- `muscles` - Muscle groups
- `user_program_preferences` - User preferences for programs
- `user_exercise_preferences` - User preferences for exercises
- `user_equipment` - Equipment available to users

### Migrations

Database schema changes are managed using Liquibase migrations located in `resources/migrations/`. Each migration is versioned and includes both schema changes and data population scripts.

## 🔧 Development

### Code Style

The project follows Kotlin coding conventions and uses ktlint for code formatting:

```bash
# Format code
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck
```

### Building

```bash
# Build the application
./gradlew build

# Build without tests
./gradlew build -x test

# Create executable JAR
./gradlew bootJar
```

### Docker

```bash
# Build Docker image
docker build -t congen .

# Run with Docker Compose
docker-compose up
```

## 📁 Project Structure

```
congen/
├── src/
│   ├── main/kotlin/com/congen/
│   │   ├── controllers/     # REST API endpoints
│   │   ├── service/         # Business logic
│   │   ├── dal/            # Data access layer
│   │   ├── model/          # Data models
│   │   ├── util/           # Utilities and validation
│   │   ├── config/         # Configuration classes
│   │   ├── exceptions/     # Custom exceptions
│   │   ├── components/     # Spring components
│   │   └── client/         # Database client
│   ├── test/               # Unit tests
│   └── integrationTest/    # Integration tests
├── resources/
│   ├── migrations/         # Database migrations
│   └── application-*.properties
├── docs/                   # Documentation
└── docker-compose.yml
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Write tests for new features
- Follow existing code style
- Update documentation for API changes
- Add migration scripts for database changes
- Ensure all tests pass before submitting PR

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For questions, issues, or contributions, please:

1. Check the [documentation](docs/)
2. Search existing [issues](../../issues)
3. Create a new issue with detailed information

## 🔄 Version History

- **v1.0.0** - Initial release with core workout generation functionality
- **v1.1.0** - Added user preferences and program customization
- **v1.2.0** - Enhanced validation and error handling

For detailed changelog, see [CHANGELOG.md](CHANGELOG.md).
