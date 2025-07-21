/**
 * Deployment environment.  Default to loc/local.
 */
export const DEPLOYMENT_ENVIRONMENT =
  process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT ?? "loc";
