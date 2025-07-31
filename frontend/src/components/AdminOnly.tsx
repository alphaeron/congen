import React from 'react';
import { AuthorizedElement } from './AuthorizedElement';

interface AdminOnlyProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * AdminOnly component that only renders content for users with admin role.
 * 
 * This is an example of how to use role-based authorization with groups/roles.
 * 
 * @param children The content to render for admin users
 * @param fallback The content to render for non-admin users (optional)
 * @return The authorized element component
 */
export const AdminOnly: React.FC<AdminOnlyProps> = ({ children, fallback }) => {
  return (
    <AuthorizedElement roles={['admin']} fallback={fallback}>
      {children}
    </AuthorizedElement>
  );
};