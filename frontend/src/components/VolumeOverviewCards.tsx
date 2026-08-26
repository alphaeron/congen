import { Box, useTheme } from '@mui/material';
import { motion } from 'framer-motion';
import React, { useMemo } from 'react';

import { VolumeCategoryCard } from './VolumeCategoryCard';
import type { UserDataExport, Exercise, UserOneRepMax } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { buildVolumeOverviewModel } from '../utils/volumeOverviewUtils';

interface VolumeOverviewCardsProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  workoutsPerWeek: number;
  currentWeek: number;
  preferredUnit?: 'KG' | 'LBS';
}

/**
 * Volume overview KPI cards with plan-relative bullets, W{{n}} history, and load insights.
 *
 * @param userDataExport Raw user export containing workouts and optional 1RM records
 * @param exerciseData Exercise metadata used for ME/DE/Accessory categorization
 * @param workoutsPerWeek Program days per week for week bucketing
 * @param currentWeek Active program week number
 * @param preferredUnit Weight unit for volume display
 * @return Volume overview section
 */
export const VolumeOverviewCards: React.FC<VolumeOverviewCardsProps> = ({
  userDataExport,
  exerciseData,
  workoutsPerWeek,
  currentWeek,
  preferredUnit = 'LBS',
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  const model = useMemo(
    () =>
      buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        workoutsPerWeek,
        currentWeek,
        preferredUnit
      ),
    [userDataExport, exerciseData, workoutsPerWeek, currentWeek, preferredUnit]
  );

  if (!model) {
    return null;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      style={{ display: 'flex', flexDirection: 'column', gap: 12, overflow: 'visible' }}
    >
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', overflow: 'visible' }}>
        {model.categories.map((card, index) => (
          <VolumeCategoryCard
            key={card.type}
            card={card}
            index={index}
            preferredUnit={preferredUnit}
            nivoTheme={nivoTheme}
            weekVolumes={model.weekVolumes}
            userOneRepMaxes={userDataExport?.user_one_rep_max as UserOneRepMax[] | undefined}
          />
        ))}
      </Box>
    </motion.div>
  );
};
