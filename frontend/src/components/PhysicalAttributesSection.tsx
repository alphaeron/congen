import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  TextField,
  Typography,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { updateUserProfile, getCurrentUser } from '../api/user';
import { useAuth } from '../contexts/AuthContext';

/**
 * Physical attributes section component for user profile.
 *
 * This component allows users to manage their physical attributes including
 * age, weight, height, and gender. All data is encrypted at rest for GDPR compliance.
 *
 * @return Physical attributes section component
 */
export function PhysicalAttributesSection(): React.ReactElement {
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const [saving, setSaving] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    name: user?.name || '',
    age: user?.age || '',
    weight: user?.weight || '',
    height: user?.height || '',
    gender: user?.gender || '',
  });

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value === '' ? '' : (field === 'name' || field === 'gender' ? value : parseInt(value, 10) || ''),
    }));
  };

  const handleSave = async () => {
    if (!user) {
      enqueueSnackbar('User not found', { variant: 'error' });
      return;
    }

    setSaving(true);
    try {
      const updateData = {
        name: formData.name,
        age: formData.age ? Number(formData.age) : undefined,
        weight: formData.weight ? Number(formData.weight) : undefined,
        height: formData.height ? Number(formData.height) : undefined,
        gender: formData.gender || undefined,
      };

      await updateUserProfile(updateData);
      // Refresh user data to get the updated information
      const updatedUser = await getCurrentUser();
      // Update the form data with the fresh user data
      setFormData({
        name: updatedUser.name,
        age: updatedUser.age || '',
        weight: updatedUser.weight || '',
        height: updatedUser.height || '',
        gender: updatedUser.gender || '',
      });
      enqueueSnackbar('Profile updated successfully', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to update profile', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const hasChanges = () => {
    return (
      formData.name !== (user?.name || '') ||
      formData.age !== (user?.age || '') ||
      formData.weight !== (user?.weight || '') ||
      formData.height !== (user?.height || '') ||
      formData.gender !== (user?.gender || '')
    );
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Physical Attributes
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Manage your physical attributes for personalized workout recommendations. 
        All data is encrypted at rest for your privacy and GDPR compliance.
      </Typography>

      <Card>
        <CardContent>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Name"
                value={formData.name}
                onChange={handleInputChange('name')}
                required
                helperText="Your full name"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Age"
                type="number"
                value={formData.age}
                onChange={handleInputChange('age')}
                inputProps={{ min: 1, max: 120 }}
                helperText="Your age in years"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Weight"
                type="number"
                value={formData.weight}
                onChange={handleInputChange('weight')}
                inputProps={{ min: 1, max: 1000 }}
                helperText="Your weight in pounds (lbs)"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Height"
                type="number"
                value={formData.height}
                onChange={handleInputChange('height')}
                inputProps={{ min: 1, max: 120 }}
                helperText="Your height in inches (in)"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Gender"
                value={formData.gender}
                onChange={handleInputChange('gender')}
                select
                SelectProps={{
                  native: true,
                }}
                helperText="Your gender"
              >
                <option value="">Select gender</option>
                <option value="male">Male</option>
                <option value="female">Female</option>
              </TextField>
            </Grid>
          </Grid>

          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleSave}
              disabled={saving || !hasChanges()}
              startIcon={saving ? <CircularProgress size={20} /> : null}
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      <Box sx={{ mt: 2 }}>
        <Typography variant="body2" color="text.secondary">
          <strong>Privacy Note:</strong> Your physical attributes are encrypted at rest and 
          used only to provide personalized workout recommendations. You can update or 
          remove this information at any time.
        </Typography>
      </Box>
    </Box>
  );
}
