import React from 'react';
import {
  CardContent,
  CardHeader,
  Box,
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
import PotionIcon from '../resources/potion-icon.svg';
import GearIcon from '../resources/gear-icon.svg';

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
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              STATUS
            </GameText>
          </Box>
        }
        sx={{ pb: 1, textAlign: 'center' }}
      />
      
      <CardContent>
        {/* Main Content: Status Info (left) and Radar Chart (right) */}
        <Box sx={{ display: 'flex', gap: 3, mb: 3, overflow: 'visible' }}>
          {/* Status Information - Left side */}
          <Box sx={{ flex: '0 0 60%', display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Level and Name */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <CustomSvgIcon src={AdventurerProfileIcon} alt="Adventurer Profile" sx={{ fontSize: 80, color: 'white' }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                <GameText variant="h2" sx={{ fontSize: '3rem', fontWeight: 'bold', color: '#00bcd4', textShadow: '0 0 10px #00bcd4', lineHeight: 1 }}>
                  {scores.level}
                </GameText>
                <GameText variant="h6" sx={{ color: '#e0e0e0', textShadow: '0 0 5px #00bcd4', mt: -0.5 }}>
                  LEVEL
                </GameText>
              </Box>
            </Box>

            {/* Status Bars - Single Row */}
            <Box sx={{ display: 'flex', flexDirection: 'row', gap: 2, alignItems: 'center' }}>
              <GameProgressBar
                icon={<CustomSvgIcon src={RecoveryIcon} alt="HP" sx={{ fontSize: 20, color: '#00bcd4' }} />}
                label="HP"
                current={Math.max(0, scores.hp - scores.hp_loss)}
                max={scores.hp}
                color="#00bcd4"
                tooltip="HP (Health Points) - Long-term physical durability and structural integrity. Represents tissue wear and tear that accumulates over time. Recovers slowly (days to weeks)."
              />

              <GameProgressBar
                icon={<CustomSvgIcon src={PotionIcon} alt="MP" sx={{ fontSize: 20, color: '#00bcd4' }} />}
                label="MP"
                current={Math.max(0, scores.mp - scores.mp_loss)}
                max={scores.mp}
                color="#00bcd4"
                tooltip="MP (Magic Points) - Short-term neurological and cognitive energy. Represents CNS readiness and mental sharpness. Recovers overnight to 2-3 days."
              />

              <GameCircularProgressBar
                icon={<CustomSvgIcon src={GearIcon} alt="Fatigue" sx={{ fontSize: 20, color: '#00bcd4' }} />}
                label="Fatigue"
                current={Math.max(0, scores.fatigue - scores.fatigue_loss)}
                max={scores.fatigue}
                color="#00bcd4"
                tooltip="Fatigue - Immediate performance inhibition and session-to-session exhaustion. Represents how drained you feel right now. Recovers in hours to 1-2 days."
              />
            </Box>
          </Box>

          {/* Performance Radar Chart - Right side */}
          <Box sx={{ flex: '0 0 40%', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'visible' }}>
            <PerformanceRadarChart
              scores={scores}
              metrics={metrics}
              weeklyTests={weeklyTests}
              wilksScore={wilksScore}
              title=""
              height={250}
            />
          </Box>
        </Box>

        {/* Skills Section */}
        <Box>
          {/* Skills Header */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              p: 1,
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              borderRadius: 1,
              mb: 1,
            }}
          >
            <GameText variant="subtitle2" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}>
              <CustomSvgIcon src={SkillsIcon} alt="Skills" sx={{ fontSize: 16, color: 'white' }} />
              Skills
            </GameText>
          </Box>
          
          {/* Skills Content */}
          <Box sx={{ p: 1, backgroundColor: 'rgba(255, 255, 255, 0.05)', borderRadius: 1 }}>
            {scores.skills.length > 0 ? (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {scores.skills.map((skill, index) => (
                  <GameSkillChip
                    key={index}
                    label={skill}
                    size="small"
                    icon={<CustomSvgIcon src={SkillsIcon} alt="Skill" sx={{ fontSize: 16, color: '#00bcd4' }} />}
                  />
                ))}
              </Box>
            ) : (
              <GameTextSecondary variant="body2" sx={{ fontStyle: 'italic' }}>
                No skills unlocked yet. Complete quests to unlock skills!
              </GameTextSecondary>
            )}
          </Box>
        </Box>
      </CardContent>
    </GameCard>
  );
};
