# Security Checks

This document describes the security checks available in the Congen project. These checks are designed to identify common security pitfalls across the entire application.

## Overview

The project uses established static analysis tools instead of custom shell scripts for better maintainability and reliability. Security checks are integrated with your existing Detekt and ESLint configurations.

## Available Security Tasks

### Quick Security Check
```bash
./gradlew checkVulnerableDependencies
```
Runs dependency vulnerability scanning.

**Checks performed:**
- OWASP dependency vulnerability scanning for backend and frontend
- Detekt with security rules (already configured in your existing setup)

### Full Security Audit
```bash
./gradlew securityAudit
```
Runs comprehensive security audit including dependency vulnerability scanning.

**Additional checks:**
- OWASP dependency vulnerability scanning (with 5-minute timeout per project)
- Detailed HTML reports in `build/security-reports/`

**Note:** The dependency vulnerability scanning can take several minutes and may timeout.

## Individual Security Tasks

You can run individual security checks as needed:

```bash
# Check for vulnerable dependencies (requires OWASP dependency-check)
./gradlew checkVulnerableDependencies
```

## Integration with Build Process

The security checks are automatically integrated into the build process:

- **`./gradlew check`** - Includes `securityAudit`
- **`./gradlew build`** - Depends on `check`, so includes security audit
- **`./gradlew checkVulnerableDependencies`** - Run dependency vulnerability scanning (standalone)
- **`./gradlew securityAudit`** - Run comprehensive security audit (standalone)

## Integration with Existing Tools

### Backend (Kotlin/Java)
- **Detekt**: Already configured with security rules added to `backend/detekt.yml`
- **ktlint**: Already configured for code formatting

### Frontend (TypeScript/JavaScript)
- **ESLint**: Already configured in your existing setup

## Required Tools

Some security checks require external tools to be installed:

### OWASP Dependency Check
For comprehensive dependency vulnerability scanning:
```bash
# macOS
brew install dependency-check

# Ubuntu/Debian
sudo apt-get install dependency-check

# Or download from: https://owasp.org/www-project-dependency-check/
```



## Security Check Details

### Detekt Security Rules
Security rules are included in [`backend/detekt.yml`](../backend/detekt.yml) under the `potential-bugs` rule set.

### OWASP Dependency Check
Scans for known vulnerabilities in:
- Backend dependencies (Gradle)
- Frontend dependencies (npm)
- Generates detailed HTML reports

## Reports

When running vulnerability scans, HTML reports are generated in `build/security-reports/`:
- `backend-dependencies.html` - Backend dependency vulnerabilities
- `frontend-dependencies.html` - Frontend dependency vulnerabilities

## Configuration

### Exclusions
The security checks automatically exclude:
- `.git/` directory
- `.gradle/` directory
- `build/` directories
- `node_modules/` directories
- `.terraform/` directory
- `.idea/` directory
- `target/` directory
- `dist/` directory

### Performance

- `checkVulnerableDependencies` may take 5-10 minutes depending on project size
- `securityAudit` may take 5-10 minutes depending on project size

## Best Practices

1. **Run security checks regularly** - Include in CI/CD pipelines
2. **Address high and critical findings** - Fix vulnerabilities promptly
3. **Review moderate findings** - Assess risk and fix as appropriate
4. **Keep dependencies updated** - Regular updates reduce vulnerability exposure
5. **Use existing tools** - Leverage your existing Detekt and ESLint configurations

## Troubleshooting

### Common Issues

**"dependency-check not found"**
- Install OWASP dependency-check tool
- See installation instructions above

**False positives**
- Security checks may flag legitimate code
- Review findings and adjust exclusions if needed
- Update patterns in `gradle/security.gradle` if necessary

## Benefits of This Approach

1. **Maintainable**: Uses established tools instead of custom shell scripts
2. **Reliable**: Leverages battle-tested static analysis engines
3. **Integrated**: Works with your existing Detekt and ESLint setup
4. **Focused**: Concentrates on dependency vulnerability scanning which is critical for security
5. **Comprehensive**: Provides detailed HTML reports for vulnerability analysis 