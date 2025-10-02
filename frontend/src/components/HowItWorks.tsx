import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Divider from '@mui/material/Divider';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { 
  FitnessCenter, 
  Assessment, 
  History, 
  DataObject, 
  Pool, 
  SportsGymnastics,
  Speed,
  Accessible,
  Tune,
  Balance,
  RotateRight,
  CheckCircle
} from '@mui/icons-material';

import { CycleDiagramReact as CycleDiagram } from './CycleDiagramReact';
import { GameText, GameCard } from './GameTheme';
import { AdventurerStatusCard } from './AdventurerStatusCard';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';

// Personalization cycle data
const personalizationNodes = [
  { id: 'generation', label: 'Generate Workouts', details: ['Conjugate Workout Generation'] },
  { id: 'personalization', label: 'Workout Personalization', details: ['One Rep Max Integration'] },
  { id: 'tracking', label: 'Workout Tracking', details: ['Record Performance'] },
  { id: 'performance', label: 'Performance Tracking', details: ['Calculate Scores', 'Leveling Progression'] }
];

// Sample data for AdventurerStatusCard demonstration
// Calculated using backend logic: average score = 73.3, tanh scaling = level 15
const samplePerformanceScores: UserPerformanceScores = {
  id: 1,
  keycloak_id: 'sample-user',
  explosiveness_score: 75.5,  // 60+ = "Quick Burst" skill
  aerobic_capacity_score: 68.2,  // 60+ = "Endurance Runner" skill  
  recovery_score: 82.1,  // 80+ = "Rapid Recovery" skill
  reaction_time_score: 71.8,  // 60+ = "Quick Response" skill
  mobility_score: 65.4,  // 60+ = "Agile Movement" skill
  strength_score: 78.9,  // 60+ = "Strong Lifter" skill
  wilks_score: 285.6,
  level: 15,  // Calculated: tanh((73.3-50)/15) + 1) * 50 = 15
  level_change_reason: 'weekly_test_completed',
  hp: 85.2,
  hp_loss: 12.3,
  mp: 78.7,
  mp_loss: 8.1,
  fatigue: 45.6,
  fatigue_loss: 15.2,
  skills: ['Quick Burst', 'Endurance Runner', 'Rapid Recovery', 'Quick Response', 'Agile Movement', 'Strong Lifter'],
  created_at: new Date()
};

const samplePerformanceMetrics: UserPerformanceMetrics = {
  keycloak_id: 'sample-user',
  vo2_max: 42.5,
  hrv: 45.2,
  sleep_score: 78.3,
  strain: 12.1,
  recovery: 85.6,
  created_at: new Date(),
  updated_at: new Date()
};

const sampleWeeklyTests: UserTestResult[] = [
  {
    id: 1,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'vertical_jump',
    status: 'COMPLETED',
    result_value: 52.3,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: 2,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'hr_recovery',
    status: 'COMPLETED',
    result_value: 28,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: 3,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'reflex',
    status: 'COMPLETED',
    result_value: 285,
    created_at: new Date(),
    updated_at: new Date()
  }
];


// Algorithm Infographic Component
const AlgorithmInfographic = () => {
  return (
    <Box
      sx={{
        width: '100%',
        maxWidth: 800,
        height: 400,
        position: 'relative',
        background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)',
        borderRadius: 4,
        overflow: 'hidden',
        boxShadow: '0 20px 40px rgba(0, 0, 0, 0.3)',
        border: '1px solid rgba(59, 130, 246, 0.2)',
      }}
    >
      {/* Background Pattern */}
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: `
            radial-gradient(circle at 20% 20%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
            radial-gradient(circle at 80% 80%, rgba(124, 58, 237, 0.1) 0%, transparent 50%),
            radial-gradient(circle at 40% 60%, rgba(5, 150, 105, 0.1) 0%, transparent 50%)
          `,
        }}
      />

      {/* Input Stage - Top */}
      <Box
        sx={{
          position: 'absolute',
          top: 30,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          gap: 2,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#3b82f6' }}>
          <FitnessCenter sx={{ fontSize: 18 }} />
          <Box sx={{ fontSize: '13px', fontWeight: 600, color: '#f1f5f9' }}>Equipment</Box>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#3b82f6' }}>
          <Assessment sx={{ fontSize: 18 }} />
          <Box sx={{ fontSize: '13px', fontWeight: 600, color: '#f1f5f9' }}>Strength</Box>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#3b82f6' }}>
          <History sx={{ fontSize: 18 }} />
          <Box sx={{ fontSize: '13px', fontWeight: 600, color: '#f1f5f9' }}>History</Box>
        </Box>
      </Box>

      {/* Arrow Down */}
      <Box
        sx={{
          position: 'absolute',
          top: 80,
          left: '50%',
          transform: 'translateX(-50%)',
          width: 0,
          height: 0,
          borderLeft: '6px solid transparent',
          borderRight: '6px solid transparent',
          borderTop: '10px solid #3b82f6',
        }}
      />

      {/* Data Preparation & Exercise Pool - Same Row */}
      <Box
        sx={{
          position: 'absolute',
          top: 110,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          gap: 4,
          alignItems: 'center',
        }}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            background: 'rgba(124, 58, 237, 0.2)',
            border: '1px solid rgba(124, 58, 237, 0.4)',
            borderRadius: 3,
            px: 2,
            py: 1.5,
          }}
        >
          <DataObject sx={{ fontSize: 20, color: '#7c3aed' }} />
          <Box sx={{ fontSize: '16px', fontWeight: 700, color: '#f1f5f9' }}>Data Preparation</Box>
        </Box>
        
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            background: 'rgba(124, 58, 237, 0.2)',
            border: '1px solid rgba(124, 58, 237, 0.4)',
            borderRadius: 3,
            px: 2,
            py: 1.5,
          }}
        >
          <Pool sx={{ fontSize: 20, color: '#7c3aed' }} />
          <Box sx={{ fontSize: '16px', fontWeight: 700, color: '#f1f5f9' }}>Exercise Pool</Box>
        </Box>
      </Box>

      {/* Arrow Down */}
      <Box
        sx={{
          position: 'absolute',
          top: 180,
          left: '50%',
          transform: 'translateX(-50%)',
          width: 0,
          height: 0,
          borderLeft: '6px solid transparent',
          borderRight: '6px solid transparent',
          borderTop: '10px solid #7c3aed',
        }}
      />

      {/* Workout Stage Generation - Based on FourDayWorkoutStageGenerationService */}
      <Box
        sx={{
          position: 'absolute',
          top: 210,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          flexDirection: 'column',
          gap: 1.5,
          alignItems: 'center',
        }}
      >
        {/* Primary & Secondary Exercise Selection */}
        <Box
          sx={{
            display: 'flex',
            gap: 3,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#059669' }}>
            <Speed sx={{ fontSize: 14 }} />
            <Box sx={{ fontSize: '11px', fontWeight: 600, color: '#f1f5f9' }}>Primary Exercise</Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#059669' }}>
            <Accessible sx={{ fontSize: 14 }} />
            <Box sx={{ fontSize: '11px', fontWeight: 600, color: '#f1f5f9' }}>Secondary Exercise</Box>
          </Box>
        </Box>

        {/* Set Scheme Generation - Elaborated */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#059669', mb: 1 }}>
          <Tune sx={{ fontSize: 14 }} />
          <Box sx={{ fontSize: '11px', fontWeight: 600, color: '#f1f5f9' }}>Set Scheme Generation</Box>
        </Box>
        
        {/* Set Scheme Components */}
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            gap: 0.5,
            alignItems: 'center',
          }}
        >
          {/* Prilepin Guidelines */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#7c3aed' }}>
            <Assessment sx={{ fontSize: 10 }} />
            <Box sx={{ fontSize: '9px', fontWeight: 600, color: '#f1f5f9' }}>Prilepin Guidelines</Box>
          </Box>
          
          {/* Weight Selection */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#7c3aed' }}>
            <FitnessCenter sx={{ fontSize: 10 }} />
            <Box sx={{ fontSize: '9px', fontWeight: 600, color: '#f1f5f9' }}>Weight Selection</Box>
          </Box>
          
          {/* Movement Balance Integration */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#7c3aed' }}>
            <Balance sx={{ fontSize: 10 }} />
            <Box sx={{ fontSize: '9px', fontWeight: 600, color: '#f1f5f9' }}>Movement Balance</Box>
          </Box>
          
          {/* Exercise Rotation Logic */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#7c3aed' }}>
            <RotateRight sx={{ fontSize: 10 }} />
            <Box sx={{ fontSize: '9px', fontWeight: 600, color: '#f1f5f9' }}>Exercise Rotation</Box>
          </Box>
        </Box>

        {/* Workout Stages */}
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            mt: 0.5,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#dc2626' }}>
            <Balance sx={{ fontSize: 12 }} />
            <Box sx={{ fontSize: '10px', fontWeight: 600, color: '#f1f5f9' }}>Warmup</Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#dc2626' }}>
            <RotateRight sx={{ fontSize: 12 }} />
            <Box sx={{ fontSize: '10px', fontWeight: 600, color: '#f1f5f9' }}>Primary</Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#dc2626' }}>
            <CheckCircle sx={{ fontSize: 12 }} />
            <Box sx={{ fontSize: '10px', fontWeight: 600, color: '#f1f5f9' }}>Secondary</Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#dc2626' }}>
            <FitnessCenter sx={{ fontSize: 12 }} />
            <Box sx={{ fontSize: '10px', fontWeight: 600, color: '#f1f5f9' }}>Accessory</Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#dc2626' }}>
            <Assessment sx={{ fontSize: 12 }} />
            <Box sx={{ fontSize: '10px', fontWeight: 600, color: '#f1f5f9' }}>Conditioning</Box>
          </Box>
        </Box>
      </Box>

      {/* Connecting Lines - Positioned to avoid text overlap */}
      <Box
        sx={{
          position: 'absolute',
          top: 80,
          left: '50%',
          transform: 'translateX(-50%)',
          width: '2px',
          height: '200px',
          background: 'linear-gradient(to bottom, #3b82f6, #7c3aed, #059669)',
          zIndex: 0,
        }}
      />
    </Box>
  );
};

// Individual section components for better UX
const AlgorithmSection = () => (
  <Box
    id="algorithm"
    sx={{
      py: { xs: 8, sm: 12 },
      position: 'relative',
      width: '100%',
      px: { xs: 2, sm: 4, md: 6 },
    }}
  >
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', lg: 'row' },
        alignItems: 'center',
        gap: { xs: 4, lg: 8 },
      }}
    >
          <Box
            sx={{
              flex: { xs: 1, lg: 0.8 },
              textAlign: { xs: 'center', lg: 'left' },
            }}
          >
        <GameText
          variant="h2"
          textVariant="glow"
          sx={{
            fontWeight: 700,
            mb: 3,
            fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
          }}
        >
          Advanced Algorithm
        </GameText>
        <GameText
          variant="h5"
          textVariant="secondary"
          sx={{
            fontWeight: 400,
            mb: 4,
            lineHeight: 1.6,
            opacity: 0.9,
          }}
        >
          Our sophisticated conjugate method algorithm automatically selects exercises, 
          calculates optimal weights, and structures your workouts based on proven 
          scientific principles.
        </GameText>
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
            flexWrap: 'wrap',
          }}
        >
          {[
            'Conjugate Method',
            'Exercise Rotation', 
            'Load Balancing',
            'Weak Point Training'
          ].map((feature, index) => (
            <GameCard
              key={index}
              sx={{
                p: 2,
                background: theme => 
                  `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.1)}, ${alpha(theme.palette.primary.main, 0.05)})`,
                border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
                flex: '1 1 auto',
                minWidth: '200px',
              }}
            >
              <GameText
                variant="body1"
                textVariant="glow"
                sx={{ fontWeight: 600 }}
              >
                {feature}
              </GameText>
            </GameCard>
          ))}
        </Box>
      </Box>
      
        <Box
          sx={{
            flex: { xs: 1, lg: 1.2 },
            minHeight: 400,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 3,
          }}
        >
          <AlgorithmInfographic />
        </Box>
    </Box>
  </Box>
);

const GamificationSection = () => (
  <Box
    id="gamification"
    sx={{
      py: { xs: 8, sm: 12 },
      position: 'relative',
      background: theme => alpha(theme.palette.background.paper, 0.5),
      width: '100%',
      px: { xs: 2, sm: 4, md: 6 },
    }}
  >
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', lg: 'row-reverse' },
        alignItems: 'center',
        gap: { xs: 4, lg: 8 },
      }}
    >
      <Box
        sx={{
          flex: 1,
          textAlign: { xs: 'center', lg: 'right' },
        }}
      >
        <GameText
          variant="h2"
          textVariant="glow"
          sx={{
            fontWeight: 700,
            mb: 3,
            fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
          }}
        >
          RPG-Style Gamification
        </GameText>
        <GameText
          variant="h5"
          textVariant="secondary"
          sx={{
            fontWeight: 400,
            mb: 4,
            lineHeight: 1.6,
            opacity: 0.9,
          }}
        >
          Transform your fitness journey into an epic RPG adventure. Level up your 
          character, unlock skills, and master the HP/MP/Fatigue 
          system for maximum motivation.
        </GameText>
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
            flexWrap: 'wrap',
            justifyContent: { xs: 'center', lg: 'flex-end' },
          }}
        >
          {[
            'Level Progression',
            'Performance Tracking', 
            'Skill System',
            'HP/MP Mechanics'
          ].map((feature, index) => (
            <GameCard
              key={index}
              sx={{
                p: 2,
                background: theme => 
                  `linear-gradient(135deg, ${alpha('#8b5cf6', 0.1)}, ${alpha('#8b5cf6', 0.05)})`,
                border: theme => `1px solid ${alpha('#8b5cf6', 0.2)}`,
                flex: '1 1 auto',
                minWidth: '200px',
              }}
            >
              <GameText
                variant="body1"
                textVariant="glow"
                sx={{ fontWeight: 600 }}
              >
                {feature}
              </GameText>
            </GameCard>
          ))}
        </Box>
      </Box>
      
          <Box
            sx={{
              flex: { xs: 1, lg: 1.2 },
              minHeight: 600,
              maxHeight: 800,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Box sx={{ maxWidth: 800, width: '100%' }}>
              <AdventurerStatusCard
                scores={samplePerformanceScores}
                metrics={samplePerformanceMetrics}
                weeklyTests={sampleWeeklyTests}
                userName="Alex Chen"
              />
            </Box>
          </Box>
    </Box>
  </Box>
);

const PersonalizationSection = () => (
  <Box
    id="personalization"
    sx={{
      py: { xs: 8, sm: 12 },
      position: 'relative',
      width: '100%',
      px: { xs: 2, sm: 4, md: 6 },
    }}
  >
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', lg: 'row' },
        alignItems: { xs: 'center', lg: 'flex-start' },
        gap: { xs: 2, lg: 3 },
        minHeight: { xs: 'auto', lg: '60vh' },
        width: '100%',
      }}
    >
          <Box
            sx={{
              flex: { xs: 1, lg: 0.5 },
              textAlign: { xs: 'center', lg: 'left' },
            }}
          >
        <GameText
          variant="h2"
          textVariant="glow"
          sx={{
            fontWeight: 700,
            mb: 3,
            fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
          }}
        >
          Smart Personalization
        </GameText>
        <GameText
          variant="h5"
          textVariant="secondary"
          sx={{
            fontWeight: 400,
            mb: 4,
            lineHeight: 1.6,
            opacity: 0.9,
          }}
        >
          Every aspect of your program is algorithmically tailored to your equipment, 
          goals, experience level, and physical attributes. Real-time adjustments 
          ensure continuous optimization for maximum results.
        </GameText>
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
            flexWrap: 'wrap',
          }}
        >
          {[
            'Equipment Matching',
            'Time Optimization', 
            'Real-time Updates',
            'Continuous Learning'
          ].map((feature, index) => (
            <GameCard
              key={index}
              sx={{
                p: 2,
                background: theme => 
                  `linear-gradient(135deg, ${alpha('#22c55e', 0.1)}, ${alpha('#22c55e', 0.05)})`,
                border: theme => `1px solid ${alpha('#22c55e', 0.2)}`,
                flex: '1 1 auto',
                minWidth: '200px',
              }}
            >
              <GameText
                variant="body1"
                textVariant="glow"
                sx={{ fontWeight: 600 }}
              >
                {feature}
              </GameText>
            </GameCard>
          ))}
        </Box>
      </Box>
      
          <Box
            sx={{
              flex: { xs: 1, lg: 1.5 },
              minWidth: { xs: 200, sm: 300, md: 400 },
              width: '100%',
              height: '100%',
              aspectRatio: '1 / 1',
              maxHeight: '80vh',
              overflow: 'visible',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <CycleDiagram 
              nodes={personalizationNodes} 
            />
          </Box>
    </Box>
  </Box>
);

export function HowItWorks() {
  return (
    <React.Fragment>
      <AlgorithmSection />
      <Divider />
      <GamificationSection />
      <Divider />
      <PersonalizationSection />
      <Divider />
    </React.Fragment>
  );
}
