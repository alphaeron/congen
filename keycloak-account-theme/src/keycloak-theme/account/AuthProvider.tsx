import React from 'react';
import { AuthProvider as OidcAuthProvider } from 'react-oidc-context';
import { getAuthProviderConfig } from './oidcConfig';
import { AuthProvider as CustomAuthProvider } from './AuthContext';

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const authConfig = getAuthProviderConfig();

  return (
    <OidcAuthProvider {...authConfig}>
      <CustomAuthProvider>
        {children}
      </CustomAuthProvider>
    </OidcAuthProvider>
  );
};
