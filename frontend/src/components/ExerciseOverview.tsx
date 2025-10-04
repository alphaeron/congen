import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import { useVirtualizer } from '@tanstack/react-virtual';
import * as React from 'react';
import { useSearchParams } from 'react-router';

import { EmptyState } from './EmptyState';
import { ExerciseCard } from './ExerciseCard';
import { ExerciseSearchDrawer } from './ExerciseSearchDrawer';
import { LoadingSpinner } from './LoadingSpinner';
import { GameText, GAME_CLASSES } from './GameTheme';
import type { Equipment, Exercise, Muscle } from '../api/types';
import { useData } from '../contexts/DataContext';

import '../styles/Form.css';

/**
 * Shows an overview of all available exercises.
 *
 * @return The exercise overview component.
 */
export function ExerciseOverview(): React.ReactElement {
  const [exercisesToDisplay, setExercisesToDisplay] = React.useState<Exercise[]>([]);
  const [searchParams, setSearchParams] = useSearchParams();

  // Helper functions for URL query parameters
  const parseArrayParam = (param: string | null): string[] => {
    return param ? param.split(',').filter(Boolean) : [];
  };

  const parseBooleanParam = (param: string | null): boolean | null => {
    if (param === 'true') return true;
    if (param === 'false') return false;
    return null;
  };

  const serializeFiltersToURL = (filters: typeof appliedFilters) => {
    const params = new URLSearchParams();
    
    if (filters.selectedExercises.length > 0) {
      params.set('exercises', filters.selectedExercises.join(','));
    }
    if (filters.movementTypes.length > 0) {
      params.set('movementTypes', filters.movementTypes.join(','));
    }
    if (filters.equipment.length > 0) {
      params.set('equipment', filters.equipment.join(','));
    }
    if (filters.targetMuscles.length > 0) {
      params.set('targetMuscles', filters.targetMuscles.join(','));
    }
    if (filters.isUnilateral !== null) {
      params.set('isUnilateral', filters.isUnilateral.toString());
    }
    if (filters.isAccessory !== null) {
      params.set('isAccessory', filters.isAccessory.toString());
    }
    if (filters.isUpper !== null) {
      params.set('isUpper', filters.isUpper.toString());
    }

    setSearchParams(params);
  };

  // Single source of truth: derive filters from URL parameters
  const appliedFilters = React.useMemo(() => ({
    selectedExercises: parseArrayParam(searchParams.get('exercises')),
    movementTypes: parseArrayParam(searchParams.get('movementTypes')),
    equipment: parseArrayParam(searchParams.get('equipment')),
    targetMuscles: parseArrayParam(searchParams.get('targetMuscles')),
    isUnilateral: parseBooleanParam(searchParams.get('isUnilateral')),
    isAccessory: parseBooleanParam(searchParams.get('isAccessory')),
    isUpper: parseBooleanParam(searchParams.get('isUpper')),
  }), [searchParams]);

  // Check if there are any active filters
  const hasActiveFilters = React.useMemo(() => {
    return (
      appliedFilters.selectedExercises.length > 0 ||
      appliedFilters.movementTypes.length > 0 ||
      appliedFilters.equipment.length > 0 ||
      appliedFilters.targetMuscles.length > 0 ||
      appliedFilters.isUnilateral !== null ||
      appliedFilters.isAccessory !== null ||
      appliedFilters.isUpper !== null
    );
  }, [appliedFilters]);

  // Single function to update filters by updating URL
  const updateFilters = React.useCallback((newFilters: typeof appliedFilters) => {
    serializeFiltersToURL(newFilters);
  }, []);

  // Virtualization setup
  const parentRef = React.useRef<HTMLDivElement>(null);
  const itemsPerRow = 4; // Number of items per row in the grid
  const rowHeight = 300; // Approximate height of each exercise card row with spacing

  const {
    loadAllExercises,
    loadAllEquipment,
    loadAllMuscles,
    loadAllExerciseMuscleData,
    loadAllExerciseEquipmentData,
    exerciseMuscleData,
    exerciseEquipmentData,
    allExercises,
    allEquipment,
    allMuscles,
    error: dataError,
    isLoadingSpecific,
    getErrorForDataType,
  } = useData();

  // Load data on mount
  React.useEffect(() => {
    const loadData = async () => {
      // Load all data including mappings
      await Promise.all([
        loadAllExercises(),
        loadAllEquipment(),
        loadAllMuscles(),
        loadAllExerciseMuscleData(),
        loadAllExerciseEquipmentData(),
      ]);
    };

    loadData();
  }, [loadAllExercises, loadAllEquipment, loadAllMuscles, loadAllExerciseMuscleData, loadAllExerciseEquipmentData]);

  const isExercisesError = !!dataError;
  const isEquipmentError = !!dataError;
  const isMusclesError = !!dataError;
  const isExerciseMuscleError = !!getErrorForDataType('exerciseMuscleData');
  const isExerciseEquipmentError = !!getErrorForDataType('exerciseEquipmentData');


  // Convert DataContext data to the expected format
  const exerciseEquipmentMap = React.useMemo(() => {
    const mapping = new Map<string, Set<string>>();
    // Convert DataContext exerciseEquipmentData to the expected format
    for (const [exerciseName, equipmentList] of Array.from(exerciseEquipmentData.entries())) {
      mapping.set(exerciseName, new Set(equipmentList.map((eq: any) => eq.equipment_name)));
    }
    return mapping;
  }, [exerciseEquipmentData]);

  const exerciseMuscleMap = React.useMemo(() => {
    const mapping = new Map<string, Set<string>>();
    // Convert DataContext exerciseMuscleData to the expected format
    for (const [exerciseName, muscleList] of Array.from(exerciseMuscleData.entries())) {
      mapping.set(exerciseName, new Set(muscleList));
    }
    return mapping;
  }, [exerciseMuscleData]);

  // Enhanced filtering with search drawer
  React.useEffect(() => {
    if (!allExercises || allExercises.length === 0) return;
    
    // Don't run equipment/muscle filters if mapping data isn't loaded yet
    const needsEquipmentData = appliedFilters.equipment.length > 0;
    const needsMuscleData = appliedFilters.targetMuscles.length > 0;
    
    if (needsEquipmentData && exerciseEquipmentMap.size === 0) {
      return;
    }
    if (needsMuscleData && exerciseMuscleMap.size === 0) {
      return;
    }

    let filtered = allExercises;

    // Selected exercises filter
    if (appliedFilters.selectedExercises.length > 0) {
      filtered = filtered.filter(e => 
        appliedFilters.selectedExercises.includes(e.name)
      );
    }

    // Movement type filters
    if (appliedFilters.movementTypes.length > 0) {
      filtered = filtered.filter(e => appliedFilters.movementTypes.includes(e.movement_type));
    }

    // Equipment filters
    if (appliedFilters.equipment.length > 0) {
      filtered = filtered.filter(e => {
        const equipmentSet = exerciseEquipmentMap?.get(e.name);
        return appliedFilters.equipment.some(eq => equipmentSet?.has(eq));
      });
    }

    // Target muscle filters
    if (appliedFilters.targetMuscles.length > 0) {
      filtered = filtered.filter(e => {
        const muscleSet = exerciseMuscleMap?.get(e.name);
        return appliedFilters.targetMuscles.some(muscle => muscleSet?.has(muscle));
      });
    }

    // Advanced filters
    if (appliedFilters.isUnilateral !== null) {
      filtered = filtered.filter(e => e.is_unilateral === appliedFilters.isUnilateral);
    }

    if (appliedFilters.isAccessory !== null) {
      filtered = filtered.filter(e => e.is_accessory === appliedFilters.isAccessory);
    }

    if (appliedFilters.isUpper !== null) {
      filtered = filtered.filter(e => e.is_upper === appliedFilters.isUpper);
    }

    setExercisesToDisplay(filtered);
  }, [
    allExercises,
    appliedFilters,
    exerciseEquipmentMap,
    exerciseMuscleMap,
  ]);

  const isLoading =
    isLoadingSpecific('exercises') ||
    isLoadingSpecific('equipment') ||
    isLoadingSpecific('muscles') ||
    isLoadingSpecific('exerciseMuscleData') ||
    isLoadingSpecific('exerciseEquipmentData');

  const hasError =
    isExercisesError ||
    isEquipmentError ||
    isMusclesError ||
    isExerciseMuscleError ||
    isExerciseEquipmentError;

  // Calculate virtual rows for grid layout
  const totalRows = Math.ceil(exercisesToDisplay.length / itemsPerRow);

  // Create virtualizer for the grid
  const virtualizer = useVirtualizer({
    count: totalRows,
    getScrollElement: () => parentRef.current,
    estimateSize: () => rowHeight,
    overscan: 2, // Render 2 extra rows above and below viewport
  });

  if (isLoading) {
    return <LoadingSpinner message="Loading exercises..." />;
  }

  if (hasError) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ borderRadius: 2 }}>
          <AlertTitle>Error</AlertTitle>
          Failed to load exercises. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
    <Box
      sx={{
        display: 'flex',
        height: 'calc(100vh - 64px)', // Account for app bar height
        position: 'fixed',
        top: 64, // Start below the app bar
        left: 0,
        right: 0,
        bottom: 0,
        overflow: 'hidden', // Prevent overflow
        zIndex: 1,
      }}
    >
      {/* Search Drawer */}
      <ExerciseSearchDrawer
        exercises={allExercises || []}
        equipment={allEquipment || []}
        muscles={allMuscles || []}
        onFiltersChange={updateFilters}
        appliedFilters={appliedFilters}
      />

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          height: '100%',
          overflow: 'auto', // Allow content to scroll if needed
          maxWidth: 'calc(100% - 240px)', // Prevent overflow
        }}
      >
        <Container maxWidth="xl" sx={{ py: 3 }}>
          <Stack spacing={4}>
        {/* Applied Filters */}
        {hasActiveFilters && (
          <Box sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
              <GameText variant="h6" textVariant="secondary" sx={{ color: '#1e293b' }}>
                Applied Filters
              </GameText>
              <Button
                size="small"
                onClick={() => updateFilters({
                  selectedExercises: [],
                  movementTypes: [],
                  equipment: [],
                  targetMuscles: [],
                  isUnilateral: null,
                  isAccessory: null,
                  isUpper: null,
                })}
                className="modern-button"
                sx={{ 
                  minWidth: 'auto', 
                  p: 0.5,
                  background: 'linear-gradient(135deg, #00bcd4, #00acc1)',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #00acc1, #0097a7)',
                  },
                }}
              >
                Clear All
              </Button>
            </Box>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {appliedFilters.selectedExercises.map(exercise => (
                <Chip
                  key={exercise}
                  label={exercise}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    selectedExercises: appliedFilters.selectedExercises.filter(e => e !== exercise)
                  })}
                  color="primary"
                  variant="outlined"
                />
              ))}
              {appliedFilters.movementTypes.map(type => (
                <Chip
                  key={type}
                  label={type}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    movementTypes: appliedFilters.movementTypes.filter(t => t !== type)
                  })}
                  color="primary"
                  variant="outlined"
                />
              ))}
              {appliedFilters.equipment.map(eq => (
                <Chip
                  key={eq}
                  label={eq}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    equipment: appliedFilters.equipment.filter(e => e !== eq)
                  })}
                  color="primary"
                  variant="outlined"
                />
              ))}
              {appliedFilters.targetMuscles.map(muscle => (
                <Chip
                  key={muscle}
                  label={muscle}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    targetMuscles: appliedFilters.targetMuscles.filter(m => m !== muscle)
                  })}
                  color="primary"
                  variant="outlined"
                />
              ))}
              {appliedFilters.isUnilateral !== null && (
                <Chip
                  label={appliedFilters.isUnilateral ? 'Unilateral' : 'Bilateral'}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    isUnilateral: null
                  })}
                  color="primary"
                  variant="outlined"
                />
              )}
              {appliedFilters.isAccessory !== null && (
                <Chip
                  label={appliedFilters.isAccessory ? 'Accessory' : 'Primary'}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    isAccessory: null
                  })}
                  color="secondary"
                  variant="outlined"
                />
              )}
              {appliedFilters.isUpper !== null && (
                <Chip
                  label={appliedFilters.isUpper ? 'Upper Body' : 'Lower Body'}
                  size="small"
                  onDelete={() => updateFilters({
                    ...appliedFilters,
                    isUpper: null
                  })}
                  color="success"
                  variant="outlined"
                />
              )}
            </Box>
          </Box>
        )}

        {/* Results Section */}
        <Box>
          <GameText variant="h5" className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.marginBottom3}`} sx={{ color: '#1e293b' }}>
            Results ({exercisesToDisplay.length} exercises)
          </GameText>

          {exercisesToDisplay.length === 0 ? (
            <EmptyState
              title="No exercises found matching your filters."
              message="Try adjusting your filter criteria to see more results."
              variant="paper"
            />
          ) : (
            <Box
              ref={parentRef}
              sx={{
                minHeight: '400px', // Minimum height for virtualization
                width: '100%',
              }}
            >
              <div
                style={{
                  height: `${virtualizer.getTotalSize()}px`,
                  width: '100%',
                  position: 'relative',
                }}
              >
                {virtualizer.getVirtualItems().map(virtualRow => {
                  const startIndex = virtualRow.index * itemsPerRow;
                  const endIndex = Math.min(startIndex + itemsPerRow, exercisesToDisplay.length);
                  const rowExercises = exercisesToDisplay.slice(startIndex, endIndex);

                  return (
                    <div
                      key={virtualRow.key}
                      style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: `${virtualRow.size}px`,
                        transform: `translateY(${virtualRow.start}px)`,
                      }}
                    >
                      <Grid container spacing={2} sx={{ height: '100%' }}>
                        {rowExercises.map(exercise => (
                          <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={exercise.name} sx={{ pb: 2 }}>
                            <ExerciseCard exercise={exercise} />
                          </Grid>
                        ))}
                        {/* Fill remaining grid slots with empty space */}
                        {Array.from({ length: itemsPerRow - rowExercises.length }).map(
                          (_, index) => (
                            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={`empty-${index}`}>
                              <div style={{ height: '100%' }} />
                            </Grid>
                          )
                        )}
                      </Grid>
                    </div>
                  );
                })}
              </div>
            </Box>
          )}
        </Box>
          </Stack>
        </Container>
      </Box>
    </Box>
  );
}
