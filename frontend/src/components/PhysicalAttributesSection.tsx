import { Button, CardContent, CircularProgress, Grid } from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { motion } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React from 'react';

import { FormField } from './FormField';
import { GameText, GameCard, GAME_CLASSES } from './GameTheme';
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
      if (!user) {
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

        await updateUserProfile(updateData);

        // Refresh user data to get the updated information
        const updatedUser = await getCurrentUser();

        // Update the form data with the fresh user data
        form.setFieldValue('age', updatedUser.age || '');
        form.setFieldValue('weight', updatedUser.weight || '');
        form.setFieldValue('height', updatedUser.height || '');
        form.setFieldValue('gender', updatedUser.gender || '');
        enqueueSnackbar('Profile updated successfully', { variant: 'success' });
      } catch {
        enqueueSnackbar('Failed to update profile', { variant: 'error' });
      }
    },
  });

  if (!isReady) {
    return <LoadingSpinner message="Loading profile..." fullHeight={false} />;
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.8, ease: 'easeOut' }}
    >
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
      >
        <GameText variant="h5" gutterBottom>
          Physical Attributes
        </GameText>
      </motion.div>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.4 }}
      >
        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom3}>
          Manage your physical attributes for personalized workout recommendations. All data is
          encrypted at rest for your privacy and GDPR compliance.
        </GameText>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.6 }}
        whileHover={{ y: -2 }}
        style={{ transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
      >
        <GameCard
          sx={{
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              boxShadow: '0 8px 25px rgba(0, 188, 212, 0.15)',
            },
          }}
        >
          <CardContent>
            <form
              onSubmit={e => {
                e.preventDefault();
                e.stopPropagation();
                form.handleSubmit();
              }}
            >
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <motion.div
                    initial={{ opacity: 0, x: -30 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.6, ease: 'easeOut', delay: 0.8 }}
                  >
                    <FormField
                      type="number"
                      label="Age"
                      name="age"
                      form={form}
                      inputProps={{ min: 1, max: 120 }}
                      helperText="Your age in years"
                    />
                  </motion.div>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <motion.div
                    initial={{ opacity: 0, x: 30 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.6, ease: 'easeOut', delay: 0.9 }}
                  >
                    <FormField
                      type="number"
                      label="Weight (lbs)"
                      name="weight"
                      form={form}
                      inputProps={{ min: 1, max: 1000 }}
                      helperText="Your weight in pounds"
                    />
                  </motion.div>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <motion.div
                    initial={{ opacity: 0, x: -30 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.6, ease: 'easeOut', delay: 1.0 }}
                  >
                    <FormField
                      type="number"
                      label="Height (cm)"
                      name="height"
                      form={form}
                      inputProps={{ min: 50, max: 300 }}
                      helperText="Your height in centimeters"
                    />
                  </motion.div>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <motion.div
                    initial={{ opacity: 0, x: 30 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.6, ease: 'easeOut', delay: 1.1 }}
                  >
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
                  </motion.div>
                </Grid>
              </Grid>

              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: 1.2 }}
                style={{
                  marginTop: '24px',
                  display: 'flex',
                  justifyContent: 'flex-end',
                }}
              >
                <motion.div
                  whileHover={{ y: -2 }}
                  whileTap={{ scale: 0.98 }}
                  style={{ transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
                >
                  <Button
                    type="button"
                    variant="contained"
                    disabled={form.state.isSubmitting}
                    startIcon={form.state.isSubmitting ? <CircularProgress size={20} /> : null}
                    sx={{
                      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      '&:hover:not(:disabled)': {
                        boxShadow: '0 8px 25px rgba(0, 188, 212, 0.4)',
                      },
                    }}
                    onClick={async () => {
                      // Call the onSubmit function directly
                      const formData = form.state.values as PhysicalAttributesFormData;

                      if (!user) {
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

                        await updateUserProfile(updateData);

                        // Refresh all data in DataContext (including Wilks score)
                        await refreshData();

                        enqueueSnackbar('Profile updated successfully', { variant: 'success' });
                      } catch {
                        enqueueSnackbar('Failed to update profile', { variant: 'error' });
                      }
                    }}
                  >
                    {form.state.isSubmitting ? 'Saving...' : 'Save Changes'}
                  </Button>
                </motion.div>
              </motion.div>
            </form>
          </CardContent>
        </GameCard>
      </motion.div>
    </motion.div>
  );
}
