import { Box, Button, CardContent, CircularProgress, Grid } from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React from 'react';

import { FormField } from './FormField';
import { GameText, GameCard, GameButton, GAME_CLASSES } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { updateUserProfile, getCurrentUser } from '../api/user';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

/**
 * Physical attributes section component for user profile.
 *
 * This component allows users to manage their physical attributes including
 * age, weight, height, and gender. All data is encrypted at rest for GDPR compliance.
 *
 * @return Physical attributes section component
 */
interface PhysicalAttributesFormData {
  age: number | '';
  weight: number | '';
  height: number | '';
  gender: string;
}

export function PhysicalAttributesSection(): React.ReactElement {
  const { user } = useAuth();
  const { refreshData, isReady } = useData();
  const { enqueueSnackbar } = useSnackbar();

  const form = useForm({
    defaultValues: {
      age: user?.age || '',
      weight: user?.weight || '',
      height: user?.height || '',
      gender: user?.gender || '',
    } as PhysicalAttributesFormData,
    validators: {
      onChange: ({ value }: { value: PhysicalAttributesFormData }) => {
        const errors: Record<string, string> = {};

        if (value.age && (value.age < 1 || value.age > 120)) {
          errors.age = 'Age must be between 1 and 120 years';
        }

        if (value.weight && (value.weight < 1 || value.weight > 1000)) {
          errors.weight = 'Weight must be between 1 and 1000 pounds';
        }

        if (value.height && (value.height < 1 || value.height > 120)) {
          errors.height = 'Height must be between 1 and 120 inches';
        }

        return Object.keys(errors).length > 0 ? errors : undefined;
      },
    },
    onSubmit: async ({ value }: { value: PhysicalAttributesFormData }) => {
      console.log('Form submitted with values:', value);
      if (!user) {
        console.log('No user found');
        enqueueSnackbar('User not found', { variant: 'error' });
        return;
      }

      try {
        const updateData = {
          name: user.name, // Keep the current name unchanged
          age: value.age ? Number(value.age) : undefined,
          weight: value.weight ? Number(value.weight) : undefined,
          height: value.height ? Number(value.height) : undefined,
          gender: value.gender || undefined,
        };

        console.log('Updating profile with data:', updateData);
        await updateUserProfile(updateData);
        console.log('Profile updated successfully');
        
        // Refresh user data to get the updated information
        const updatedUser = await getCurrentUser();
        console.log('Updated user data:', updatedUser);
        
        // Update the form data with the fresh user data
        form.setFieldValue('age', updatedUser.age || '');
        form.setFieldValue('weight', updatedUser.weight || '');
        form.setFieldValue('height', updatedUser.height || '');
        form.setFieldValue('gender', updatedUser.gender || '');
        enqueueSnackbar('Profile updated successfully', { variant: 'success' });
      } catch (error) {
        console.error('Failed to update profile:', error);
        enqueueSnackbar('Failed to update profile', { variant: 'error' });
      }
    },
  });

  if (!isReady) {
    return <LoadingSpinner message="Loading profile..." fullHeight={false} />;
  }

  return (
    <Box>
      <GameText variant="h5" gutterBottom>
        Physical Attributes
      </GameText>
      <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom3}>
        Manage your physical attributes for personalized workout recommendations. All data is
        encrypted at rest for your privacy and GDPR compliance.
      </GameText>

      <GameCard>
        <CardContent>
          <form
            onSubmit={e => {
              e.preventDefault();
              e.stopPropagation();
              console.log('Form onSubmit triggered');
              form.handleSubmit();
            }}
          >
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormField
                  type="number"
                  label="Age"
                  name="age"
                  form={form}
                  inputProps={{ min: 1, max: 120 }}
                  helperText="Your age in years"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormField
                  type="number"
                  label="Weight (lbs)"
                  name="weight"
                  form={form}
                  inputProps={{ min: 1, max: 1000 }}
                  helperText="Your weight in pounds"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormField
                  type="number"
                  label="Height (cm)"
                  name="height"
                  form={form}
                  inputProps={{ min: 50, max: 300 }}
                  helperText="Your height in centimeters"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormField
                  type="select"
                  label="Gender"
                  name="gender"
                  form={form}
                  options={[
                    { value: '', label: 'Select gender' },
                    { value: 'male', label: 'Male' },
                    { value: 'female', label: 'Female' },
                  ]}
                  helperText="Your gender"
                />
              </Grid>
            </Grid>

            <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                type="button"
                variant="contained"
                disabled={form.state.isSubmitting}
                startIcon={form.state.isSubmitting ? <CircularProgress size={20} /> : null}
                onClick={async () => {
                  console.log('Save button clicked');
                  console.log('Form state:', form.state);
                  console.log('Form values:', form.state.values);
                  
                  // Call the onSubmit function directly
                  const formData = form.state.values as PhysicalAttributesFormData;
                  console.log('Calling onSubmit with:', formData);
                  
                  if (!user) {
                    console.log('No user found');
                    enqueueSnackbar('User not found', { variant: 'error' });
                    return;
                  }

                  try {
                    const updateData = {
                      name: user.name,
                      age: formData.age ? Number(formData.age) : undefined,
                      weight: formData.weight ? Number(formData.weight) : undefined,
                      height: formData.height ? Number(formData.height) : undefined,
                      gender: formData.gender || undefined,
                    };

                    console.log('Updating profile with data:', updateData);
                    await updateUserProfile(updateData);
                    console.log('Profile updated successfully');
                    
                    // Refresh all data in DataContext (including Wilks score)
                    console.log('Refreshing DataContext...');
                    await refreshData();
                    console.log('DataContext refreshed');
                    
                    enqueueSnackbar('Profile updated successfully', { variant: 'success' });
                  } catch (error) {
                    console.error('Failed to update profile:', error);
                    enqueueSnackbar('Failed to update profile', { variant: 'error' });
                  }
                }}
              >
                {form.state.isSubmitting ? 'Saving...' : 'Save Changes'}
              </Button>
            </Box>
          </form>
        </CardContent>
      </GameCard>
    </Box>
  );
}
