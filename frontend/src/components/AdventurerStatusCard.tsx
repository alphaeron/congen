import React, { useState } from 'react';
import {
  CardContent,
  CardHeader,
  Box,
  Collapse,
  IconButton,
  Avatar,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Psychology as PsychologyIcon,
  BatteryAlert as BatteryAlertIcon,
  EmojiEvents as EmojiEventsIcon,
  Favorite as FavoriteIcon,
} from '@mui/icons-material';
import type { UserPerformanceScores, UserPerformanceMetrics } from '../api/types';
import { PerformanceRadarChart } from './PerformanceRadarChart';
import { CustomSvgIcon } from './CustomSvgIcon';
import { GameProgressBar, GameCircularProgressBar } from './GameProgressBar';
import { GameCard, GameSkillChip, GameText, GameTextSecondary } from './GameTheme';

// Import custom SVG icons
import AdventurerProfileIcon from '../resources/adventurer-profile-icon.svg';
import SkillsIcon from '../resources/skills-icon.svg';

interface AdventurerStatusCardProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  userName?: string;
}

export const AdventurerStatusCard: React.FC<AdventurerStatusCardProps> = ({
  scores,
  metrics,
  userName = 'Raven Thornfield',
}) => {
  const [expandedSkills, setExpandedSkills] = useState(false);

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
    <GameCard>
      <CardHeader
        avatar={
          <Avatar
            sx={{
              bgcolor: 'rgba(255, 255, 255, 0.2)',
              width: 56,
              height: 56,
              fontSize: '1.5rem',
            }}
          >
            <CustomSvgIcon src={AdventurerProfileIcon} alt="Adventurer Profile" sx={{ fontSize: 32, color: 'white' }} />
          </Avatar>
        }
        title={
          <Box>
            <GameText variant="h6" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}>
              <CustomSvgIcon src={AdventurerProfileIcon} alt="Adventurer Profile" sx={{ fontSize: 20, color: 'white' }} />
              Adventurer Profile
            </GameText>
          </Box>
        }
        sx={{ pb: 1 }}
      />
      
      <CardContent>
        {/* Profile Information */}
        <Box sx={{ mb: 2, p: 2, backgroundColor: 'rgba(255, 255, 255, 0.1)', borderRadius: 1 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
              <GameTextSecondary variant="body2">
                <strong>Name:</strong> {userName}
              </GameTextSecondary>
              <GameTextSecondary variant="body2">
                <strong>Level:</strong> {scores.level}
              </GameTextSecondary>
            </Box>
          </Box>
        </Box>

        {/* HP/MP/Fatigue Status Bars */}
        <Box sx={{ mb: 2 }}>
          <GameProgressBar
            icon={<FavoriteIcon sx={{ color: getHpColor(scores.hp - scores.hp_loss) }} />}
            label="HP"
            current={Math.max(0, scores.hp - scores.hp_loss)}
            max={scores.hp}
            color={getHpColor(scores.hp - scores.hp_loss)}
            tooltip="HP (Health Points) - Long-term physical durability and structural integrity. Represents tissue wear and tear that accumulates over time. Recovers slowly (days to weeks)."
          />

          <GameProgressBar
            icon={<PsychologyIcon sx={{ color: getMpColor(scores.mp - scores.mp_loss) }} />}
            label="MP"
            current={Math.max(0, scores.mp - scores.mp_loss)}
            max={scores.mp}
            color={getMpColor(scores.mp - scores.mp_loss)}
            tooltip="MP (Magic Points) - Short-term neurological and cognitive energy. Represents CNS readiness and mental sharpness. Recovers overnight to 2-3 days."
          />

          <GameCircularProgressBar
            icon={<BatteryAlertIcon sx={{ color: getFatigueColor(scores.fatigue - scores.fatigue_loss) }} />}
            label="Fatigue"
            current={Math.max(0, scores.fatigue - scores.fatigue_loss)}
            max={scores.fatigue}
            color={getFatigueColor(scores.fatigue - scores.fatigue_loss)}
            tooltip="Fatigue - Immediate performance inhibition and session-to-session exhaustion. Represents how drained you feel right now. Recovers in hours to 1-2 days."
          />
        </Box>

        {/* Performance Radar Chart */}
        <Box sx={{ mb: 2 }}>
          <PerformanceRadarChart
            scores={scores}
            metrics={metrics}
            title="Athletic Profile"
            height={250}
          />
        </Box>

        {/* Skills Section */}
        <Box>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              cursor: 'pointer',
              p: 1,
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              borderRadius: 1,
            }}
            onClick={() => setExpandedSkills(!expandedSkills)}
          >
                  <GameText variant="subtitle2" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}>
                    <CustomSvgIcon src={SkillsIcon} alt="Skills" sx={{ fontSize: 16, color: 'white' }} />
                    Skills
                  </GameText>
            <IconButton size="small" sx={{ color: 'white' }}>
              {expandedSkills ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>
          </Box>
          <Collapse in={expandedSkills}>
            <Box sx={{ p: 1, backgroundColor: 'rgba(255, 255, 255, 0.05)', borderRadius: 1 }}>
              {scores.skills.length > 0 ? (
                      scores.skills.map((skill, index) => (
                        <GameSkillChip
                          key={index}
                          label={skill}
                          size="small"
                          icon={<EmojiEventsIcon />}
                        />
                      ))
                    ) : (
                      <GameTextSecondary variant="body2" sx={{ fontStyle: 'italic' }}>
                        No skills unlocked yet. Complete tests to unlock skills!
                      </GameTextSecondary>
                    )}
            </Box>
          </Collapse>
        </Box>
      </CardContent>
    </GameCard>
  );
};
