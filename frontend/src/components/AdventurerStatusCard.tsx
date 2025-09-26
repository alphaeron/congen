import React from 'react';
import {
  CardContent,
  CardHeader,
  Box,
  Stack,
  Grid,
} from '@mui/material';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';
import { PerformanceRadarChart } from './PerformanceRadarChart';
import { CustomSvgIcon } from './CustomSvgIcon';
import { GameProgressBar, GameCircularProgressBar } from './GameProgressBar';
import { GameCard, GameSkillChip, GameText, GameTextSecondary } from './GameTheme';

// Import custom SVG icons
import AdventurerProfileIcon from '../resources/adventurer-profile-icon.svg';
import SkillsIcon from '../resources/skills-icon.svg';
import PowerIcon from '../resources/power-icon.svg';
import RecoveryIcon from '../resources/recovery-icon.svg';
import SpeedIcon from '../resources/speed-icon.svg';
import HeartIcon from '../resources/heart-icon.svg';
import PotionBottleIcon from '../resources/potion-bottle-icon.svg';
import Gear8Icon from '../resources/gear-8-icon.svg';
import RunningShoeIcon from '../resources/running-shoe-icon.svg';
import EarSoundIcon from '../resources/ear-sound-icon.svg';

interface AdventurerStatusCardProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  weeklyTests?: UserTestResult[] | null;
  wilksScore?: number | null;
  userName?: string;
}

export const AdventurerStatusCard: React.FC<AdventurerStatusCardProps> = ({
  scores,
  metrics,
  weeklyTests,
  wilksScore,
  userName = 'Raven Thornfield',
}) => {

  const getHpColor = (hp: number): string => {
    if (hp >= 80) return '#4CAF50';
    if (hp >= 60) return '#FF9800';
    if (hp >= 40) return '#FF5722';
    return '#F44336';
  };

  const getMpColor = (mp: number): string => {
    if (mp >= 80) return '#2196F3';
    if (mp >= 60) return '#9C27B0';
    if (mp >= 40) return '#E91E63';
    return '#673AB7';
  };

  const getFatigueColor = (fatigue: number): string => {
    if (fatigue <= 30) return '#4CAF50';
    if (fatigue <= 60) return '#FF9800';
    if (fatigue <= 80) return '#FF5722';
    return '#F44336';
  };

  return (
    <GameCard sx={{ overflow: 'visible' }}>
      <CardHeader
        title={
          <Box sx={{ textAlign: 'center' }}>
            <GameText variant="h3" sx={{ fontWeight: 'bold', color: '#ffffff', textShadow: '0 0 3px #00bcd4' }}>
              STATUS
            </GameText>
          </Box>
        }
        sx={{ pb: 1, textAlign: 'center' }}
      />
      
      <CardContent>
        {/* Profile Information - Centered under STATUS */}
        <Grid container spacing={1} mb={3} overflow="visible">
          <Grid size={12}>
            <Stack direction="column" alignItems="center">
              <Stack direction="row" alignItems="center" spacing={2}>
                <CustomSvgIcon src={AdventurerProfileIcon} alt="Profile" sx={{ fontSize: 80, color: 'white' }} />
                <Stack direction="column" alignItems="flex-start">
                  <GameText variant="h2" sx={{ fontSize: '3rem', fontWeight: 'bold', color: '#00bcd4', textShadow: '0 0 10px #00bcd4', lineHeight: 1 }}>
                    {scores.level}
                  </GameText>
                  <GameText variant="h6" sx={{ color: '#e0e0e0', textShadow: '0 0 5px #00bcd4', mt: -0.5 }}>
                    LEVEL
                  </GameText>
                </Stack>
              </Stack>
              <GameText variant="h6" sx={{ color: '#ffffff', textShadow: '0 0 3px #00bcd4', mt: 1 }}>
                {userName}
              </GameText>
            </Stack>
          </Grid>
        </Grid>

        {/* Status Bars - Centered under profile */}
        <Grid container spacing={6} justifyContent="center" alignItems="center" mb={3}>
          <Grid size="auto">
            <GameProgressBar
              icon={<CustomSvgIcon src={HeartIcon} alt="HP" sx={{ fontSize: 40, color: getHpColor(scores.hp) }} />}
              label="HP"
              current={Math.max(0, scores.hp - scores.hp_loss)}
              max={scores.hp}
              color={getHpColor(scores.hp)}
              tooltip="HP (Health Points) - Long-term physical durability and structural integrity. Represents tissue wear and tear that accumulates over time. Recovers slowly (days to weeks)."
            />
          </Grid>
          <Grid size="auto">
            <GameProgressBar
              icon={<CustomSvgIcon src={PotionBottleIcon} alt="MP" sx={{ fontSize: 40, color: getMpColor(scores.mp) }} />}
              label="MP"
              current={Math.max(0, scores.mp - scores.mp_loss)}
              max={scores.mp}
              color={getMpColor(scores.mp)}
              tooltip="MP (Magic Points) - Short-term neurological and cognitive energy. Represents CNS readiness and mental sharpness. Recovers overnight to 2-3 days."
            />
          </Grid>
          <Grid size="auto">
            <GameCircularProgressBar
              label="Fatigue"
              current={Math.max(0, scores.fatigue - scores.fatigue_loss)}
              max={scores.fatigue}
              color={getFatigueColor(scores.fatigue)}
              tooltip="Fatigue - Immediate performance inhibition and session-to-session exhaustion. Represents how drained you feel right now. Recovers in hours to 1-2 days."
            />
          </Grid>
        </Grid>

        {/* Bottom Section: Weekly Quests (left) and Radar Chart (right) */}
        <Grid container spacing={1} mb={3} overflow="visible" justifyContent="center">
          <Grid size="auto">
            {/* Weekly Quests Progress and Skills - Left side */}
            <Grid container spacing={3} p={2} justifyContent="center" alignItems="center">
              <Grid size={12}>
                <GameProgressBar
                  label="Weekly Quests"
                  current={weeklyTests?.filter(test => test.status === 'COMPLETED').length || 0}
                  max={weeklyTests?.length || 0}
                  color="#FFD700"
                  tooltip="Weekly quest completion progress"
                />
              </Grid>
              
              {/* Skills Section */}
              <Grid size={12}>
                <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                  <GameText variant="h6" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1, color: '#ffffff', textShadow: '0 0 3px #00bcd4' }}>
                    Skills
                  </GameText>
                </Stack>
                {scores.skills.length > 0 ? (
                  <Grid container spacing={1}>
                    {scores.skills.map((skill, index) => (
                      <Grid key={index} size="auto">
                        <GameSkillChip
                          label={skill}
                          size="small"
                          icon={<CustomSvgIcon src={SkillsIcon} alt="Skill" sx={{ fontSize: 16, color: '#00bcd4' }} />}
                        />
                      </Grid>
                    ))}
                  </Grid>
                ) : (
                  <GameTextSecondary variant="body2" sx={{ fontStyle: 'italic' }}>
                    No skills unlocked yet. Complete quests to unlock skills!
                  </GameTextSecondary>
                )}
              </Grid>
            </Grid>
          </Grid>
          
          <Grid size="auto">
            {/* Performance Radar Chart - Right side */}
            <PerformanceRadarChart
              scores={scores}
              metrics={metrics}
              weeklyTests={weeklyTests}
              wilksScore={wilksScore}
              title=""
              height={250}
            />
          </Grid>
        </Grid>

      </CardContent>
    </GameCard>
  );
};
