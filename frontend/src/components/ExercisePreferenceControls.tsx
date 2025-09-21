import {
  Favorite as FavoriteIcon,
  Block as BlockIcon,
  Remove as RemoveIcon,
} from '@mui/icons-material';
import {
  Chip,
  IconButton,
  Tooltip,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect } from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import type { UserExercisePreference } from '../api/types';
import {
  upsertUserExercisePreference,
  removeUserExercisePreference,
} from '../api/userExercisePreference';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

/**
 * Exercise preference state type.
 */
type ExercisePreferenceState = 'prefer' | 'ignore' | 'neutral';

/**
 * Props for the ExercisePreferenceControls component.
 */
interface ExercisePreferenceControlsProps {
  exerciseName: string;
  variant?: 'segmented' | 'chip' | 'icon';
  size?: 'small' | 'medium';
  onPreferenceChange?: (preference: UserExercisePreference | null) => void;
} // end interface ExercisePreferenceControlsProps

/**
 * Component for managing exercise preferences (prefer/ignore).
 *
 * @param props The props for the component.
 * @return The exercise preference controls component.
 */
export function ExercisePreferenceControls(
  props: ExercisePreferenceControlsProps
): React.ReactElement<ExercisePreferenceControlsProps> {
  const { exerciseName, variant = 'segmented', size = 'small', onPreferenceChange } = props;
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const { userExercisePreferences, loadUserExercisePreferences, refreshData } = useData();

  const [preference, setPreference] = useState<UserExercisePreference | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Load current preference from DataContext
  useEffect(() => {
    const loadPreference = async () => {
      if (!user?.keycloak_id) return;

      setLoading(true);
      try {
        // Load user exercise preferences if not already loaded
        if (userExercisePreferences.length === 0) {
          await loadUserExercisePreferences();
        }

        // Find current preference from DataContext data
        const currentPreference = userExercisePreferences.find(
          p => p.exercise_name === exerciseName
        );
        setPreference(currentPreference || null);
      } catch {
        enqueueSnackbar('Failed to load exercise preference:', { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };

    loadPreference();
  }, [
    user?.keycloak_id,
    exerciseName,
    userExercisePreferences,
    loadUserExercisePreferences,
    enqueueSnackbar,
  ]);

  const getCurrentPreferenceState = (): ExercisePreferenceState => {
    if (!preference) return 'neutral';
    return preference.should_avoid ? 'ignore' : 'prefer';
  };

  const handlePreferenceChange = async (newState: ExercisePreferenceState) => {
    if (!user?.keycloak_id) return;

    setSaving(true);
    try {
      if (newState === 'neutral') {
        // Remove preference
        if (preference) {
          await removeUserExercisePreference(user.keycloak_id, exerciseName);
          setPreference(null);
          onPreferenceChange?.(null);
          enqueueSnackbar('Exercise preference removed', { variant: 'success' });
        }
      } else {
        // Set preference (prefer or ignore)
        const shouldAvoid = newState === 'ignore';
        const newPreference = await upsertUserExercisePreference(
          user.keycloak_id,
          exerciseName,
          shouldAvoid
        );
        setPreference(newPreference);
        onPreferenceChange?.(newPreference);

        enqueueSnackbar(`Exercise ${shouldAvoid ? 'ignored' : 'preferred'} successfully`, {
          variant: 'success',
        });
      }

      // Refresh DataContext to ensure all components have the latest data
      await refreshData();
    } catch {
      enqueueSnackbar('Failed to update exercise preference', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size={20} />;
  }

  const currentState = getCurrentPreferenceState();

  if (variant === 'segmented') {
    return (
      <ToggleButtonGroup
        value={currentState}
        exclusive
        onChange={(_, newState) => {
          if (newState !== null) {
            handlePreferenceChange(newState);
          }
        }}
        size={size}
        disabled={saving}
        sx={{
          '& .MuiToggleButton-root': {
            border: '1px solid',
            borderColor: 'divider',
            '&.Mui-selected': {
              backgroundColor: 'primary.main',
              color: 'primary.contrastText',
              '&:hover': {
                backgroundColor: 'primary.dark',
              },
            },
            '&:hover': {
              backgroundColor: 'action.hover',
            },
          },
        }}
      >
        <ToggleButton value="prefer" aria-label="prefer exercise">
          <FavoriteIcon sx={{ mr: 0.5 }} />
          <Typography variant="caption">Prefer</Typography>
        </ToggleButton>
        <ToggleButton value="neutral" aria-label="neutral preference">
          <RemoveIcon sx={{ mr: 0.5 }} />
          <Typography variant="caption">Neutral</Typography>
        </ToggleButton>
        <ToggleButton value="ignore" aria-label="ignore exercise">
          <BlockIcon sx={{ mr: 0.5 }} />
          <Typography variant="caption">Ignore</Typography>
        </ToggleButton>
      </ToggleButtonGroup>
    );
  }

  if (variant === 'chip') {
    const getChipColor = () => {
      switch (currentState) {
        case 'prefer':
          return 'success';
        case 'ignore':
          return 'error';
        default:
          return 'default';
      }
    };

    const getChipLabel = () => {
      switch (currentState) {
        case 'prefer':
          return 'Preferred';
        case 'ignore':
          return 'Ignored';
        default:
          return 'Neutral';
      }
    };

    const getChipIcon = () => {
      switch (currentState) {
        case 'prefer':
          return <FavoriteIcon />;
        case 'ignore':
          return <BlockIcon />;
        default:
          return <RemoveIcon />;
      }
    };

    return (
      <Chip
        icon={getChipIcon()}
        label={getChipLabel()}
        color={getChipColor()}
        size={size}
        onClick={() => {
          // Cycle through states: neutral -> prefer -> ignore -> neutral
          const nextState =
            currentState === 'neutral'
              ? 'prefer'
              : currentState === 'prefer'
                ? 'ignore'
                : 'neutral';
          handlePreferenceChange(nextState);
        }}
        disabled={saving}
        sx={{ cursor: 'pointer' }}
      />
    );
  }

  if (variant === 'icon') {
    const getIconColor = () => {
      switch (currentState) {
        case 'prefer':
          return 'success';
        case 'ignore':
          return 'error';
        default:
          return 'default';
      }
    };

    const getIcon = () => {
      switch (currentState) {
        case 'prefer':
          return <FavoriteIcon />;
        case 'ignore':
          return <BlockIcon />;
        default:
          return <RemoveIcon />;
      }
    };

    const getTooltipText = () => {
      switch (currentState) {
        case 'prefer':
          return 'Preferred - Click to change';
        case 'ignore':
          return 'Ignored - Click to change';
        default:
          return 'Neutral - Click to set preference';
      }
    };

    return (
      <Tooltip title={getTooltipText()}>
        <IconButton
          size={size}
          onClick={() => {
            // Cycle through states: neutral -> prefer -> ignore -> neutral
            const nextState =
              currentState === 'neutral'
                ? 'prefer'
                : currentState === 'prefer'
                  ? 'ignore'
                  : 'neutral';
            handlePreferenceChange(nextState);
          }}
          disabled={saving}
          color={getIconColor()}
        >
          {getIcon()}
        </IconButton>
      </Tooltip>
    );
  }

  // Default segmented variant
  return (
    <ToggleButtonGroup
      value={currentState}
      exclusive
      onChange={(_, newState) => {
        if (newState !== null) {
          handlePreferenceChange(newState);
        }
      }}
      size={size}
      disabled={saving}
    >
      <ToggleButton value="prefer" aria-label="prefer exercise">
        <FavoriteIcon sx={{ mr: 0.5 }} />
        <Typography variant="caption">Prefer</Typography>
      </ToggleButton>
      <ToggleButton value="neutral" aria-label="neutral preference">
        <RemoveIcon sx={{ mr: 0.5 }} />
        <Typography variant="caption">No Preference</Typography>
      </ToggleButton>
      <ToggleButton value="ignore" aria-label="ignore exercise">
        <BlockIcon sx={{ mr: 0.5 }} />
        <Typography variant="caption">Ignore</Typography>
      </ToggleButton>
    </ToggleButtonGroup>
  );
} // end component ExercisePreferenceControls
