// WorkoutAlgorithmInfographicStructured.js
import React from 'react'
import { motion } from 'framer-motion'

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
  text: '#e8e6ef',
  electricity: '#00ffff',
  electricityBright: '#ffffff',
  component: '#2a2a2a',
  componentBorder: '#4a4a4a',
  led: '#00ff00',
  ledBright: '#ffffff'
}

const COMPONENT_WIDTH = 120

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
          overflow: 'hidden' as const,
          boxShadow: `0 0 10px ${node.ledColor}40`
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
      whileHover={{ scale: 1.02 }}
    >
      {/* Electrical flow along borders */}
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

      {/* LED indicator */}
      <motion.div
        style={{
          position: 'absolute',
          top: '8px',
          right: '8px',
          width: '8px',
          height: '8px',
          borderRadius: '50%',
          background: node.ledColor,
          zIndex: 3
        }}
        animate={{
          opacity: [0.3, 1, 0.3],
          boxShadow: [
            `0 0 5px ${node.ledColor}`,
            `0 0 15px ${node.ledColor}`,
            `0 0 5px ${node.ledColor}`
          ]
        }}
        transition={{
          delay: totalDelay + 0.5,
          duration: 1.5,
          repeat: Infinity,
          repeatDelay: 2.5,
          ease: "easeInOut"
        }}
      />

      {/* Component pins/connectors */}
      <div style={{
        position: 'absolute',
        top: '50%',
        left: '-4px',
        width: '8px',
        height: '2px',
        background: COLORS.copper,
        borderRadius: '1px',
        transform: 'translateY(-50%)',
        zIndex: 2
      }} />
      <div style={{
        position: 'absolute',
        top: '50%',
        right: '-4px',
        width: '8px',
        height: '2px',
        background: COLORS.copper,
        borderRadius: '1px',
        transform: 'translateY(-50%)',
        zIndex: 2
      }} />
      <div style={{
        position: 'absolute',
        top: '-4px',
        left: '50%',
        width: '2px',
        height: '8px',
        background: COLORS.copper,
        borderRadius: '1px',
        transform: 'translateX(-50%)',
        zIndex: 2
      }} />
      <div style={{
        position: 'absolute',
        bottom: '-4px',
        left: '50%',
        width: '2px',
        height: '8px',
        background: COLORS.copper,
        borderRadius: '1px',
        transform: 'translateX(-50%)',
        zIndex: 2
      }} />
      
      {node.shape === 'triangle' ? (
        <>
          {/* Triangle background using CSS */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: COLORS.component,
            clipPath: 'polygon(50% 0%, 0% 100%, 100% 100%)',
            zIndex: 1,
            border: `2px solid ${COLORS.componentBorder}`
          }} />
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
    <div style={{
      background: COLORS.bg,
      minHeight: '100vh',
      padding: 20,
      color: COLORS.text,
      fontFamily: 'Inter, Roboto, system-ui, Arial',
      position: 'relative'
    }}>
      {/* Circuit board background pattern */}
      <div style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: `
          radial-gradient(circle at 20% 20%, ${COLORS.copper}20 1px, transparent 1px),
          radial-gradient(circle at 80% 80%, ${COLORS.copper}20 1px, transparent 1px),
          radial-gradient(circle at 40% 60%, ${COLORS.copper}15 1px, transparent 1px),
          linear-gradient(45deg, ${COLORS.circuitBoard} 0%, ${COLORS.bg} 100%)
        `,
        backgroundSize: '50px 50px, 30px 30px, 70px 70px, 100% 100%',
        opacity: 0.3,
        zIndex: 0
      }} />

      <div style={{
        maxWidth: 900,
        width: '100%',
        margin: '0 auto',
        borderRadius: 8,
        padding: 20,
        background: COLORS.circuitBoard,
        border: `2px solid ${COLORS.copper}`,
        position: 'relative',
        overflow: 'visible',
        zIndex: 1,
        boxShadow: `0 0 20px ${COLORS.copper}40, inset 0 0 20px rgba(0,0,0,0.3)`
      }}>
        {/* Circuit layout */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px', padding: '20px 0', position: 'relative' }}>
          
          {/* Input Phase - Sensors */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '15px',
            border: `2px solid ${COLORS.copper}`,
            borderRadius: '8px',
            background: `${COLORS.component}40`,
            position: 'relative'
          }}>
            <div style={{
              position: 'absolute',
              top: '-12px',
              left: '20px',
              background: COLORS.circuitBoard,
              padding: '0 10px',
              color: COLORS.electricity,
              fontSize: '12px',
              fontWeight: 'bold'
            }}>
              INPUT SENSORS
            </div>
            {['equipment', 'one_rm', 'previous'].map((id, index) => (
              <CircuitComponent key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={0} itemIndex={index} />
            ))}
          </div>

          {/* Processing Phase - Logic Chips */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '15px',
            border: `2px solid ${COLORS.copper}`,
            borderRadius: '8px',
            background: `${COLORS.component}40`,
            position: 'relative'
          }}>
            <div style={{
              position: 'absolute',
              top: '-12px',
              left: '20px',
              background: COLORS.circuitBoard,
              padding: '0 10px',
              color: COLORS.electricity,
              fontSize: '12px',
              fontWeight: 'bold'
            }}>
              PROCESSING UNITS
            </div>
            
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
            </div>
            
            {/* Second component - Available Equipment Filter */}
            <CircuitComponent node={STAGES.available_equipment} groupIndex={1} itemIndex={1} />
            
            {/* Third component - Exercise Matching Logic */}
            <CircuitComponent node={STAGES.exercise_matching} groupIndex={1} itemIndex={2} />
          </div>

          {/* Generation Phase - Advanced Processors */}
          <div style={{ 
            display: 'flex', 
            flexDirection: 'column',
            gap: '15px',
            padding: '15px',
            border: `2px solid ${COLORS.copper}`,
            borderRadius: '8px',
            background: `${COLORS.component}40`,
            position: 'relative'
          }}>
            <div style={{
              position: 'absolute',
              top: '-12px',
              left: '20px',
              background: COLORS.circuitBoard,
              padding: '0 10px',
              color: COLORS.electricity,
              fontSize: '12px',
              fontWeight: 'bold'
            }}>
              GENERATION PROCESSORS
            </div>
            {/* Top row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['session_time_allocation', 'rotating_exercise_selection', 'weak_point_targeting'].map((id, index) => (
                <CircuitComponent key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={2} itemIndex={index} />
              ))}
            </div>
            {/* Bottom row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['movement_balancing', 'weight_selection', 'set_scheme_generation'].map((id, index) => (
                <CircuitComponent key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={2} itemIndex={index + 3} />
              ))}
            </div>
          </div>

          {/* Output Phase - Actuators */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '15px',
            border: `2px solid ${COLORS.copper}`,
            borderRadius: '8px',
            background: `${COLORS.component}40`,
            position: 'relative'
          }}>
            <div style={{
              position: 'absolute',
              top: '-12px',
              left: '20px',
              background: COLORS.circuitBoard,
              padding: '0 10px',
              color: COLORS.electricity,
              fontSize: '12px',
              fontWeight: 'bold'
            }}>
              OUTPUT ACTUATORS
            </div>
            {['warmup', 'primary_out', 'secondary_out', 'accessory', 'conditioning'].map((id, index) => (
              <CircuitComponent key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={3} itemIndex={index} />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
