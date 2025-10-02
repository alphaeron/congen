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

// simple animation variants
const fadeIn = { initial: { opacity: 0, y: 8 }, animate: { opacity: 1, y: 0 }, transition: { duration: 0.6 } }

// small helper: gradient id
const gradId = (id: string) => `g-${id}`

// SVG shapes renderer - ALL shapes exactly the same size
function Shape({ node, size = 180 }: { node: any; size?: number }) {
  const r = size / 2
  const { colorFrom, colorTo, shape } = node
  const gradient = `url(#${gradId(node.id)})`

  if (shape === 'circle') {
    return <circle r={r} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'rounded-pill') {
    return <rect x={-r - 40} y={-r/2} width={size + 80} height={r} rx={r/2} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'rounded-rect') {
    return <rect x={-r} y={-r} width={size} height={size} rx={20} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'diamond') {
    return <polygon points={`${0},${-r} ${r},0 0,${r} ${-r},0`} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'filter-hex') {
    return <rect x={-r} y={-r} width={size} height={size} rx={25} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'triangle') {
    return <polygon points={`0,${-r} ${r},${r} ${-r},${r}`} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  // fallback
  return <circle r={r} fill={gradient} />
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

// HTML-based node component using proper CSS
function NodeHTML({ node }: { node: any }) {
  const wrappedText = wrapText(node.title, node.shape === 'triangle' ? 80 : 120, 14)
  
  const getShapeStyles = () => {
    const baseStyles = {
      width: '140px',
      height: '140px',
      background: `linear-gradient(135deg, ${node.colorFrom}, ${node.colorTo})`,
      borderRadius: '10px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '700',
      fontSize: '14px',
      textAlign: 'center' as const,
      lineHeight: '16px',
      padding: '12px',
      boxSizing: 'border-box' as const,
      border: '1px solid rgba(255,255,255,0.06)'
    }

    switch (node.shape) {
      case 'circle':
        return { ...baseStyles, borderRadius: '50%' }
      case 'diamond':
        return { 
          ...baseStyles, 
          width: '100px',
          height: '100px',
          transform: 'rotate(45deg)',
          borderRadius: '8px'
        }
      case 'triangle':
        return { 
          width: '140px',
          height: '140px',
          background: 'transparent',
          borderRadius: '0',
          display: 'flex',
          alignItems: 'flex-end',
          justifyContent: 'center',
          paddingBottom: '20px',
          position: 'relative' as const,
          color: '#fff',
          fontWeight: '700',
          fontSize: '14px',
          textAlign: 'center' as const,
          lineHeight: '16px',
          boxSizing: 'border-box' as const,
          border: 'none'
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

// Clean group-to-group connector with downward flow
function ConnectorPath({ x1, y1, x2, y2, index = 0, colorFrom = '#7dd3fc', colorTo = '#ff8a6b', dataType = '', flowDirection = 'forward' }: { x1: number; y1: number; x2: number; y2: number; index?: number; colorFrom?: string; colorTo?: string; dataType?: string; flowDirection?: string }) {
  const mx = (x1 + x2) / 2
  const my = (y1 + y2) / 2
  
  // Simple straight vertical line for clean group-to-group flow
  const d = `M ${x1} ${y1} L ${x2} ${y2}`

  // Arrow at the bottom
  const arrowSize = 8
  const arrowX = x2
  const arrowY = y2 - 10

  return (
    <>
        <defs>
        <linearGradient id={`conn-${index}`} x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor={colorFrom} stopOpacity="0.95" />
          <stop offset="100%" stopColor={colorTo} stopOpacity="0.95" />
              </linearGradient>
        {/* Glow filter for enhanced visibility */}
        <filter id={`glow-${index}`} x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
            <feMerge>
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>
        </defs>

      {/* Main connection path - straight vertical line */}
      <motion.path
        d={d}
        stroke={`url(#conn-${index})`}
        strokeWidth={4}
        fill="none"
        strokeLinecap="round"
        filter={`url(#glow-${index})`}
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={{ duration: 1.2, delay: 0.1 * index }}
      />
      
      {/* Simple animated data particles flowing downward */}
      {[...Array(2)].map((_, i) => (
              <motion.circle
          key={i}
          r="2"
          fill={colorFrom}
          initial={{ cx: x1, cy: y1, opacity: 0 }}
          animate={{ 
            cx: x2, 
            cy: y2 - 10,
            opacity: [0, 1, 0]
          }}
          transition={{
            delay: 1.2 + i * 0.3,
            duration: 2.0,
            repeat: Infinity,
            ease: "linear"
          }}
        />
      ))}
      
      
      {/* Data type label */}
      {dataType && (
        <motion.text
          x={mx}
          y={my - 20}
          textAnchor="middle"
          fontSize="16"
          fill={colorTo}
          fontWeight="700"
          initial={{ opacity: 0, y: my - 10 }}
          animate={{ opacity: 1, y: my - 20 }}
          transition={{ delay: 1.5, duration: 0.4 }}
        >
          {dataType}
        </motion.text>
      )}
    </>
  )
}

// Right side numbered step card (process style)
function RightCard({ node, number, top }: { node: any; number: number; top: number }) {
  const bg = `linear-gradient(180deg, ${node.colorFrom}12 0%, ${node.colorTo}08 100%)`
  return (
    <motion.div
      initial={{ opacity: 0, x: 12 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.1 * number }}
      style={{
        position: 'absolute',
        left: 980,
        top,
        width: 320,
        padding: 14,
        borderRadius: 14,
        background: bg,
        border: '1px solid rgba(255,255,255,0.04)',
        color: '#e6e3f0',
        display: 'flex',
        gap: 12,
        alignItems: 'flex-start',
      }}
    >
      <div style={{
        width: 46, height: 46, borderRadius: 999, background: `linear-gradient(135deg, ${node.colorFrom}, ${node.colorTo})`,
        display: 'grid', placeItems: 'center', fontWeight: 800, color: 'white'
      }}>
        {number < 10 ? `0${number}` : number}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: 800 }}>{node.title}</div>
        <div style={{ marginTop: 6, fontSize: 13, color: '#cfd6ea' }}>{node.subtitle || node.title}</div>
      </div>
    </motion.div>
  )
}

// Main export component
export default function WorkoutAlgorithmInfographicStructured() {
  // fixed layout coordinates (keeps structured look like reference)
  // left column x positions and y spacing
  const leftX = 160
  const midX = 420
  const rightX = 680

  // Reactive centering system - calculate positions dynamically
  const containerWidth = 1200 // Bigger container to accommodate larger shapes
  const containerStartX = 160
  
  // Helper function to center items within a row - truly reactive
  const centerItemsInRow = (itemCount: number, y: number) => {
    const items = []
    const shapeSize = 180 // All shapes are the same size now
    const totalSpacing = containerWidth - (itemCount * shapeSize)
    const spacing = totalSpacing / (itemCount + 1) // Equal spacing on both sides and between
    
    for (let i = 0; i < itemCount; i++) {
      const x = containerStartX + spacing + (i * (shapeSize + spacing)) + (shapeSize / 2) // Half shape size
      items.push([x, y])
    }
    return items
  }
  
  // Calculate positions reactively with proper spacing between bounding boxes
  const inputPositions = centerItemsInRow(3, 150)
  const processingPositions = centerItemsInRow(3, 400) // Increased from 350
  const generationTopPositions = centerItemsInRow(3, 700) // Moved down to add space above generation container
  const generationBottomPositions = centerItemsInRow(2, 850) // Proper spacing from top row
  const outputPositions = centerItemsInRow(5, 1200) // Moved further down to match spacing of other boxes
  
  // Calculate container dimensions dynamically
  const shapeSize = 180
  const padding = 32
  const rowSpacing = 130 // Space between rows in generation phase
  
  const inputContainerHeight = shapeSize + padding
  const processingContainerHeight = shapeSize + padding
  const generationContainerHeight = (shapeSize * 2) + rowSpacing + padding
  const outputContainerHeight = shapeSize + padding
  
  const inputContainerY = 150 - (shapeSize / 2) - (padding / 2)
  const processingContainerY = 400 - (shapeSize / 2) - (padding / 2) // Updated to match new position
  const generationContainerY = 700 - (shapeSize / 2) - (padding / 2) // Updated to match new position
  const outputContainerY = 1200 - (shapeSize / 2) - (padding / 2) // Updated to match new position
  
  // Calculate dynamic SVG dimensions
  const svgWidth = containerStartX + containerWidth + 160 // 160px margin on right
  const svgHeight = outputContainerY + outputContainerHeight + 100 // 100px margin on bottom
  
  const pos = {
    // INPUT PHASE - reactively centered (3 items)
    equipment: inputPositions[0],
    one_rm: inputPositions[1],
    previous: inputPositions[2],

    // PROCESSING PHASE - reactively centered (3 items)
    data_prep: processingPositions[0],
    exercise_pool: processingPositions[1],
    set_scheme: processingPositions[2],

    // GENERATION PHASE - reactively centered
    // Top row (3 items)
    primary_select: generationTopPositions[0],
    secondary_select: generationTopPositions[1],
    session_time: generationTopPositions[2],
    // Bottom row (2 items)
    matching: generationBottomPositions[0],
    weak_point: generationBottomPositions[1],

    // OUTPUT PHASE - reactively centered (5 items)
    warmup: outputPositions[0],
    primary_out: outputPositions[1],
    secondary_out: outputPositions[2],
    accessory: outputPositions[3],
    conditioning: outputPositions[4]
  }

  // Group-to-group connectors for clean flow - keep vertical lines, remove diagonal lines
  const groupConnectors = [
    // Input Phase to Processing Phase
    { from: 'input_phase', to: 'processing_phase', dataType: '' },
    
    // Processing Phase to Generation Phase
    { from: 'processing_phase', to: 'generation_phase', dataType: '' },
    
    // Generation Phase to Output Phase
    { from: 'generation_phase', to: 'output_phase', dataType: '' }
  ]

  // Group center positions for clean connections - reactively calculated
  const groupCenters = {
    input_phase: [inputPositions[1][0], 150],      // Center of input phase (middle item)
    processing_phase: [processingPositions[1][0], 400], // Center of processing phase (middle item)
    generation_phase: [inputPositions[1][0], 775], // Center of generation phase (between two rows, use same x as input)
    output_phase: [outputPositions[2][0], 1200]      // Center of output phase (middle item)
  }

  // order for right-side cards (roughly top-to-bottom order)
  const rightOrder = [
    'equipment',
    'one_rm',
    'previous',
    'sliding_window',
    'available_equipment',
    'exercise_matching',
    'session_time_allocation',
    'rotating_exercise_selection',
    'weak_point_targeting',
    'movement_balancing',
    'weight_selection',
    'set_scheme_generation',
    'warmup',
    'primary_out',
    'secondary_out',
    'accessory',
    'conditioning'
  ]

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
        {/* Proper flex-based layout with margins and padding */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '40px', padding: '20px 0', position: 'relative' }}>
          
          {/* Input Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '16px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '12px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['equipment', 'one_rm', 'previous'].map(id => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} />
            ))}
          </div>

          {/* Processing Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '16px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '12px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['sliding_window', 'available_equipment', 'exercise_matching'].map(id => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} />
            ))}
          </div>

          {/* Generation Phase */}
          <div style={{ 
            display: 'flex', 
            flexDirection: 'column',
            gap: '20px',
            padding: '16px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '12px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {/* Top row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['session_time_allocation', 'rotating_exercise_selection', 'weak_point_targeting'].map(id => (
                <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} />
              ))}
            </div>
            {/* Bottom row */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {['movement_balancing', 'weight_selection', 'set_scheme_generation'].map(id => (
                <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} />
              ))}
            </div>
          </div>

          {/* Output Phase */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '16px',
            border: '1px dashed rgba(255,255,255,0.08)',
            borderRadius: '12px',
            background: 'rgba(255,255,255,0.02)'
          }}>
            {['warmup', 'primary_out', 'secondary_out', 'accessory', 'conditioning'].map(id => (
              <NodeHTML key={id} node={STAGES[id as keyof typeof STAGES]} />
            ))}
          </div>

        </div>

        {/* Animated dots flowing down */}
        {[...Array(3)].map((_, i) => (
          <motion.div
            key={i}
            style={{
              position: 'absolute',
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              background: '#7dd3fc',
              left: '50%',
              transform: 'translateX(-50%)',
              top: '0',
              zIndex: 2
            }}
            animate={{
              top: ['0%', '100%'],
              opacity: [0, 1, 0]
            }}
            transition={{
              duration: 3,
              delay: i * 0.5,
              repeat: Infinity,
              ease: 'linear'
            }}
          />
        ))}


      </div>
    </div>
  )
}
