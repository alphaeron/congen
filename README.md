# Conjugate Workout Generator (Congen)

A monorepo for the Conjugate Workout Generator project, which provides a full-stack solution for generating personalized workout programs using the conjugate method. This repository contains both the backend (Kotlin/Spring Boot) and the frontend (Node.js/TypeScript) components, as well as shared infrastructure and documentation.

## Project Structure

```
congen/
├── backend/    # Kotlin/Spring Boot backend API and business logic
├── frontend/   # Node.js/TypeScript frontend (UI)
├── k8s/        # Shared Kubernetes manifests and overlays
├── gradle/     # Shared Gradle scripts
├── README.md   # Project-level documentation (this file)
└── ...         # Other shared files and configs
```

## Components

- **backend/**: Contains the Spring Boot API, business logic, database migrations, and backend documentation. See [backend/README.md](backend/README.md) for full backend details, setup, and API documentation.
- **frontend/**: Contains the React/TypeScript frontend application with Material-UI components, authentication, and dashboard functionality.
- **k8s/**: Shared Kubernetes manifests and overlays for deploying the backend (and eventually frontend) to local, staging, or production environments.

## Quick Start

### Prerequisites
- Java 17+ (for backend)
- Node.js 18+ and npm (for frontend)
- Gradle 8+
- Docker
- Minikube and kubectl (for local Kubernetes deployment/testing, automatically used by Gradle tasks)

### Backend
See [backend/README.md](backend/README.md) for full instructions.

Common backend Gradle tasks (run from the project root):
```bash
# Build the backend
./gradlew :backend:build

# Run backend tests
./gradlew :backend:test

# Deploy backend to Kubernetes (local)
./gradlew deployAll -Penvironment=local
```

### Frontend
The frontend is a React application with TypeScript and Material-UI. See [frontend/README.md](frontend/README.md) for full setup instructions.

Quick start:
```bash
cd frontend
npm install
npm start
```

The frontend includes:
- **Authentication**: Keycloak integration with OAuth2
- **Dashboard**: User profile management and workout tracking
- **Exercise Library**: Browse and search exercises
- **Performance Tracking**: Gamified fitness metrics and scoring
- **Responsive Design**: Mobile-friendly interface

## Building and Running
- All backend Gradle tasks must be called with the `:backend:` prefix from the project root (e.g., `./gradlew :backend:build`).
- Frontend tasks are run using npm/yarn inside the `frontend/` directory.

## Contributing
- See [backend/README.md](backend/README.md) for backend contribution guidelines.
- See [frontend/README.md](frontend/README.md) for frontend contribution guidelines.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
