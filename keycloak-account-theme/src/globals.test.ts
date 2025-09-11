// Mock process.env
const originalEnv = process.env;

describe('globals', () => {
  beforeEach(() => {
    jest.resetModules();
    process.env = { ...originalEnv };
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  describe('DEPLOYMENT_ENVIRONMENT', () => {
    it('should default to local when REACT_APP_DEPLOYMENT_ENVIRONMENT is not set', async () => {
      delete process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT;
      const { DEPLOYMENT_ENVIRONMENT } = await import('./globals');
      expect(DEPLOYMENT_ENVIRONMENT).toBe('local');
    });

    it('should use REACT_APP_DEPLOYMENT_ENVIRONMENT when set', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'staging';
      const { DEPLOYMENT_ENVIRONMENT } = await import('./globals');
      expect(DEPLOYMENT_ENVIRONMENT).toBe('staging');
    });

    it('should use REACT_APP_DEPLOYMENT_ENVIRONMENT when set to production', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'production';
      const { DEPLOYMENT_ENVIRONMENT } = await import('./globals');
      expect(DEPLOYMENT_ENVIRONMENT).toBe('production');
    });
  });

  describe('BASE_URL', () => {
    it('should return local URL for local environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'local';
      const { BASE_URL } = await import('./globals');
      expect(BASE_URL).toBe('http://localhost');
    });

    it('should return staging URL for staging environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'staging';
      const { BASE_URL } = await import('./globals');
      expect(BASE_URL).toBe('https://staging.congen.com');
    });

    it('should return production URL for production environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'production';
      const { BASE_URL } = await import('./globals');
      expect(BASE_URL).toBe('https://congen.com');
    });

    it('should default to localhost for unknown environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'unknown';
      const { BASE_URL } = await import('./globals');
      expect(BASE_URL).toBe('http://localhost');
    });
  });

  describe('KEYCLOAK_URL', () => {
    it('should include port 8080 for local environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'local';
      const { KEYCLOAK_URL } = await import('./globals');
      expect(KEYCLOAK_URL).toBe('http://localhost:8080');
    });

    it('should not include port for staging environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'staging';
      const { KEYCLOAK_URL } = await import('./globals');
      expect(KEYCLOAK_URL).toBe('https://staging.congen.com');
    });

    it('should not include port for production environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'production';
      const { KEYCLOAK_URL } = await import('./globals');
      expect(KEYCLOAK_URL).toBe('https://congen.com');
    });
  });

  describe('BACKEND_URL', () => {
    it('should include port 8888 for local environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'local';
      const { BACKEND_URL } = await import('./globals');
      expect(BACKEND_URL).toBe('http://localhost:8888');
    });

    it('should not include port for staging environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'staging';
      const { BACKEND_URL } = await import('./globals');
      expect(BACKEND_URL).toBe('https://staging.congen.com');
    });

    it('should not include port for production environment', async () => {
      process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'production';
      const { BACKEND_URL } = await import('./globals');
      expect(BACKEND_URL).toBe('https://congen.com');
    });
  });
});
