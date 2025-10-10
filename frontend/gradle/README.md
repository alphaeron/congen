# Frontend Gradle Integration

Modular Gradle build system for the frontend project using `gradle-node-plugin`.

## Quick Start

```bash
# Development
./gradlew :frontend:npm_run_start                    # Start dev server
./gradlew :frontend:buildApp -Penvironment=local     # Debug build
./gradlew :frontend:buildApp -Penvironment=production # Production build

# Testing & Quality
./gradlew :frontend:test                             # Run tests
./gradlew :frontend:lint                             # Run linting
./gradlew :frontend:checkApp                         # All checks

# Deployment
./gradlew :frontend:prepareDeployment -Penvironment=production
```

## Key Tasks

| Task                | Description                          | Usage                                                     |
| ------------------- | ------------------------------------ | --------------------------------------------------------- |
| `buildApp`          | Environment-aware build              | `-Penvironment=local\|local-persist\|staging\|production` |
| `dockerBuild`       | Build Docker image                   | `-Penvironment=local\|local-persist\|staging\|production` |
| `prepareDeployment` | Build + patch manifests              | `-Penvironment=local\|local-persist\|staging\|production` |
| `test`              | Run tests                            | `./gradlew :frontend:test`                                |
| `lint`              | Run linting                          | `./gradlew :frontend:lint`                                |
| `checkApp`          | All checks (test + lint + typeCheck) | `./gradlew :frontend:checkApp`                            |
| `npm_run_start`     | Start dev server                     | `./gradlew :frontend:npm_run_start`                       |

## Root Project Aliases

From root directory: `./gradlew frontendStart`, `./gradlew frontendBuild`, `./gradlew frontendTest`, etc.

## Multi-Project

`./gradlew build` - Build all projects  
`./gradlew check` - Run all checks  
`./gradlew clean` - Clean all projects

## Modules

| File            | Purpose                 |
| --------------- | ----------------------- |
| `node.gradle`   | Node.js/npm tasks       |
| `test.gradle`   | Testing tasks           |
| `style.gradle`  | Code quality tasks      |
| `deploy.gradle` | Docker/deployment tasks |

**Node.js**: 20.11.0, **npm**: 10.2.4 (auto-managed by gradle-node-plugin)

## Workflows

### Development

```bash
./gradlew :frontend:npm_run_start     # Start dev server
./gradlew :frontend:test              # Run tests
./gradlew :frontend:lint              # Run linting
./gradlew :frontend:checkApp          # All checks
```

### Deployment

```bash
./gradlew :frontend:prepareDeployment -Penvironment=production
kubectl apply -k k8s/overlays/production/
```

### CI/CD

```bash
./gradlew :frontend:checkApp          # Lint + test + typeCheck
./gradlew :frontend:buildApp -Penvironment=production
```

## Troubleshooting

**Node.js not found**: Auto-downloaded by gradle-node-plugin  
**Build fails**: Check `dist/` directory exists after build  
**Plugin errors**: Declare plugins in main `build.gradle`, not modules

## Migration

| npm script      | Gradle task                                             |
| --------------- | ------------------------------------------------------- |
| `npm start`     | `./gradlew :frontend:npm_run_start`                     |
| `npm run build` | `./gradlew :frontend:buildApp -Penvironment=production` |
| `npm run test`  | `./gradlew :frontend:test`                              |
| `npm run lint`  | `./gradlew :frontend:lint`                              |
