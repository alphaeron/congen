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
      border: `2px solid ${COLORS.componentBorder}`,
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
          paddingBottom: '15px',
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
            border: `2px solid ${COLORS.electricity}`,
            borderRadius: node.shape === 'circle' ? '50%' : node.shape === 'triangle' ? '0' : '4px',
            zIndex: 1,
            opacity: 0.8
          }}
        animate={{
          boxShadow: [
            `0 0 5px ${COLORS.electricity}`,
            `0 0 20px ${COLORS.electricityBright}`,
            `0 0 5px ${COLORS.electricity}`
          ]
        }}
        transition={{
          delay: totalDelay,
          duration: 2,
          repeat: Infinity,
          repeatDelay: 3,
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
                'drop-shadow(0 0 20px #00ffff)',
                'drop-shadow(0 0 5px #00ffff)'
              ]
            }}
            transition={{
              delay: totalDelay,
              duration: 2,
              repeat: Infinity,
              repeatDelay: 3,
              ease: "easeInOut"
            }}
          >
            <polygon
              points={`${COMPONENT_WIDTH/2},2 2,${COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO} ${COMPONENT_WIDTH-2},${COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO}`}
              fill="none"
              stroke={COLORS.electricity}
              strokeWidth="2"
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
          width: '15px',
          height: '3px',
          background: COLORS.electricityBright,
          borderRadius: '2px',
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
             padding: '15px',
             position: 'relative'
           }}>
             {/* Horizontal line at top of group */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${15 + COMPONENT_WIDTH / 2}px`,
               width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1
             }} />
             {/* Strategic extensions from top horizontal line */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: 'calc(-60vw + 50%)',
               width: '60vw',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.6
             }} />
             <div style={{
               position: 'absolute',
               top: '0px',
               right: `${15 + COMPONENT_WIDTH / 2 - 180}px`,
               width: '180px',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.4
             }} />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Strategic extensions from bottom horizontal line */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: 'calc(-46.67vw + 50%)',
              width: '46.67vw',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.5
            }} />
            <div style={{
              position: 'absolute',
              bottom: '0px',
              right: `${15 + COMPONENT_WIDTH / 2 - 90}px`,
              width: '90px',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.7
            }} />
             
             {/* First component - Equipment */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.equipment} groupIndex={0} itemIndex={0} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${COMPONENT_WIDTH}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${COMPONENT_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Second component - One Rep Maxes */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.one_rm} groupIndex={0} itemIndex={1} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${COMPONENT_WIDTH}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${COMPONENT_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Third component - Previous Workouts */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.previous} groupIndex={0} itemIndex={2} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Vertical line extending down to Processing Phase */}
            <div style={{
              position: 'absolute',
              bottom: '-34px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `${LINE_WIDTH}px`,
              height: '40px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
          </div>

           {/* Processing Phase - Logic Chips */}
           <div style={{ 
             display: 'flex', 
             justifyContent: 'space-between', 
             alignItems: 'center',
             padding: '15px',
             position: 'relative'
           }}>
             {/* Horizontal line at top of group */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${15 + COMPONENT_WIDTH / 2}px`,
               width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1
             }} />
             {/* Strategic extensions from top horizontal line */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: 'calc(-100vw + 50%)',
               width: '100vw',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.8
             }} />
             <div style={{
               position: 'absolute',
               top: '0px',
               right: `${15 + COMPONENT_WIDTH / 2 - 200}px`,
               width: '200px',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.3
             }} />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Strategic extensions from bottom horizontal line */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: 'calc(-25vw + 50%)',
              width: '25vw',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.4
            }} />
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: '50%',
              width: 'calc(100vw - 50%)',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.6
            }} />
             
             {/* First component - Sliding Window Analysis */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.sliding_window} groupIndex={1} itemIndex={0} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${COMPONENT_WIDTH}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${COMPONENT_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Second component - Available Equipment Filter */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.available_equipment} groupIndex={1} itemIndex={1} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${COMPONENT_WIDTH}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${COMPONENT_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Third component - Exercise Matching Logic */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.exercise_matching} groupIndex={1} itemIndex={2} />
              {/* Vertical line from top of component to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom of component to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Vertical line extending down to Generation Phase */}
            <div style={{
              position: 'absolute',
              bottom: '-34px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `${LINE_WIDTH}px`,
              height: '40px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
          </div>

           {/* Generation Phase - Advanced Processors */}
           <div style={{ 
             display: 'flex', 
             flexDirection: 'column',
             gap: '15px',
             padding: '15px',
             position: 'relative'
           }}>
             {/* Horizontal line at top of group */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${15 + COMPONENT_WIDTH / 2}px`,
               width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1
             }} />
             {/* Strategic extensions from top horizontal line */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${15 + COMPONENT_WIDTH / 2 - 140}px`,
               width: '140px',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.5
             }} />
             <div style={{
               position: 'absolute',
               top: '0px',
               right: `${15 + COMPONENT_WIDTH / 2 - 60}px`,
               width: '60px',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.9
             }} />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Strategic extensions from bottom horizontal line */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: 'calc(-50vw + 50%)',
              width: '50vw',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.7
            }} />
            <div style={{
              position: 'absolute',
              bottom: '0px',
              right: `${15 + COMPONENT_WIDTH / 2 - 170}px`,
              width: '170px',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.4
            }} />
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
                  height: '2px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from top of component to group top */}
                <div style={{
                  position: 'absolute',
                  top: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-15px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `15px`,
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
                  height: '2px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from top of component to group top */}
                <div style={{
                  position: 'absolute',
                  top: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-15px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `15px`,
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
                  top: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line extending down */}
                <div style={{
                  position: 'absolute',
                  left: '50%',
                  bottom: `-15px`,
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: `15px`,
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
                  height: '2px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from bottom of component to group bottom */}
                <div style={{
                  position: 'absolute',
                  bottom: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
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
                  height: '2px',
                  background: COLORS.electricity,
                  boxShadow: `0 0 5px ${COLORS.electricity}`,
                  zIndex: 1
                }} />
                {/* Vertical line from bottom of component to group bottom */}
                <div style={{
                  position: 'absolute',
                  bottom: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
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
                  bottom: '-15px',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: `${LINE_WIDTH}px`,
                  height: '15px',
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
              left: `${15 + COMPONENT_WIDTH / 2}px`,
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
             padding: '15px',
             position: 'relative'
           }}>
             {/* Horizontal line at top of group */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: `${15 + COMPONENT_WIDTH / 2}px`,
               width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1
             }} />
             {/* Strategic extensions from top horizontal line */}
             <div style={{
               position: 'absolute',
               top: '0px',
               left: 'calc(-33vw + 50%)',
               width: '33vw',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.6
             }} />
             <div style={{
               position: 'absolute',
               top: '0px',
               right: `${15 + COMPONENT_WIDTH / 2 - 160}px`,
               width: '160px',
               height: '2px',
               background: COLORS.electricity,
               boxShadow: `0 0 5px ${COLORS.electricity}`,
               zIndex: 1,
               opacity: 0.5
             }} />
            {/* Horizontal line at bottom of group */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: `${15 + COMPONENT_WIDTH / 2}px`,
              width: `calc(100% - ${30 + COMPONENT_WIDTH}px)`,
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1
            }} />
            {/* Strategic extensions from bottom horizontal line */}
            <div style={{
              position: 'absolute',
              bottom: '0px',
              left: 'calc(-100vw + 50%)',
              width: '100vw',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.4
            }} />
            <div style={{
              position: 'absolute',
              bottom: '0px',
              right: `${15 + COMPONENT_WIDTH / 2 - 100}px`,
              width: '100px',
              height: '2px',
              background: COLORS.electricity,
              boxShadow: `0 0 5px ${COLORS.electricity}`,
              zIndex: 1,
              opacity: 0.8
            }} />
             
             {/* First component - Warmup */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.warmup} groupIndex={3} itemIndex={0} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${TRIANGLE_LINE_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from tip of triangle to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom center of triangle to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: `${15 + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Second component - Primary */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.primary_out} groupIndex={3} itemIndex={1} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${TRIANGLE_LINE_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from tip of triangle to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom center of triangle to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: `${15 + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Third component - Secondary */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.secondary_out} groupIndex={3} itemIndex={2} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${TRIANGLE_LINE_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from tip of triangle to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom center of triangle to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: `${15 + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Fourth component - Accessory */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.accessory} groupIndex={3} itemIndex={3} />
              {/* Line extending from right edge */}
              <div style={{
                position: 'absolute',
                right: `-${TRIANGLE_LINE_RIGHT_OFFSET}px`,
                top: '50%',
                transform: 'translateY(-50%)',
                width: `${TRIANGLE_LINE_WIDTH}px`,
                height: '2px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from tip of triangle to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom center of triangle to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: `${15 + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
            
            {/* Fifth component - Conditioning */}
            <div style={{ position: 'relative' }}>
              <CircuitComponent node={STAGES.conditioning} groupIndex={3} itemIndex={4} />
              {/* Vertical line from tip of triangle to group top */}
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: '15px',
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
              {/* Vertical line from bottom center of triangle to group bottom */}
              <div style={{
                position: 'absolute',
                bottom: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                width: `${LINE_WIDTH}px`,
                height: `${15 + (COMPONENT_WIDTH - COMPONENT_WIDTH * EQUILATERAL_TRIANGLE_HEIGHT_RATIO)}px`,
                background: COLORS.electricity,
                boxShadow: `0 0 5px ${COLORS.electricity}`,
                zIndex: 1
              }} />
            </div>
          </div>
        </div>
          </div>
        </Box>
      </Box>
    </Box>
  )
}
