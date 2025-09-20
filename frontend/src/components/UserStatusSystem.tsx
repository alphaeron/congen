import { Box, Grid, Typography, Card, CardContent } from '@mui/material';
import React, { useMemo } from 'react';

import type { StatusLevel, StatusMetric } from './StatusCard';
import { StatusCard } from './StatusCard';
import type { User, UserDataExport, UserOneRepMax } from '../api/types';

export interface UserStatus {
  overall: StatusLevel;
  strength: StatusMetric;
  cardio: StatusMetric;
  recovery: StatusMetric;
  nutrition: StatusMetric;
  lastUpdated: Date;
}

interface UserStatusSystemProps {
  user: User;
  userData: UserDataExport | null;
  oneRepMaxes: UserOneRepMax[];
}

/**
 * UserStatusSystem component for displaying comprehensive user status information.
 *
 * This component calculates and displays various health and fitness metrics
 * with status indicators and color coding.
 *
 * @param userData The user's training data
 * @param oneRepMaxes The user's one rep max records
 * @return UserStatusSystem component
 */
export const UserStatusSystem: React.FC<UserStatusSystemProps> = ({ userData, oneRepMaxes }) => {
  // Calculate user status metrics
  const userStatus = useMemo((): UserStatus => {
    const now = new Date();

    // Calculate strength status based on recent 1RM improvements
    const recentOneRepMaxes = oneRepMaxes.slice(-10); // Last 10 records
    const strengthImprovement =
      recentOneRepMaxes.length > 1
        ? (recentOneRepMaxes[recentOneRepMaxes.length - 1]?.one_rep_max || 0) -
          (recentOneRepMaxes[0]?.one_rep_max || 0)
        : 0;

    const strengthStatus: StatusLevel =
      strengthImprovement > 0 ? 'excellent' : strengthImprovement === 0 ? 'good' : 'fair';

    // Calculate training frequency
    const totalWorkouts =
      userData?.training_programs?.reduce((total, program) => total + program.workouts.length, 0) ||
      0;

    const trainingFrequency =
      totalWorkouts > 20
        ? 'excellent'
        : totalWorkouts > 10
          ? 'good'
          : totalWorkouts > 5
            ? 'fair'
            : 'needs_attention';

    // Calculate overall status
    const overallStatus: StatusLevel =
      strengthStatus === 'excellent' && trainingFrequency === 'excellent'
        ? 'excellent'
        : strengthStatus === 'good' && trainingFrequency === 'good'
          ? 'good'
          : strengthStatus === 'fair' || trainingFrequency === 'fair'
            ? 'fair'
            : 'needs_attention';

    return {
      overall: overallStatus,
      strength: {
        value: recentOneRepMaxes.length,
        unit: '1RMs',
        status: strengthStatus,
        trend: strengthImprovement > 0 ? 'up' : strengthImprovement < 0 ? 'down' : 'stable',
        lastMeasurement: now,
      },
      cardio: {
        value: 0, // Placeholder for future cardio metrics
        unit: 'VO2 Max',
        status: 'good',
        trend: 'stable',
        lastMeasurement: now,
      },
      recovery: {
        value: 85, // Placeholder for recovery metrics
        unit: '%',
        status: 'good',
        trend: 'stable',
        lastMeasurement: now,
      },
      nutrition: {
        value: 0, // Placeholder for nutrition metrics
        unit: 'Score',
        status: 'good',
        trend: 'stable',
        lastMeasurement: now,
      },
      lastUpdated: now,
    };
  }, [userData, oneRepMaxes]);

  return (
    <Box>
      {/* Overall Status Card */}
      <Card sx={{ mb: 3, border: '2px solid', borderColor: 'primary.main' }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
            <Typography variant="h5" fontWeight="bold" color="primary">
              Overall Health & Fitness Status
            </Typography>
            <StatusCard
              title=""
              status={userStatus.overall}
              value={userStatus.overall.charAt(0).toUpperCase() + userStatus.overall.slice(1)}
            />
          </Box>
          <Typography variant="body2" color="text.secondary">
            Based on your training progress, strength gains, and consistency
          </Typography>
        </CardContent>
      </Card>

      {/* Status Metrics Grid */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatusCard
            title="Strength Training"
            status={userStatus.strength.status}
            value={userStatus.strength.value}
            unit={userStatus.strength.unit}
            trend={userStatus.strength.trend}
            lastUpdated={userStatus.strength.lastMeasurement}
            description="Based on recent 1RM improvements"
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatusCard
            title="Cardio Fitness"
            status={userStatus.cardio.status}
            value={userStatus.cardio.value}
            unit={userStatus.cardio.unit}
            trend={userStatus.cardio.trend}
            lastUpdated={userStatus.cardio.lastMeasurement}
            description="VO2 Max and cardiovascular health"
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatusCard
            title="Recovery"
            status={userStatus.recovery.status}
            value={userStatus.recovery.value}
            unit={userStatus.recovery.unit}
            trend={userStatus.recovery.trend}
            lastUpdated={userStatus.recovery.lastMeasurement}
            description="Sleep quality and recovery metrics"
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatusCard
            title="Nutrition"
            status={userStatus.nutrition.status}
            value={userStatus.nutrition.value}
            unit={userStatus.nutrition.unit}
            trend={userStatus.nutrition.trend}
            lastUpdated={userStatus.nutrition.lastMeasurement}
            description="Nutritional balance and hydration"
          />
        </Grid>
      </Grid>
    </Box>
  );
};
