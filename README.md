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
- Minikube (for local Kubernetes development)
- kubectl (Kubernetes command-line tool)
- Skaffold (for streamlined development)
- Docker (for containerization)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd congen
   ```

2. **Set up Kubernetes environment**
   ```bash
   # Run the setup script
   ./scripts/setup-kubernetes.sh
   ```

3. **Build and deploy the application**
   ```bash
   # Build Docker image using JIB and deploy to Kubernetes
   ./gradlew buildDockerImage deployToLocal
   
   # Or use Skaffold for development (watches for changes)
   ./gradlew skaffoldDev
   ```

4. **Access the application**
   ```bash
   # Get Minikube IP
   minikube ip
   
   # Access the application (NodePort 30080)
   curl http://$(minikube ip):30080/actuator/health
   
   # Or use port forwarding
   kubectl port-forward -n congen service/congen 8080:8080
   ```

5. **Run tests**
   ```bash
   # Unit tests
   ./gradlew test
   
   # Integration tests (requires PostgreSQL)
   ./gradlew integrationTest
   
   # All tests
   ./gradlew check
   ```

### Alternative: Traditional Local Development

If you prefer to run the application locally without Kubernetes:

1. **Set up PostgreSQL**
   - Install PostgreSQL
   - Create a database named `congen`
   - Update `application.properties` with your connection details

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

### Configuration

The application supports multiple profiles:

- **Development**: `application.properties` (default)
- **Test**: `application-test.properties`
- **Staging**: `application-staging.properties`
- **Production**: `application-prod.properties`

### Kubernetes Deployment

The application is designed to run on Kubernetes with environment-specific configurations:

**Note**: Database migrations are automatically generated from Liquibase files during deployment. The `k8s/base/migrations-configmap.yaml` file is auto-generated and should not be manually edited.

- **Local Development**: `k8s/overlays/local/`
- **Staging**: `k8s/overlays/staging/`
- **Production**: `k8s/overlays/production/`

For detailed Kubernetes deployment instructions, see [KUBERNETES_DEPLOYMENT.md](KUBERNETES_DEPLOYMENT.md).

#### Quick Kubernetes Commands

```bash
# Deploy to local environment
./gradlew deployToLocal

# Deploy to staging
kubectl apply -k k8s/overlays/staging

# Deploy to production
kubectl apply -k k8s/overlays/production

# Clean up
./gradlew skaffoldDelete
```

#### Using the Deployment Script

For a more streamlined experience, use the comprehensive deployment script:

```bash
# Deploy to local environment
./scripts/deploy.sh local

# Deploy to staging
./scripts/deploy.sh staging

# Deploy to production
./scripts/deploy.sh production

# Check deployment status
./scripts/deploy.sh status

# View logs
./scripts/deploy.sh logs

# Clean up
./scripts/deploy.sh cleanup --env local
```

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

For detailed API documentation, see [api-documentation.md](docs/api-documentation.md).

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

# Run only integration tests (TestContainers)
./gradlew integrationTest

# Run integration tests against Kubernetes
./gradlew kubernetesIntegrationTest

# Run all integration tests (TestContainers + Kubernetes)
./gradlew allIntegrationTests

# Check Kubernetes test environment status
./gradlew checkKubernetesTestEnv

# Cleanup Kubernetes test environment
./gradlew cleanupKubernetesTestEnv

# Run tests with coverage
./gradlew jacocoTestReport
```

### Integration Testing with Kubernetes

The application supports multiple integration testing approaches:

#### Option 1: TestContainers (Recommended for Development)
```bash
# Fast, isolated testing
./gradlew integrationTest
```

#### Option 2: Kubernetes Integration Testing
```bash
# Test against actual Kubernetes deployment (automatic setup)
./gradlew kubernetesIntegrationTest

# Or use the script for more control
./scripts/run-kubernetes-tests.sh kubernetes
```

#### Option 3: All Integration Tests
```bash
# Run both TestContainers and Kubernetes tests
./gradlew allIntegrationTests

# Or use the script
./scripts/run-kubernetes-tests.sh all
```

For detailed information about integration testing, see [INTEGRATION_TESTING_KUBERNETES.md](docs/INTEGRATION_TESTING_KUBERNETES.md).

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

For detailed information about the migration system, see [DATABASE_MIGRATIONS.md](docs/DATABASE_MIGRATIONS.md).

**Quick Reference**: [MIGRATION_QUICK_REFERENCE.md](docs/MIGRATION_QUICK_REFERENCE.md)

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
./gradlew buildDockerImage

# Or manually
docker build -t congen .
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
├── k8s/                    # Kubernetes manifests
├── scripts/                # Utility scripts
└── skaffold*.yaml          # Skaffold configuration
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
