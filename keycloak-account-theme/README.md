# Congen Keycloak Account Theme

A custom React-based account theme for Keycloak using Material-UI components that matches the Congen application design.

## Overview

This project provides a custom Keycloak account theme that:

- **Custom React Components**: Uses Material-UI components with Congen's design system
- **Account Management**: Custom account overview with user profile, security settings, and quick actions
- **Responsive Design**: Mobile-friendly interface that matches the main Congen application
- **Theme Integration**: Seamlessly integrates with Keycloak's deployment system

## Architecture

### Theme Structure

```
keycloak-account-theme/
├── src/
│   ├── account/
│   │   ├── KcPage.tsx          # Main account page component
│   │   ├── KcContext.ts        # Keycloak context types
│   │   └── i18n.ts            # Internationalization
│   ├── main.tsx               # Application entry point
│   └── theme.tsx              # Material-UI theme definition
├── scripts/
│   ├── build-theme.sh         # Custom theme build script
│   └── update-k8s-configmap.sh # Kubernetes ConfigMap updater
├── dist_keycloak/             # Generated theme files
│   ├── congen-account-theme.jar
│   └── themes/                # Theme directory structure
└── public/                    # Static assets
```

### Build Process

The project uses a **custom build process** instead of the standard keycloakify build due to compatibility issues:

1. **Webpack Build**: Compiles React components and generates optimized bundles
2. **Theme Assembly**: Creates proper Keycloak theme structure with FreeMarker templates
3. **JAR Packaging**: Packages the theme as a JAR file for Keycloak deployment
4. **Kubernetes Integration**: Updates ConfigMaps for deployment

## Development

### Prerequisites

- Node.js 20.x or later
- npm or yarn
- Gradle (for deployment)

### Setup

```bash
# Install dependencies
npm install

# Build the theme
npm run build-keycloak-theme

# Or use the custom build script directly
./scripts/build-theme.sh
```

### Development Workflow

1. **Make Changes**: Edit React components in `src/`
2. **Build Theme**: Run `./scripts/build-theme.sh`
3. **Update ConfigMap**: Run `./scripts/update-k8s-configmap.sh`
4. **Deploy**: Use Gradle deployment tasks

### Key Components

#### CongenAccountOverview

The main account overview component (`src/account/KcPage.tsx`) provides:

- **Personal Information Card**: User profile details
- **Security Settings Card**: Password and 2FA management
- **Quick Actions Grid**: Common account actions

#### Theme Configuration

The Material-UI theme (`src/theme.tsx`) matches the main Congen application:

- **Primary Color**: Blue (#0ea5e9)
- **Secondary Color**: Orange (#f97316)
- **Typography**: Inter font family
- **Component Styling**: Rounded corners, modern shadows

## Deployment

### Automatic Deployment

```bash
# Build and update Kubernetes resources
./gradlew :keycloak-account-theme:build

# Deploy to specific environment
./gradlew deployment -Penvironment=staging
```

### Manual Deployment

```bash
# 1. Build the theme
./scripts/build-theme.sh

# 2. Update Kubernetes ConfigMap
./scripts/update-k8s-configmap.sh

# 3. Apply Kubernetes resources
kubectl apply -k k8s/overlays/staging
```

### Theme Files Generated

The build process generates:

- **congen-account-theme.jar**: Complete theme package
- **FreeMarker Templates**: Account page templates (.ftl files)
- **Static Resources**: JavaScript, CSS, and image files
- **theme.properties**: Keycloak theme configuration

## Kubernetes Integration

### ConfigMaps

The theme uses two ConfigMaps:

1. **keycloak-theme**: CSS-based login theme and fallback account theme
2. **keycloak-account-theme-jar**: React-based account theme JAR

### Deployment Process

The Keycloak StatefulSet includes an init container that:

1. Extracts the theme JAR to `/opt/keycloak/themes/`
2. Sets up the CSS-based login theme
3. Configures proper file permissions
4. Provides fallback if JAR is not available

### Theme Selection

In Keycloak Admin Console:

- **Login Theme**: `congen` (CSS-based)
- **Account Theme**: `congen-account-theme` (React-based)

## Customization

### Adding New Account Pages

1. Create new React components in `src/account/`
2. Update the switch statement in `KcPage.tsx`
3. Add corresponding FreeMarker templates in the build script

### Styling Updates

1. Modify the theme configuration in `src/theme.tsx`
2. Update component styles using Material-UI's sx prop
3. Rebuild and redeploy

### Internationalization

1. Add translations to `src/account/i18n.ts`
2. Use the `useI18n` hook in components
3. Update message files in the build script

## Troubleshooting

### Common Issues

#### Theme Not Loading

- Check Keycloak logs for theme extraction errors
- Verify ConfigMap contains the JAR file
- Ensure proper file permissions in init container

#### Styling Issues

- Check browser console for JavaScript errors
- Verify all required static resources are loaded
- Test with different browsers and screen sizes

#### Build Failures

- Ensure all dependencies are installed
- Check Node.js version compatibility
- Verify webpack configuration

### Debug Commands

```bash
# Check theme JAR contents
unzip -l dist_keycloak/congen-account-theme.jar

# Verify Kubernetes ConfigMap
kubectl get configmap keycloak-account-theme-jar -o yaml

# Check Keycloak pod logs
kubectl logs -f keycloak-0 -c theme-setup
kubectl logs -f keycloak-0 -c keycloak
```

## Performance Optimization

### Bundle Optimization

The current build generates:

- **Runtime**: ~2KB
- **React**: ~346KB
- **MUI**: ~141KB
- **Vendors**: ~447KB
- **Theme Code**: ~13KB

### Future Improvements

1. **Code Splitting**: Implement lazy loading for account pages
2. **Tree Shaking**: Optimize Material-UI imports
3. **Caching**: Add proper cache headers for static resources
4. **Compression**: Enable gzip compression for assets

## Testing

### Local Testing

```bash
# Run webpack dev server
npm start

# Run tests
npm test

# Check bundle analyzer
npm run build && npx webpack-bundle-analyzer dist/
```

### Integration Testing

1. Deploy to staging environment
2. Test all account pages and features
3. Verify responsive design on different devices
4. Check browser compatibility

## Contributing

### Code Standards

- Use TypeScript for all new components
- Follow Material-UI component patterns
- Maintain responsive design principles
- Add proper error handling and loading states

### Pull Request Process

1. Create feature branch from main
2. Implement changes with tests
3. Update documentation as needed
4. Test deployment in staging environment
5. Submit pull request with detailed description

## License

This project is part of the Congen application and follows the same licensing terms.
