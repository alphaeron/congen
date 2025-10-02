import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Divider from '@mui/material/Divider';
import { alpha } from '@mui/material/styles';
import * as React from 'react';

import { CycleDiagramReact as CycleDiagram } from './CycleDiagramReact';
import { GameText, GameCard } from './GameTheme';
import { AdventurerStatusCard } from './AdventurerStatusCard';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';

// Simplified Mermaid diagram definitions
const WORKOUT_ALGORITHM_DIAGRAM = `
graph TD
    B["Equipment Analysis"]
    C["Strength Assessment"]
    D["Training History"]
    
    B --> E["Conjugate Method Selection"]
    C --> E
    D --> E
    
    E --> F["Max Effort Days"]
    E --> G["Dynamic Effort Days"]
    E --> H["Accessory Work"]
    E --> I["Recovery Days"]
    
    F --> J["Exercise Rotation"]
    G --> J
    H --> J
    I --> J
    
    J --> K["Load Progression"]
    K --> L["Weak Point Training"]
    
    style E fill:#7c3aed,stroke:#8b5cf6,color:#fff
    style J fill:#ea580c,stroke:#f97316,color:#fff
    style L fill:#059669,stroke:#10b981,color:#fff
`;


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

interface MermaidDiagramProps {
  diagram: string;
  title: string;
  description: string;
}

const MermaidDiagram: React.FC<MermaidDiagramProps> = ({ diagram, title, description }) => {
  const [isLoaded, setIsLoaded] = React.useState(false);

  React.useEffect(() => {
    const loadMermaid = async () => {
      try {
        const mermaid = (await import('mermaid')).default;
            mermaid.initialize({
              startOnLoad: false,
              theme: 'base',
              securityLevel: 'loose',
              themeVariables: {
                fontSize: '32px',
                fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                primaryColor: '#1e293b',
                primaryTextColor: '#f1f5f9',
                primaryBorderColor: '#3b82f6',
                lineColor: '#3b82f6',
                secondaryColor: '#334155',
                tertiaryColor: '#475569',
                background: '#0f172a',
                mainBkg: '#1e293b',
                secondBkg: '#334155',
                tertiaryBkg: '#475569',
                nodeBkg: '#1e293b',
                nodeBorder: '#3b82f6',
                clusterBkg: '#1e293b',
                clusterBorder: '#3b82f6',
                defaultLinkColor: '#3b82f6',
                titleColor: '#f1f5f9',
                edgeLabelBackground: '#1e293b',
                edgeLabelColor: '#f1f5f9',
              },
              flowchart: {
                nodeSpacing: 300,
                rankSpacing: 350,
                curve: 'basis',
                padding: 80,
                htmlLabels: true,
                useMaxWidth: false,
              },
            });
        setIsLoaded(true);
      } catch (error) {
        // Failed to load Mermaid
      }
    };

    loadMermaid();
  }, []);

  React.useEffect(() => {
    if (isLoaded) {
      const renderDiagram = async () => {
        try {
          const mermaid = (await import('mermaid')).default;
          const diagramId = title ? title.replace(/\s+/g, '-').toLowerCase() : 'diagram';
          const element = document.getElementById(`mermaid-${diagramId}`);
          if (element) {
            element.innerHTML = '';
            const uniqueId = `diagram-${diagramId}-${Date.now()}`;
            const { svg } = await mermaid.render(uniqueId, diagram);
            element.innerHTML = svg;
          }
        } catch (error) {
          // Failed to render diagram
          const diagramId = title ? title.replace(/\s+/g, '-').toLowerCase() : 'diagram';
          const element = document.getElementById(`mermaid-${diagramId}`);
          if (element) {
            element.innerHTML = '<div style="color: #ef4444; text-align: center; padding: 20px;">Failed to load diagram</div>';
          }
        }
      };

      // Add a small delay to ensure DOM is ready
      setTimeout(renderDiagram, 100);
    }
  }, [isLoaded, diagram, title]);

  const diagramId = title ? title.replace(/\s+/g, '-').toLowerCase() : 'diagram';

  return (
    <Box
      sx={{
        width: '100%',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        justifyContent: 'flex-start',
        minHeight: 600,
        p: 4,
        position: 'relative',
        overflow: 'visible',
      }}
    >
      {isLoaded ? (
            <Box
              sx={{
                width: '100%',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                justifyContent: 'flex-start',
                overflow: 'visible',
                '& svg': {
                  width: '100%',
                  height: 'auto',
                  maxHeight: 'none',
                  minHeight: 'auto',
                  overflow: 'visible',
                  '& text': {
                    fontSize: '32px !important',
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important',
                    fontWeight: '600 !important',
                    fill: '#f1f5f9 !important',
                  },
                  '& .node': {
                    '& text': {
                      fontSize: '32px !important',
                      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important',
                      fontWeight: '600 !important',
                      fill: '#f1f5f9 !important',
                    },
                    '& rect': {
                      fill: '#1e293b !important',
                      stroke: '#3b82f6 !important',
                      strokeWidth: '4px !important',
                      rx: '12px !important',
                      ry: '12px !important',
                      filter: 'drop-shadow(0 4px 8px rgba(59, 130, 246, 0.3)) !important',
                    },
                    '& polygon': {
                      fill: '#1e293b !important',
                      stroke: '#3b82f6 !important',
                      strokeWidth: '12px !important',
                      filter: 'drop-shadow(0 16px 32px rgba(59, 130, 246, 0.5)) !important',
                    },
                    '& ellipse': {
                      fill: '#1e293b !important',
                      stroke: '#3b82f6 !important',
                      strokeWidth: '12px !important',
                      filter: 'drop-shadow(0 16px 32px rgba(59, 130, 246, 0.5)) !important',
                    },
                  },
                  '& .edgePath': {
                    '& path': {
                      stroke: '#3b82f6 !important',
                      strokeWidth: '8px !important',
                      filter: 'drop-shadow(0 6px 12px rgba(59, 130, 246, 0.4)) !important',
                    },
                    '& marker': {
                      fill: '#3b82f6 !important',
                    },
                  },
                  '& .cluster': {
                    '& rect': {
                      fill: 'rgba(30, 41, 59, 0.8) !important',
                      stroke: '#3b82f6 !important',
                      strokeWidth: '4px !important',
                      rx: '16px !important',
                      ry: '16px !important',
                    },
                    '& text': {
                      fontSize: '40px !important',
                      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important',
                      fontWeight: '700 !important',
                      fill: '#f1f5f9 !important',
                    },
                  },
                },
              }}
            >
          <Box
            id={`mermaid-${diagramId}`}
            sx={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          />
        </Box>
      ) : (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: '50%',
              border: theme => `3px solid ${alpha(theme.palette.primary.main, 0.3)}`,
              borderTop: theme => `3px solid ${theme.palette.primary.main}`,
              animation: 'spin 1s linear infinite',
              '@keyframes spin': {
                '0%': { transform: 'rotate(0deg)' },
                '100%': { transform: 'rotate(360deg)' },
              },
            }}
          />
          <GameText textVariant="secondary" sx={{ opacity: 0.6 }}>
            Loading diagram...
          </GameText>
        </Box>
      )}
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
            minHeight: 1200,
          }}
        >
          <MermaidDiagram
            diagram={WORKOUT_ALGORITHM_DIAGRAM}
            title="algorithm"
            description=""
          />
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
