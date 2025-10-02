// WorkoutAlgorithmInfographicStructured.js
import React from 'react'
import { motion } from 'framer-motion'

/**
 * Structured infographic: left hierarchical flow + right numbered steps.
 * - Each algorithm stage is included exactly once (no duplicates).
 * - Designed after the reference: shapes, connectors, and right-side numbered cards.
 *
 * Usage:
 * <WorkoutAlgorithmInfographicStructured />
 *
 * Dependencies:
 * - framer-motion
 */

const COLORS = {
  bg: '#0b0b12',
  panel: 'rgba(255,255,255,0.03)',
  text: '#e8e6ef',
  accentA: '#ff8a6b',
  accentB: '#7dd3fc',
  accentC: '#c084fc',
  accentD: '#ffd36b',
  accentE: '#6be0ff'
}

// single stage definitions (each stage exists exactly once)
const STAGES = {

  // Inputs (top)
  equipment: { id: 'equipment', title: 'Equipment', colorFrom: '#ff8a6b', colorTo: '#ffb56b', shape: 'circle' },
  one_rm: { id: 'one_rm', title: 'One Rep Maxes', colorFrom: '#6be0ff', colorTo: '#7dd3fc', shape: 'circle' },
  previous: { id: 'previous', title: 'Previous Workouts', colorFrom: '#c084fc', colorTo: '#a855f7', shape: 'circle' },

  // Preparation row - individual exercise pool components
  sliding_window: { id: 'sliding_window', title: 'Sliding Window Analysis', colorFrom: '#60e0d8', colorTo: '#7c6bff', shape: 'rounded-rect' },
  available_equipment: { id: 'available_equipment', title: 'Available Equipment Filter', colorFrom: '#ff8ab6', colorTo: '#ffb56b', shape: 'rounded-rect' },
  exercise_matching: { id: 'exercise_matching', title: 'Exercise Matching Logic', colorFrom: '#f59e0b', colorTo: '#ff8a6b', shape: 'rounded-rect' },

  // Generation
  session_time_allocation: { id: 'session_time_allocation', title: 'Session Time Allocation', colorFrom: '#ffb86b', colorTo: '#ff7a7a', shape: 'rounded-rect' },
  rotating_exercise_selection: { id: 'rotating_exercise_selection', title: 'Rotating Exercise Selection', colorFrom: '#7ef27e', colorTo: '#6be0ff', shape: 'rounded-rect' },
  weak_point_targeting: { id: 'weak_point_targeting', title: 'Weak Point Targeting', colorFrom: '#c084fc', colorTo: '#a855f7', shape: 'rounded-rect' },
  movement_balancing: { id: 'movement_balancing', title: 'Movement Balancing', colorFrom: '#7dd3fc', colorTo: '#60e0d8', shape: 'rounded-rect' },
  weight_selection: { id: 'weight_selection', title: 'Weight Selection', colorFrom: '#ffd36b', colorTo: '#ff7a7a', shape: 'rounded-rect' },
  set_scheme_generation: { id: 'set_scheme_generation', title: 'Set Scheme Generation', colorFrom: '#f59e0b', colorTo: '#ff8a6b', shape: 'rounded-rect' },

  // Outputs (final row)
  warmup: { id: 'warmup', title: 'Warmup Stage', colorFrom: '#99f0b7', colorTo: '#7ef27e', shape: 'triangle' },
  primary_out: { id: 'primary_out', title: 'Primary Stage', colorFrom: '#ff8a8a', colorTo: '#ff4d4d', shape: 'triangle' },
  secondary_out: { id: 'secondary_out', title: 'Secondary Stage', colorFrom: '#7dd3fc', colorTo: '#60e0d8', shape: 'triangle' },
  accessory: { id: 'accessory', title: 'Accessory Stage', colorFrom: '#f7c08a', colorTo: '#ffb56b', shape: 'triangle' },
  conditioning: { id: 'conditioning', title: 'Conditioning Stage', colorFrom: '#7ea8ff', colorTo: '#6be0ff', shape: 'triangle' }
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

// HTML-based node component with gradient sweep effect
function NodeHTML({ node, groupIndex, itemIndex }: { node: any; groupIndex: number; itemIndex: number }) {
  const wrappedText = wrapText(node.title, node.shape === 'triangle' ? 80 : 120, 14)
  
  // Calculate delay based on group and item position for coordinated flow
  const baseDelay = groupIndex * 2 // 2 seconds between groups
  const itemDelay = itemIndex * 0.3 // 0.3 seconds between items in group
  const totalDelay = baseDelay + itemDelay
  
  const getShapeStyles = () => {
    const baseStyles = {
      width: '120px',
      height: '120px',
      background: `linear-gradient(135deg, ${node.colorFrom}, ${node.colorTo})`,
      borderRadius: '8px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '700',
      fontSize: '12px',
      textAlign: 'center' as const,
      lineHeight: '14px',
      padding: '8px',
      boxSizing: 'border-box' as const,
      border: '1px solid rgba(255,255,255,0.06)',
      position: 'relative' as const,
      overflow: 'hidden' as const
    }

    switch (node.shape) {
      case 'circle':
        return { ...baseStyles, borderRadius: '50%' }
      case 'diamond':
        return { 
          ...baseStyles, 
          width: '80px',
          height: '80px',
          transform: 'rotate(45deg)',
          borderRadius: '6px'
        }
      case 'triangle':
        return { 
          width: '120px',
          height: '120px',
          background: 'transparent',
          borderRadius: '0',
          display: 'flex',
          alignItems: 'flex-end',
          justifyContent: 'center',
          paddingBottom: '15px',
          position: 'relative' as const,
          color: '#fff',
          fontWeight: '700',
          fontSize: '12px',
          textAlign: 'center' as const,
          lineHeight: '14px',
          boxSizing: 'border-box' as const,
          border: 'none',
          overflow: 'hidden' as const
        }
      default:
        return baseStyles
    }
  }

  return (
    <motion.div
      style={getShapeStyles()}
      initial={{ scale: 0.88, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ duration: 0.5 }}
      whileHover={{ scale: 1.05 }}
    >
      {/* Gradient sweep overlay */}
      <motion.div
        style={{
          position: 'absolute',
          top: 0,
          left: '-100%',
          width: '100%',
          height: '100%',
          background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent)',
          zIndex: 1
        }}
        animate={{
          left: ['-100%', '100%']
        }}
        transition={{
          delay: totalDelay,
          duration: 1.5,
          repeat: Infinity,
          repeatDelay: 4,
          ease: "easeInOut"
        }}
      />
      
      {node.shape === 'triangle' ? (
        <>
          {/* Triangle background using CSS */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: `linear-gradient(135deg, ${node.colorFrom}, ${node.colorTo})`,
            clipPath: 'polygon(50% 0%, 0% 100%, 100% 100%)',
            zIndex: 1
          }} />
          {/* Text positioned at bottom */}
          <div style={{ 
            position: 'relative',
            zIndex: 2,
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            justifyContent: 'flex-end',
            height: '100%',
            width: '100%',
            paddingBottom: '2px'
          }}>
            {wrappedText.map((line, index) => (
              <div key={index} style={{ marginBottom: index < wrappedText.length - 1 ? '2px' : '0' }}>
                {line}
              </div>
            ))}
          </div>
        </>
      ) : (
        <div style={{ 
          position: 'relative',
          zIndex: 2,
          display: 'flex', 
          flexDirection: 'column', 
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
          width: '100%'
        }}>
          {wrappedText.map((line, index) => (
            <div key={index} style={{ marginBottom: index < wrappedText.length - 1 ? '2px' : '0' }}>
              {line}
            </div>
          ))}
        </div>
      )}
    </motion.div>
  )
}

// Main export component
export default function WorkoutAlgorithmInfographicStructured() {
  return (
    <div style={{
      background: `linear-gradient(180deg, ${COLORS.bg}, #08080b)`,
      minHeight: '100vh',
      padding: 12,
      color: COLORS.text,
      fontFamily: 'Inter, Roboto, system-ui, Arial'
    }}>
      <div style={{
        maxWidth: 800,
        width: '100%',
        margin: '0 auto',
        borderRadius: 12,
        padding: 12,
        background: 'linear-gradient(180deg, rgba(255,255,255,0.02), rgba(255,255,255,0.01))',
        border: '1px solid rgba(255,255,255,0.04)',
        position: 'relative',
        overflow: 'visible'
      }}>
        {/* Proper flex-based layout with margins and padding - compact for viewport */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', padding: '10px 0', position: 'relative' }}>
          
          {/* Input Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '8px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '8px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['equipment', 'one_rm', 'previous'].map((id, index) => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={0} itemIndex={index} />
            ))}
          </div>

          {/* Processing Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '8px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '8px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['sliding_window', 'available_equipment', 'exercise_matching'].map((id, index) => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={1} itemIndex={index} />
            ))}
          </div>

          {/* Generation Phase */}
          <div style={{ 
            display: 'flex', 
            flexDirection: 'column',
            gap: '10px',
            padding: '8px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '8px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {/* Top row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['session_time_allocation', 'rotating_exercise_selection', 'weak_point_targeting'].map((id, index) => (
                <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={2} itemIndex={index} />
              ))}
            </div>
            {/* Bottom row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['movement_balancing', 'weight_selection', 'set_scheme_generation'].map((id, index) => (
                <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={2} itemIndex={index + 3} />
              ))}
            </div>
          </div>

          {/* Output Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '8px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '8px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['warmup', 'primary_out', 'secondary_out', 'accessory', 'conditioning'].map((id, index) => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} groupIndex={3} itemIndex={index} />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
