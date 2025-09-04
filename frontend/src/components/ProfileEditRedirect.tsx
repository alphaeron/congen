import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { Box, Typography, Alert, Button } from '@mui/material'
import { updateUserProfile } from '../api/user'
import { LoadingSpinner } from './LoadingSpinner'

/**
 * Component to handle redirects back from Keycloak after profile edits.
 *
 * This component checks if the user has returned from a profile edit operation
 * and syncs any changes with the backend before redirecting them back to
 * their original location in the application.
 */
export const ProfileEditRedirect: React.FC = () => {
  const navigate = useNavigate()
  const auth = useAuth()
  const [isSyncing, setIsSyncing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const syncProfileChanges = async () => {
      try {
        setIsSyncing(true)
        setError(null)

        // Check if we have a stored redirect path from a profile edit operation
        const redirectPath = sessionStorage.getItem('congen_redirect_after_profile_edit')

        if (redirectPath) {
          // Clear the stored path
          sessionStorage.removeItem('congen_redirect_after_profile_edit')

          // Get the user's current profile from Keycloak token
          if (auth.user?.profile) {
            const { name } = auth.user.profile
            
            // Sync the profile changes with the backend
            if (name) {
              await updateUserProfile({ name })
            }
          }

          // Redirect back to the original location
          navigate(redirectPath, { replace: true })
        } else {
          // No redirect path found, go to profile page
          navigate('/profile', { replace: true })
        }
      } catch (err) {
        console.error('Error syncing profile changes:', err)
        setError('Failed to sync profile changes. Please try again.')
      } finally {
        setIsSyncing(false)
      }
    }

    // Wait a moment for the auth context to be ready
    const timer = setTimeout(syncProfileChanges, 100)
    return () => clearTimeout(timer)
  }, [navigate, auth.user])

  if (error) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button variant="contained" onClick={() => navigate('/profile')}>
          Go to Profile
        </Button>
      </Box>
    )
  }

  if (isSyncing) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
        <LoadingSpinner />
        <Typography variant="h6">Syncing profile changes...</Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center">
          Please wait while we sync your profile changes with the application.
        </Typography>
      </Box>
    )
  }

  return (
    <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
      <Typography variant="h6">Redirecting...</Typography>
      <Typography variant="body2" color="text.secondary" textAlign="center">
        Please wait while we process your request.
      </Typography>
    </Box>
  )
}
