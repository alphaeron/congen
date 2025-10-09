# Congen Frontend

A React/TypeScript frontend application for the Conjugate Workout Generator, featuring Material-UI components, Keycloak authentication, and comprehensive fitness tracking capabilities.

## Features

- **Authentication**: Keycloak integration with OAuth2 authorization code flow
- **Dashboard**: User profile management and workout tracking
- **Exercise Library**: Browse and search exercises with detailed information
- **Performance Tracking**: Gamified fitness metrics with HP/MP/Fatigue scoring
- **Responsive Design**: Mobile-friendly interface with Material-UI components
- **Cookie Management**: GDPR-compliant cookie consent system

## Technology Stack

- **React** with TypeScript
- **Material-UI** for UI components
- **React Router** for navigation
- **Axios** for API communication
- **React OIDC Context** for authentication
- **Webpack** for bundling
- **Jest** for testing

## Available Scripts

### `npm start`

Runs the app in development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.\
You will also see any lint errors in the console.

### `npm test`

Launches the test runner in interactive watch mode.\
Runs Jest tests with React Testing Library.

### `npm run build`

Builds the app for production to the `dist` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.\
Your app is ready to be deployed!

### `npm run lint`

Runs ESLint to check for code quality issues.

### `npm run lint:fix`

Runs ESLint and automatically fixes fixable issues.

## Development Setup

### Prerequisites

- Node.js 18+ and npm
- Backend API running (see [backend/README.md](../backend/README.md))
- Keycloak instance configured (see [docs/KEYCLOAK_INTEGRATION.md](../docs/KEYCLOAK_INTEGRATION.md))

### Installation

1. **Install dependencies**:

   ```bash
   npm install
   ```

2. **Configure environment**:
   The app uses environment variables for configuration. See the authentication setup in [src/auth/OidcConfig.ts](src/auth/OidcConfig.ts).

3. **Start development server**:
   ```bash
   npm start
   ```

## Project Structure

```
src/
├── api/                    # API client functions
├── auth/                   # Authentication configuration
├── components/             # Reusable UI components
├── contexts/               # React contexts (Auth, Data, Cookies)
├── hooks/                  # Custom React hooks
├── pages/                  # Page components
├── resources/              # Static assets (images, icons)
├── styles/                 # Global styles and CSS
├── theme/                  # Material-UI theme configuration
└── utils/                  # Utility functions
```

## Key Components

### Authentication

- **OidcConfig**: Keycloak configuration
- **AuthContext**: Authentication state management
- **ProtectedRoute**: Route protection wrapper
- **AuthCallback**: OAuth2 callback handler

### Dashboard

- **Dashboard**: Main dashboard with sidebar navigation
- **DashboardOverview**: Performance metrics and overview
- **ProgramManagement**: Workout program management
- **WorkoutsOverview**: Workout tracking and history

### User Management

- **UserProfile**: User profile editing and preferences
- **ExerciseOverview**: Exercise library browser
- **ExerciseDetails**: Detailed exercise information

## API Integration

The frontend communicates with the backend API through:

- **Axios HTTP client** with interceptors
- **Type-safe API functions** in `src/api/`
- **Automatic token refresh** via OIDC context
- **Error handling** with user-friendly messages

## Testing

The project uses Jest and React Testing Library for testing:

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage
```

## Building for Production

```bash
# Build the application
npm run build

# The built files will be in the dist/ directory
```

## Contributing

1. Follow the existing code style and patterns
2. Write tests for new components and features
3. Update documentation for API changes
4. Ensure all tests pass before submitting PR

## Learn More

- [React Documentation](https://reactjs.org/)
- [Material-UI Documentation](https://mui.com/)
- [React Router Documentation](https://reactrouter.com/)
- [React OIDC Context Documentation](https://github.com/authts/react-oidc-context)
