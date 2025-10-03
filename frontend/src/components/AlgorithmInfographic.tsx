// WorkoutAlgorithmInfographicStructured.js
import React from 'react'
import { motion } from 'framer-motion'
import Box from '@mui/material/Box'
import { alpha } from '@mui/material/styles'
import { GameText, GameCard } from './GameTheme'

/**
 * Circuit Board Infographic: Algorithm stages displayed as electronic components
 * with flowing electricity along circuit traces and component borders.
 * - Each algorithm stage is a circuit component with electrical flow animations
 * - Circuit traces connect components with animated electrical current
 * - Circuit board background with copper traces and solder points
 *
 * Usage:
 * <WorkoutAlgorithmInfographicStructured />
 *
 * Dependencies:
 * - framer-motion
 */

const COLORS = {
  bg: '#0a0a0a',
  circuitBoard: '#1a1a1a',
  copper: '#b87333',
  copperBright: '#ffb366',
  solder: '#c0c0c0',
  text: '#00bcd4',
  electricity: '#00ffff',
  electricityBright: '#ffffff',
  component: 'rgba(0, 188, 212, 0.15)',
  componentBorder: '#4a4a4a',
  led: '#00ff00',
  ledBright: '#ffffff'
}

const COMPONENT_WIDTH = 120
const GROUP_GAP = 30
const GROUP_PADDING = 15
const LINE_WIDTH = 2
const EQUILATERAL_TRIANGLE_HEIGHT_RATIO = Math.sqrt(3) / 2
// Calculate horizontal line length between triangles using trigonometry
// For equilateral triangle, the horizontal line at 50% height should be 1/2 the base width
const TRIANGLE_HORIZONTAL_LINE_LENGTH = COMPONENT_WIDTH / 2
// Triangle line positioning calculations
const TRIANGLE_LINE_RIGHT_OFFSET = COMPONENT_WIDTH / 4 - 2
const TRIANGLE_LINE_WIDTH = TRIANGLE_HORIZONTAL_LINE_LENGTH - 4

// Circuit component definitions with electronic component types
const STAGES = {

  // Inputs (top) - Sensors and input modules
  equipment: { id: 'equipment', title: 'Equipment', componentType: 'sensor', shape: 'circle', ledColor: '#ff8a6b' },
  one_rm: { id: 'one_rm', title: 'One Rep Maxes', componentType: 'sensor', shape: 'circle', ledColor: '#6be0ff' },
  previous: { id: 'previous', title: 'Previous Workouts', componentType: 'sensor', shape: 'circle', ledColor: '#c084fc' },

  // Processing row - Logic chips and processors
  sliding_window: { id: 'sliding_window', title: 'Sliding Window Analysis', componentType: 'processor', shape: 'rounded-rect', ledColor: '#60e0d8' },
  available_equipment: { id: 'available_equipment', title: 'Available Equipment Filter', componentType: 'filter', shape: 'rounded-rect', ledColor: '#ff8ab6' },
  exercise_matching: { id: 'exercise_matching', title: 'Exercise Matching Logic', componentType: 'processor', shape: 'rounded-rect', ledColor: '#f59e0b' },

  // Generation - Advanced processing units
  session_time_allocation: { id: 'session_time_allocation', title: 'Session Time Allocation', componentType: 'processor', shape: 'rounded-rect', ledColor: '#ffb86b' },
  rotating_exercise_selection: { id: 'rotating_exercise_selection', title: 'Rotating Exercise Selection', componentType: 'processor', shape: 'rounded-rect', ledColor: '#7ef27e' },
  weak_point_targeting: { id: 'weak_point_targeting', title: 'Weak Point Targeting', componentType: 'processor', shape: 'rounded-rect', ledColor: '#c084fc' },
  movement_balancing: { id: 'movement_balancing', title: 'Movement Balancing', componentType: 'processor', shape: 'rounded-rect', ledColor: '#7dd3fc' },
  weight_selection: { id: 'weight_selection', title: 'Weight Selection', componentType: 'processor', shape: 'rounded-rect', ledColor: '#ffd36b' },
  set_scheme_generation: { id: 'set_scheme_generation', title: 'Set Scheme Generation', componentType: 'processor', shape: 'rounded-rect', ledColor: '#f59e0b' },

  // Outputs (final row) - Output modules and actuators
  warmup: { id: 'warmup', title: 'Warmup Stage', componentType: 'output', shape: 'triangle', ledColor: '#99f0b7' },
  primary_out: { id: 'primary_out', title: 'Primary Stage', componentType: 'output', shape: 'triangle', ledColor: '#ff8a8a' },
  secondary_out: { id: 'secondary_out', title: 'Secondary Stage', componentType: 'output', shape: 'triangle', ledColor: '#7dd3fc' },
  accessory: { id: 'accessory', title: 'Accessory Stage', componentType: 'output', shape: 'triangle', ledColor: '#f7c08a' },
  conditioning: { id: 'conditioning', title: 'Conditioning Stage', componentType: 'output', shape: 'triangle', ledColor: '#7ea8ff' }
}

// Helper function to wrap text within shapes - ONLY break at whitespace
function wrapText(text: string, maxWidth: number, fontSize: number): string[] {
  const words = text.split(' ')
  const lines: string[] = []
  let currentLine = ''
  
  for (const word of words) {
    const testLine = currentLine + (currentLine ? ' ' : '') + word
    const testWidth = testLine.length * fontSize * 0.6 // Character width estimation
    
    if (testWidth <= maxWidth) {
      currentLine = testLine
    } else {
      if (currentLine) {
        lines.push(currentLine)
        currentLine = word
      } else {
        // If single word is too long, we need bigger shapes - don't break mid-word
        lines.push(word)
      }
    }
  }
  
  if (currentLine) {
    lines.push(currentLine)
  }
  
  return lines
}

// Circuit component with electrical flow animations
function CircuitComponent({ node, groupIndex, itemIndex }: { node: any; groupIndex: number; itemIndex: number }) {
  const wrappedText = wrapText(node.title, node.shape === 'triangle' ? 80 : 120, 12)
  
  // Calculate delay based on group and item position for coordinated electrical flow
  const baseDelay = groupIndex * 1.5 // 1.5 seconds between groups
  const itemDelay = itemIndex * 0.2 // 0.2 seconds between items in group
  const totalDelay = baseDelay + itemDelay
  
  const getComponentStyles = () => {
    const baseStyles = {
      width: `${COMPONENT_WIDTH}px`,
      height: `${COMPONENT_WIDTH}px`,
      background: COLORS.component,
      borderRadius: '4px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: COLORS.text,
      fontWeight: '600',
      fontSize: '11px',
      textAlign: 'center' as const,
      lineHeight: '13px',
      textShadow: '0 0 5px #00bcd4',
      padding: '8px',
      boxSizing: 'border-box' as const,
      border: `${LINE_WIDTH}px solid ${COLORS.componentBorder}`,
      position: 'relative' as const,
      overflow: 'hidden' as const,
      boxShadow: `0 0 10px ${node.ledColor}40, inset 0 0 10px rgba(0,0,0,0.3)`
    }

    switch (node.shape) {
      case 'circle':
        return { ...baseStyles, borderRadius: '50%' }
      case 'triangle':
        return { 
          width: `${COMPONENT_WIDTH}px`,
          height: `${COMPONENT_WIDTH}px`,
          background: 'transparent',
          borderRadius: '0',
          display: 'flex',
          alignItems: 'flex-end',
          justifyContent: 'center',
          paddingBottom: `${GROUP_PADDING}px`,
          position: 'relative' as const,
          color: COLORS.text,
          fontWeight: '600',
          fontSize: '11px',
          textAlign: 'center' as const,
          lineHeight: '13px',
          boxSizing: 'border-box' as const,
          border: 'none',
          borderWidth: '0',
          borderStyle: 'none',
          overflow: 'visible' as const,
          boxShadow: 'none',
          outline: 'none'
        }
      default:
        return baseStyles
    }
  }

  return (
    <motion.div
      style={getComponentStyles()}
      initial={{ scale: 0.9, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ duration: 0.6 }}
    >
      {/* Electrical flow along borders - disabled for Output Actuators triangles */}
      {!(node.shape === 'triangle' && groupIndex === 3) && (
        <motion.div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            border: `${LINE_WIDTH}px solid ${COLORS.electricity}`,
            borderRadius: node.shape === 'circle' ? '50%' : node.shape === 'triangle' ? '0' : '4px',
            zIndex: 1,
            opacity: 0.8
          }}
        animate={{
          boxShadow: [
            `0 0 5px ${COLORS.electricity}`,
            `0 0 25px ${COLORS.electricityBright}`,
            `0 0 5px ${COLORS.electricity}`
          ],
          borderColor: [
            COLORS.electricity,
            COLORS.electricityBright,
            COLORS.electricity
          ]
        }}
        transition={{
          delay: totalDelay,
          duration: 1.5,
          repeat: Infinity,
          repeatDelay: 2,
          ease: "easeInOut"
        }}
      />
      )}
      
      {node.shape === 'triangle' ? (
        <>
          {/* Pure triangle shape - no rectangular container */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: COLORS.component,
            clipPath: `polygon(50% 0%, 0% ${(EQUILATERAL_TRIANGLE_HEIGHT_RATIO * 100).toFixed(1)}%, 100% ${(EQUILATERAL_TRIANGLE_HEIGHT_RATIO * 100).toFixed(1)}%)`,
            zIndex: 1,
            boxShadow: `0 0 10px ${node.ledColor}40, inset 0 0 10px rgba(0,0,0,0.3)`
          }} />
          {/* Triangle border using SVG with electrical flow animation */}
          <motion.svg 
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: '100%',
              zIndex: 2,
              pointerEvents: 'none'
            }}
            animate={{
              filter: [
                'drop-shadow(0 0 5px #00ffff)',
                'drop-shadow(0 0 25px #00ffff)',
                'drop-shadow(0 0 5px #00ffff)'
              ]
            }}
            transition={{
              delay: totalDelay,
              duration: 1.5,
              repeat: Infinity,
              repeatDelay: 2,
              ease: "easeInOut"
            }}
          >
            <motion.polygon
              points={`${COMPONENT_WIDTH/2},2 2,${COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO} ${COMPONENT_WIDTH-2},${COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}`}
              fill="none"
              stroke={COLORS.electricity}
              strokeWidth="2"
              animate={{
                stroke: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ],
                strokeWidth: [2, 3, 2]
              }}
              transition={{
                delay: totalDelay,
                duration: 1.5,
                repeat: Infinity,
                repeatDelay: 2,
                ease: "easeInOut"
              }}
            />
          </motion.svg>
          {/* Text positioned at bottom */}
          <div style={{ 
            position: 'relative',
            zIndex: 4,
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            justifyContent: 'flex-end',
            height: '100%',
            width: '100%',
            paddingBottom: '8px'
          }}>
            {wrappedText.map((line, index) => (
              <div key={index} style={{ marginBottom: index < wrappedText.length - 1 ? '1px' : '0' }}>
                {line}
              </div>
            ))}
          </div>
        </>
      ) : (
        <div style={{ 
          position: 'relative',
          zIndex: 4,
          display: 'flex', 
          flexDirection: 'column', 
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
          width: '100%'
        }}>
          {wrappedText.map((line, index) => (
            <div key={index} style={{ marginBottom: index < wrappedText.length - 1 ? '1px' : '0' }}>
              {line}
            </div>
          ))}
        </div>
      )}
    </motion.div>
  )
}

// Simple connecting line component
function ConnectingLine({ 
  startX, 
  startY, 
  endX, 
  endY, 
  delay = 0 
}: { 
  startX: number; 
  startY: number; 
  endX: number; 
  endY: number; 
  delay?: number;
}) {
  const width = Math.abs(endX - startX)
  const height = Math.abs(endY - startY)
  const left = Math.min(startX, endX)
  const top = Math.min(startY, endY)

  return (
    <motion.div
      style={{
        position: 'absolute',
        left: `${left}px`,
        top: `${top}px`,
        width: `${width}px`,
        height: `${height}px`,
        background: COLORS.electricity,
        zIndex: 1,
        boxShadow: `0 0 5px ${COLORS.electricity}`,
        borderRadius: '1px'
      }}
      initial={{ scaleX: 0, scaleY: 0 }}
      animate={{ scaleX: 1, scaleY: 1 }}
      transition={{ delay: delay, duration: 0.5 }}
    >
      {/* Flowing electricity along the line */}
      <motion.div
        style={{
          position: 'absolute',
          top: '-1px',
          left: 0,
          width: `${GROUP_PADDING}px`,
          height: '3px',
          background: COLORS.electricityBright,
          borderRadius: `${LINE_WIDTH}px`,
          boxShadow: `0 0 8px ${COLORS.electricityBright}`
        }}
        animate={{
          left: ['0%', '100%']
        }}
        transition={{
          delay: delay + 0.3,
          duration: 1.5,
          repeat: Infinity,
          repeatDelay: 2,
          ease: "linear"
        }}
      />
    </motion.div>
  )
}

// Main circuit board component
export default function WorkoutAlgorithmInfographicStructured() {
  return (
    <Box
      id="algorithm"
      sx={{
        py: { xs: 8, sm: 12 },
        position: 'relative',
        width: '100%',
        px: { xs: 2, sm: 4, md: 6 },
        background: COLORS.bg,
        minHeight: '100vh',
        color: COLORS.text,
        fontFamily: 'Inter, Roboto, system-ui, Arial'
    }}>
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', lg: 'row' },
          alignItems: 'center',
          gap: { xs: 4, lg: 8 },
        }}
      >
        {/* Text Content */}
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
        
        {/* Infographic Content */}
        <Box
          sx={{
            flex: { xs: 1, lg: 1.2 },
            minHeight: 400,
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 3,
            overflow: 'visible',
          }}
        >
          <div style={{
            margin: '0 auto',
            position: 'relative',
            overflow: 'visible',
            zIndex: 1
          }}>
            {/* Circuit layout */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: `${GROUP_GAP}px`, padding: '20px 0', position: 'relative' }}>
          
           {/* Input Phase - Sensors */}
           <div style={{ 
             display: 'flex', 
             justifyContent: 'space-between', 
             alignItems: 'center',
             padding: `${GROUP_PADDING}px`,
             position: 'relative'
           }}>
             {/* Enhanced horizontal line at top of group */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
                 width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 15px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 0,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             {/* Enhanced strategic extensions from top horizontal line */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: 'calc(-60vw + 50%)',
                 width: '60vw',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.6
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 0.5,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 180}px`,
                 width: '180px',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.4
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 0.8,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
            {/* Enhanced horizontal line at bottom of group */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
                width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 15px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 1.5,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            {/* Enhanced strategic extensions from bottom horizontal line */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: 'calc(-46.67vw + 50%)',
                width: '46.67vw',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.5
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 2,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 3 * GROUP_GAP}px`,
                width: '90px',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.7
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 2.3,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
             
             {/* First component - Equipment */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.equipment} groupIndex={0} itemIndex={0} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending upward from corner */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `calc(-${GROUP_PADDING}px - 10vh)`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '10vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.2,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Second component - One Rep Maxes */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.one_rm} groupIndex={0} itemIndex={1} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending upward from corner (shorter) */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `calc(-${GROUP_PADDING}px - 5vh)`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '5vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.5,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Third component - Previous Workouts */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.previous} groupIndex={0} itemIndex={2} />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending to top of Advanced Algorithm section */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `calc(-100% - ${GROUP_PADDING}px + ${2 * LINE_WIDTH}px)`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '100%',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.8,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 0.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Enhanced vertical line extending down to Processing Phase */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '-34px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '40px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 15px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 3,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
          </div>

           {/* Processing Phase - Logic Chips */}
           <div style={{ 
             display: 'flex', 
             justifyContent: 'space-between', 
             alignItems: 'center',
             padding: `${GROUP_PADDING}px`,
             position: 'relative'
           }}>
             {/* Enhanced horizontal line at top of group */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
                 width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 15px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 4,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             {/* Enhanced strategic extensions from top horizontal line */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: 'calc(-100vw + 50%)',
                 width: '100vw',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.8
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 4.5,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 200}px`,
                 width: '200px',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.3
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 4.8,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
            {/* Enhanced horizontal line at bottom of group */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
                width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 15px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 5.5,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            {/* Enhanced strategic extensions from bottom horizontal line */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: 'calc(-25vw + 50%)',
                width: '25vw',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.4
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 6,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: '50%',
                width: 'calc(100vw - 50%)',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.6
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 6.3,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
             
             {/* First component - Sliding Window Analysis */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.sliding_window} groupIndex={1} itemIndex={0} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Second component - Available Equipment Filter */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.available_equipment} groupIndex={1} itemIndex={1} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Third component - Exercise Matching Logic */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.exercise_matching} groupIndex={1} itemIndex={2} />
              {/* Enhanced vertical line from top of component to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom of component to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 4.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Enhanced vertical line extending down to Generation Phase */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '-34px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '40px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 15px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 6.5,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
          </div>

           {/* Generation Phase - Advanced Processors */}
           <div style={{ 
             display: 'flex', 
             flexDirection: 'column',
             gap: `${GROUP_PADDING}px`,
             padding: `${GROUP_PADDING}px`,
             position: 'relative'
           }}>
             {/* Enhanced horizontal line at top of group */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
                 width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 15px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 7,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             {/* Enhanced strategic extensions from top horizontal line */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 140}px`,
                 width: '140px',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.5
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 7.5,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 60}px`,
                 width: '60px',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.9
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 7.8,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
              height: `${LINE_WIDTH}px`,
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Enhanced strategic extensions from bottom horizontal line */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: 'calc(-50vw + 50%)',
                width: '50vw',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.7
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 8.5,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 170}px`,
                width: '170px',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.4
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 8.8,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
             {/* Top row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {/* First component - Session Time Allocation */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.session_time_allocation} groupIndex={2} itemIndex={0} />
                {/* Line extending from right edge */}
                <div style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from top of component to group top */}
                <div style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-${GROUP_PADDING}px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
              
              {/* Second component - Rotating Exercise Selection */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.rotating_exercise_selection} groupIndex={2} itemIndex={1} />
                {/* Line extending from right edge */}
                <div style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from top of component to group top */}
                <div style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-${GROUP_PADDING}px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
              
              {/* Third component - Weak Point Targeting */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.weak_point_targeting} groupIndex={2} itemIndex={2} />
                {/* Vertical line from top of component to group top */}
                <div style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-${GROUP_PADDING}px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
            </div>
            {/* Bottom row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {/* First component - Movement Balancing */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.movement_balancing} groupIndex={2} itemIndex={3} />
                {/* Line extending from right edge */}
                <div style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from bottom of component to group bottom */}
                <div style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
              
              {/* Second component - Weight Selection */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.weight_selection} groupIndex={2} itemIndex={4} />
                {/* Line extending from right edge */}
                <div style={{
                  position: 'absolute',
                  right: `-${COMPONENT_WIDTH}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${COMPONENT_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from bottom of component to group bottom */}
                <div style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
              
              {/* Third component - Set Scheme Generation */}
              <div style={{ position: 'relative' }}>
                <CircuitComponent node={STAGES.set_scheme_generation} groupIndex={2} itemIndex={5} />
                {/* Vertical line from bottom of component to group bottom */}
                <div style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
              </div>
            </div>
            
            {/* Vertical line extending down to Output Phase */}
            <div style={{
              position: 'absolute',
              bottom: '-34px',
              left: '50%',
              transform: 'translateX(-50%)',
              width: `${LINE_WIDTH}px`,
              height: '40px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
          </div>

           {/* Output Phase - Actuators */}
           <div style={{ 
             display: 'flex', 
             justifyContent: 'space-between', 
             alignItems: 'center',
             padding: `${GROUP_PADDING}px`,
             position: 'relative'
           }}>
             {/* Horizontal line at top of group */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
               width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
               height: `${LINE_WIDTH}px`,
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1
             }} />
             {/* Enhanced strategic extensions from top horizontal line */}
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 left: 'calc(-33vw + 50%)',
                 width: '33vw',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.6
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 9.5,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
             <motion.div 
               style={{
                 position: 'absolute',
                 top: '0px',
                 right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 160}px`,
                 width: '160px',
                 height: `${LINE_WIDTH}px`,
                 background: COLORS.electricity,
                 boxShadow: `0 0 5px ${COLORS.electricity}`,
                 zIndex: 1,
                 opacity: 0.5
               }}
               animate={{
                 boxShadow: [
                   `0 0 5px ${COLORS.electricity}`,
                   `0 0 12px ${COLORS.electricityBright}`,
                   `0 0 5px ${COLORS.electricity}`
                 ],
                 background: [
                   COLORS.electricity,
                   COLORS.electricityBright,
                   COLORS.electricity
                 ]
               }}
               transition={{
                 delay: 9.8,
                 duration: 2,
                 repeat: Infinity,
                 repeatDelay: 3,
                 ease: "easeInOut"
               }}
             />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${GROUP_PADDING + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${GROUP_GAP + COMPONENT_WIDTH}px)`,
              height: `${LINE_WIDTH}px`,
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Enhanced strategic extensions from bottom horizontal line */}
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                left: 'calc(-100vw + 50%)',
                width: '100vw',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.4
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 10.5,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
            <motion.div 
              style={{
                position: 'absolute',
                bottom: '0px',
                right: `${GROUP_PADDING + COMPONENT_WIDTH / 2 - 100}px`,
                width: '100px',
                height: `${LINE_WIDTH}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1,
                opacity: 0.8
              }}
              animate={{
                boxShadow: [
                  `0 0 5px ${COLORS.electricity}`,
                  `0 0 12px ${COLORS.electricityBright}`,
                  `0 0 5px ${COLORS.electricity}`
                ],
                background: [
                  COLORS.electricity,
                  COLORS.electricityBright,
                  COLORS.electricity
                ]
              }}
              transition={{
                delay: 10.8,
                duration: 2,
                repeat: Infinity,
                repeatDelay: 3,
                ease: "easeInOut"
              }}
            />
             
             {/* First component - Warmup */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.warmup} groupIndex={3} itemIndex={0} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${TRIANGLE_LINE_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from tip of triangle to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom center of triangle to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending downward from triangle bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px - ${COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '10vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 11.2,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Second component - Primary */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.primary_out} groupIndex={3} itemIndex={1} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${TRIANGLE_LINE_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from tip of triangle to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom center of triangle to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending downward from triangle bottom (very stubby) */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px - ${COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '6vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 11.5,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Third component - Secondary */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.secondary_out} groupIndex={3} itemIndex={2} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${TRIANGLE_LINE_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from tip of triangle to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom center of triangle to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 7.7,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending downward from triangle bottom (shorter) */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px - ${COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '5vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 11.8,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Fourth component - Accessory */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.accessory} groupIndex={3} itemIndex={3} />
              {/* Enhanced line extending from right edge */}
              <motion.div 
                style={{
                  position: 'absolute',
                  right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: `${TRIANGLE_LINE_WIDTH}px`,
                  height: `${LINE_WIDTH}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 8.0,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from tip of triangle to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 8.0,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom center of triangle to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 8.0,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending to bottom of container */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px - ${COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `calc(100% + ${GROUP_PADDING - 2 * LINE_WIDTH}px)`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 12.1,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            {/* Fifth component - Conditioning */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.conditioning} groupIndex={3} itemIndex={4} />
              {/* Enhanced vertical line from tip of triangle to group top */}
              <motion.div 
                style={{
                  position: 'absolute',
                  top: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 8.3,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical line from bottom center of triangle to group bottom */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `${GROUP_PADDING + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 15px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 8.3,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
              {/* Enhanced vertical background circuit extending downward from triangle bottom (very stubby) */}
              <motion.div 
                style={{
                  position: 'absolute',
                  bottom: `-${GROUP_PADDING}px - ${COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}px`,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '3vh',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1,
                  opacity: 0.3
                }}
                animate={{
                  boxShadow: [
                    `0 0 5px ${COLORS.electricity}`,
                    `0 0 10px ${COLORS.electricityBright}`,
                    `0 0 5px ${COLORS.electricity}`
                  ],
                  background: [
                    COLORS.electricity,
                    COLORS.electricityBright,
                    COLORS.electricity
                  ]
                }}
                transition={{
                  delay: 12.4,
                  duration: 2,
                  repeat: Infinity,
                  repeatDelay: 3,
                  ease: "easeInOut"
                }}
              />
            </div>
          </div>
        </div>
          </div>
        </Box>
      </Box>
    </Box>
  )
}
