import {
  Box,
  Drawer,
  Checkbox,
  Chip,
  Divider,
  FormControl,
  Select,
  MenuItem,
  Autocomplete,
} from '@mui/material';
import React, { useState, useMemo } from 'react';

import { GameText, GameTextField, GAME_CLASSES } from './GameTheme';
import type { Exercise, Equipment, Muscle } from '../api/types';

interface ExerciseSearchDrawerProps {
  exercises: Exercise[];
  equipment: Equipment[];
  muscles: Muscle[];
  onFiltersChange: (filters: ExerciseFilters) => void;
  appliedFilters: ExerciseFilters;
}

interface ExerciseFilters {
  selectedExercises: string[];
  movementTypes: string[];
  equipment: string[];
  targetMuscles: string[];
  isUnilateral: boolean | null;
  isAccessory: boolean | null;
  isUpper: boolean | null;
}

/**
 * Exercise Search Drawer component providing faceted search functionality.
 * 
 * Follows e-commerce search patterns with:
 * - Progressive disclosure of filters
 * - Real-time search with autocomplete
 * - Applied filters display
 * - Clear all functionality
 * 
 * @param open Whether the drawer is open
 * @param onClose Function to close the drawer
 * @param exercises Available exercises for filtering
 * @param equipment Available equipment options
 * @param muscles Available muscle groups
 * @param onFiltersChange Callback when filters change
 * @param onSearchChange Callback when search term changes
 * @param searchTerm Current search term
 * @param appliedFilters Currently applied filters
 * @return Exercise Search Drawer component
 */
export const ExerciseSearchDrawer: React.FC<ExerciseSearchDrawerProps> = ({
  exercises,
  equipment,
  muscles,
  onFiltersChange,
  appliedFilters,
}) => {
  const [localFilters, setLocalFilters] = useState<ExerciseFilters>(appliedFilters);

  // Sync local filters with parent filters when they change
  React.useEffect(() => {
    setLocalFilters(appliedFilters);
  }, [appliedFilters]);

  // Extract unique values for filter options
  const exerciseNames = useMemo(() => {
    return exercises.map(e => e.name).sort();
  }, [exercises]);

  const movementTypes = useMemo(() => {
    return Array.from(new Set(exercises.map(e => e.movement_type))).sort();
  }, [exercises]);

  const equipmentOptions = useMemo(() => {
    return equipment.map(e => e.name).sort();
  }, [equipment]);

  const muscleOptions = useMemo(() => {
    return muscles.map(m => m.name).sort();
  }, [muscles]);

  const handleFilterChange = (filterType: keyof ExerciseFilters, value: any) => {
    const newFilters = { ...localFilters, [filterType]: value };
    setLocalFilters(newFilters);
    onFiltersChange(newFilters);
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
          position: 'relative',
          height: '100%',
          zIndex: 1,
          backgroundColor: 'background.paper',
          borderRight: 1,
          borderColor: 'divider',
          overflow: 'hidden', // Prevent drawer content overflow
          borderRadius: 0, // Remove rounded corners
          top: 0, // Position at the top of the container
        },
      }}
    >
      <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        {/* Header */}
        <Box className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.borderBottom1} ${GAME_CLASSES.borderColorDivider} ${GAME_CLASSES.flexShrink0}`}>
          <GameText variant="h6" textVariant="glow">
            Exercise Library
          </GameText>
        </Box>

        {/* Filter Sections */}
        <Box className={`${GAME_CLASSES.flex1} ${GAME_CLASSES.overflowAuto}`} sx={{ p: 2 }}>
          {/* Exercise Selection */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Exercise
            </GameText>
            <Autocomplete
              multiple
              id="exercise-autocomplete"
              options={exerciseNames}
              value={localFilters.selectedExercises}
              onChange={(_, newValue) => handleFilterChange('selectedExercises', newValue)}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip
                    variant="outlined"
                    label={option}
                    size="small"
                    color="primary"
                    {...getTagProps({ index })}
                  />
                ))
              }
              renderInput={(params) => (
                <GameTextField
                  {...params}
                  placeholder="Select exercises..."
                  size="small"
                />
              )}
              renderOption={(props, option, { selected }) => (
                <li {...props}>
                  <Checkbox
                    style={{ marginRight: 8 }}
                    checked={selected}
                  />
                  {option}
                </li>
              )}
            />
          </Box>

          {/* Movement Types */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Movement Type
            </GameText>
            <Autocomplete
              multiple
              id="movement-types-autocomplete"
              options={movementTypes}
              value={localFilters.movementTypes}
              onChange={(_, newValue) => handleFilterChange('movementTypes', newValue)}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip
                    variant="outlined"
                    label={option}
                    size="small"
                    color="primary"
                    {...getTagProps({ index })}
                  />
                ))
              }
              renderInput={(params) => (
                <GameTextField
                  {...params}
                  placeholder="Select movement types..."
                  size="small"
                />
              )}
              renderOption={(props, option, { selected }) => (
                <li {...props}>
                  <Checkbox
                    style={{ marginRight: 8 }}
                    checked={selected}
                  />
                  {option}
                </li>
              )}
            />
          </Box>

          {/* Equipment */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Equipment
            </GameText>
            <Autocomplete
              multiple
              id="equipment-autocomplete"
              options={equipmentOptions}
              value={localFilters.equipment}
              onChange={(_, newValue) => handleFilterChange('equipment', newValue)}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip
                    variant="outlined"
                    label={option}
                    size="small"
                    color="primary"
                    {...getTagProps({ index })}
                  />
                ))
              }
              renderInput={(params) => (
                <GameTextField
                  {...params}
                  placeholder="Select equipment..."
                  size="small"
                />
              )}
              renderOption={(props, option, { selected }) => (
                <li {...props}>
                  <Checkbox
                    style={{ marginRight: 8 }}
                    checked={selected}
                  />
                  {option}
                </li>
              )}
            />
          </Box>

          {/* Target Muscles */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Target Muscles
            </GameText>
            <Autocomplete
              multiple
              id="target-muscles-autocomplete"
              options={muscleOptions}
              value={localFilters.targetMuscles}
              onChange={(_, newValue) => handleFilterChange('targetMuscles', newValue)}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip
                    variant="outlined"
                    label={option}
                    size="small"
                    color="primary"
                    {...getTagProps({ index })}
                  />
                ))
              }
              renderInput={(params) => (
                <GameTextField
                  {...params}
                  placeholder="Select target muscles..."
                  size="small"
                />
              )}
              renderOption={(props, option, { selected }) => (
                <li {...props}>
                  <Checkbox
                    style={{ marginRight: 8 }}
                    checked={selected}
                  />
                  {option}
                </li>
              )}
            />
          </Box>

          <Divider sx={{ my: 2 }} />

          {/* Movement Pattern Filter */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Movement Pattern
            </GameText>
            <FormControl fullWidth size="small">
              <Select
                value={localFilters.isUnilateral === null ? 'both' : localFilters.isUnilateral ? 'unilateral' : 'bilateral'}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value === 'both') {
                    handleFilterChange('isUnilateral', null);
                  } else if (value === 'unilateral') {
                    handleFilterChange('isUnilateral', true);
                  } else if (value === 'bilateral') {
                    handleFilterChange('isUnilateral', false);
                  }
                }}
                sx={{
                  '& .MuiSelect-select': {
                    color: 'text.primary',
                    '&:focus': {
                      backgroundColor: 'transparent',
                    },
                  },
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'primary.main',
                  },
                  '&:hover .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'primary.main',
                  },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'primary.main',
                  },
                }}
              >
                <MenuItem value="both">Both</MenuItem>
                <MenuItem value="unilateral">Unilateral</MenuItem>
                <MenuItem value="bilateral">Bilateral</MenuItem>
              </Select>
            </FormControl>
          </Box>

          {/* Exercise Type Filter */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Exercise Type
            </GameText>
            <FormControl fullWidth size="small">
              <Select
                value={localFilters.isAccessory === null ? 'both' : localFilters.isAccessory ? 'accessory' : 'primary'}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value === 'both') {
                    handleFilterChange('isAccessory', null);
                  } else if (value === 'accessory') {
                    handleFilterChange('isAccessory', true);
                  } else if (value === 'primary') {
                    handleFilterChange('isAccessory', false);
                  }
                }}
                sx={{
                  '& .MuiSelect-select': {
                    color: 'text.primary',
                    '&:focus': {
                      backgroundColor: 'transparent',
                    },
                  },
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'secondary.main',
                  },
                  '&:hover .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'secondary.main',
                  },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'secondary.main',
                  },
                }}
              >
                <MenuItem value="both">Both</MenuItem>
                <MenuItem value="primary">Primary</MenuItem>
                <MenuItem value="accessory">Accessory</MenuItem>
              </Select>
            </FormControl>
          </Box>

          {/* Body Part Filter */}
          <Box sx={{ mb: 3 }}>
            <GameText variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Body Part
            </GameText>
            <FormControl fullWidth size="small">
              <Select
                value={localFilters.isUpper === null ? 'both' : localFilters.isUpper ? 'upper' : 'lower'}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value === 'both') {
                    handleFilterChange('isUpper', null);
                  } else if (value === 'upper') {
                    handleFilterChange('isUpper', true);
                  } else if (value === 'lower') {
                    handleFilterChange('isUpper', false);
                  }
                }}
                sx={{
                  '& .MuiSelect-select': {
                    color: 'text.primary',
                    '&:focus': {
                      backgroundColor: 'transparent',
                    },
                  },
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'success.main',
                  },
                  '&:hover .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'success.main',
                  },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'success.main',
                  },
                }}
              >
                <MenuItem value="both">Both</MenuItem>
                <MenuItem value="upper">Upper Body</MenuItem>
                <MenuItem value="lower">Lower Body</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </Box>
      </Box>
    </Drawer>
  );
};
