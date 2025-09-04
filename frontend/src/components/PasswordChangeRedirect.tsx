import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';

/**
 * Component to handle redirects back from Keycloak after password changes.
 * 
 * This component checks if the user has returned from a password change operation
 * and redirects them back to their original location in the application.
 */
export const PasswordChangeRedirect: React.FC = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Check if we have a stored redirect path from a password change operation
    const redirectPath = sessionStorage.getItem('congen_redirect_after_password_change');
    
    if (redirectPath) {
      // Clear the stored path
      sessionStorage.removeItem('congen_redirect_after_password_change');
      
      // Redirect back to the original location
      navigate(redirectPath, { replace: true });
    }
  }, [navigate]);

  // This component doesn't render anything visible
  return null;
};
