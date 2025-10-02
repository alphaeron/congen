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
  overview: {
    id: 'overview',
    title: 'Algorithm Overview',
    subtitle: 'Conjugate powerlifting system — intelligent algorithms working together.',
    colorFrom: '#7c6bff',
    colorTo: '#60e0d8',
    shape: 'rounded-pill'
  },

  // Inputs (top)
  equipment: { id: 'equipment', title: 'Equipment', colorFrom: '#ff8a6b', colorTo: '#ffb56b', shape: 'circle' },
  one_rm: { id: 'one_rm', title: 'One Rep Maxes', colorFrom: '#6be0ff', colorTo: '#7dd3fc', shape: 'circle' },
  previous: { id: 'previous', title: 'Previous Workouts', colorFrom: '#c084fc', colorTo: '#a855f7', shape: 'circle' },

  // Preparation row
  data_prep: { id: 'data_prep', title: 'Data Preparation', colorFrom: '#60e0d8', colorTo: '#7c6bff', shape: 'rounded-rect' },
  exercise_pool: { id: 'exercise_pool', title: 'Exercise Pool Creation', colorFrom: '#ff8ab6', colorTo: '#ffb56b', shape: 'filter-hex' },

  // Generation
  set_scheme: { id: 'set_scheme', title: 'Set Scheme Generation', colorFrom: '#f59e0b', colorTo: '#ff8a6b', shape: 'diamond' },
  primary_select: { id: 'primary_select', title: 'Primary Exercise Selection', colorFrom: '#ffd36b', colorTo: '#ff7a7a', shape: 'rounded-rect' },
  secondary_select: { id: 'secondary_select', title: 'Secondary Exercise Selection', colorFrom: '#7ef27e', colorTo: '#6be0ff', shape: 'rounded-rect' },

  // Additional components (satellites)
  session_time: { id: 'session_time', title: 'Session Time Calculation', colorFrom: '#ffb86b', colorTo: '#ff7a7a', shape: 'circle' },
  matching: { id: 'matching', title: 'Exercise Matching Service', colorFrom: '#7dd3fc', colorTo: '#60e0d8', shape: 'circle' },
  weak_point: { id: 'weak_point', title: 'Weak Point Targeting', colorFrom: '#c084fc', colorTo: '#a855f7', shape: 'circle' },

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

// SVG shapes renderer
function Shape({ node, size = 86 }: { node: any; size?: number }) {
  const r = size / 2
  const { colorFrom, colorTo, shape } = node
  const gradient = `url(#${gradId(node.id)})`

  if (shape === 'circle') {
    return <circle r={r} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'rounded-pill') {
    return <rect x={-r - 30} y={-r/2} width={size + 60} height={r} rx={r/2} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'rounded-rect') {
    return <rect x={-r} y={-r} width={size} height={size * 0.75} rx={14} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'diamond') {
    return <polygon points={`${0},${-r} ${r},0 0,${r} ${-r},0`} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'filter-hex') {
    return <rect x={-r} y={-r} width={size} height={size} rx={20} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  if (shape === 'triangle') {
    return <polygon points={`0,${-r} ${r},${r} ${-r},${r}`} fill={gradient} stroke="rgba(255,255,255,0.06)" strokeWidth="1.5" />
  }
  // fallback
  return <circle r={r} fill={gradient} />
}

// Helper function to wrap text within shapes
function wrapText(text: string, maxWidth: number, fontSize: number): string[] {
  const words = text.split(' ')
  const lines: string[] = []
  let currentLine = ''
  
  for (const word of words) {
    const testLine = currentLine + (currentLine ? ' ' : '') + word
    const testWidth = testLine.length * fontSize * 0.6 // Approximate character width
    
    if (testWidth <= maxWidth) {
      currentLine = testLine
    } else {
      if (currentLine) {
        lines.push(currentLine)
        currentLine = word
      } else {
        lines.push(word) // Single word longer than maxWidth
      }
    }
  }
  
  if (currentLine) {
    lines.push(currentLine)
  }
  
  return lines
}

// Single Node (SVG group representing a stage) — exact single instance each
function NodeSVG({ node, x, y, labelBelow = true }: { node: any; x: number; y: number; labelBelow?: boolean }) {
  const maxWidth = 70 // Maximum width for text wrapping
  const fontSize = 12 // Larger font size
  const wrappedText = wrapText(node.title, maxWidth, fontSize)
  
  return (
    <g transform={`translate(${x}, ${y})`}>
      <defs>
        <linearGradient id={gradId(node.id)} x1="0" x2="1">
          <stop offset="0%" stopColor={node.colorFrom} />
          <stop offset="100%" stopColor={node.colorTo} />
        </linearGradient>
      </defs>

      <motion.g initial={{ scale: 0.88, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ duration: 0.5 }}>
        <g>
          <Shape node={node} />
        </g>
        {/* Wrapped text inside the shape */}
        {wrappedText.map((line, index) => (
          <text 
            key={index}
            x="0" 
            y={-wrappedText.length * 3 + index * 6} 
            textAnchor="middle" 
            fontSize={fontSize} 
            fontWeight="600" 
            fill="#fff"
          >
            {line}
          </text>
        ))}
      </motion.g>
    </g>
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
      
      {/* Animated data particles flowing downward */}
      {[...Array(3)].map((_, i) => (
        <motion.circle
          key={i}
          r="3"
          fill={colorFrom}
          initial={{ cx: x1, cy: y1, opacity: 0, scale: 0 }}
          animate={{ 
            cx: x2, 
            cy: y2 - 10,
            opacity: [0, 1, 0],
            scale: [0, 1, 0]
          }}
          transition={{
            delay: 1.2 + i * 0.4,
            duration: 2.5,
            repeat: Infinity,
            ease: "easeInOut"
          }}
        />
      ))}
      
      {/* Directional arrow pointing down */}
      <motion.polygon
        points={`${arrowX - arrowSize/2},${arrowY - arrowSize} ${arrowX + arrowSize/2},${arrowY - arrowSize} ${arrowX},${arrowY}`}
        fill={colorTo}
        initial={{ opacity: 0, scale: 0 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 1.2, duration: 0.4 }}
      />
      
      {/* Data type label */}
      {dataType && (
        <motion.text
          x={mx}
          y={my - 20}
          textAnchor="middle"
          fontSize="14"
          fill={colorTo}
          fontWeight="600"
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

  // Reorganized positions with proper centering
  const pos = {
    overview: [640, 50],

    // INPUT PHASE - centered within container
    equipment: [480, 150],
    one_rm: [640, 150],
    previous: [800, 150],

    // PROCESSING PHASE - centered within container
    data_prep: [480, 300],
    exercise_pool: [640, 300],
    set_scheme: [800, 300],

    // GENERATION PHASE - centered within container
    primary_select: [480, 450],
    secondary_select: [640, 450],
    session_time: [800, 450],
    matching: [480, 520],      // Moved inside Generation container
    weak_point: [640, 520],     // Moved inside Generation container

    // OUTPUT PHASE - centered within container
    warmup: [320, 750],
    primary_out: [480, 750],
    secondary_out: [640, 750],
    accessory: [800, 750],
    conditioning: [960, 750]
  }

  // Group-to-group connectors for clean flow
  const groupConnectors = [
    // Overview to Input Phase
    { from: 'overview', to: 'input_phase', dataType: 'User Data Collection' },
    
    // Input Phase to Processing Phase
    { from: 'input_phase', to: 'processing_phase', dataType: 'Raw Data' },
    
    // Processing Phase to Generation Phase
    { from: 'processing_phase', to: 'generation_phase', dataType: 'Processed Data' },
    
    // Generation Phase to Output Phase
    { from: 'generation_phase', to: 'output_phase', dataType: 'Workout Components' }
  ]

  // Group center positions for clean connections - properly centered
  const groupCenters = {
    overview: [640, 50],
    input_phase: [640, 150],
    processing_phase: [640, 300],
    generation_phase: [640, 450],
    output_phase: [640, 750]
  }

  // order for right-side cards (roughly top-to-bottom order)
  const rightOrder = [
    'equipment',
    'one_rm',
    'previous',
    'data_prep',
    'exercise_pool',
    'set_scheme',
    'primary_select',
    'secondary_select',
    'session_time',
    'matching',
    'weak_point',
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
      padding: 28,
      color: COLORS.text,
      fontFamily: 'Inter, Roboto, system-ui, Arial'
    }}>
      <div style={{
        maxWidth: 1400,
        margin: '0 auto',
        borderRadius: 18,
        padding: 18,
        background: 'linear-gradient(180deg, rgba(255,255,255,0.02), rgba(255,255,255,0.01))',
        border: '1px solid rgba(255,255,255,0.04)',
        position: 'relative',
        overflow: 'visible'
      }}>
        {/* svg canvas */}
        <svg viewBox="0 0 1280 900" width="100%" height="900" style={{ display: 'block' }}>
          <defs>
            {/* gradients for nodes and connectors */}
            {Object.values(STAGES).map(s => (
              <linearGradient id={gradId(s.id)} key={s.id} x1="0" x2="1">
                <stop offset="0%" stopColor={s.colorFrom} />
                <stop offset="100%" stopColor={s.colorTo} />
              </linearGradient>
            ))}
          </defs>

          {/* Clean group-to-group connectors */}
          <g>
            {groupConnectors.map((connector, i) => {
              const { from, to, dataType } = connector
              const [x1, y1] = groupCenters[from as keyof typeof groupCenters]
              const [x2, y2] = groupCenters[to as keyof typeof groupCenters]
              
              // Simple vertical flow - no offsets needed for group centers
              return (
                <ConnectorPath
                  key={`${from}-${to}-${i}`}
                  x1={x1}
                  y1={y1 + 20}
                  x2={x2}
                  y2={y2 - 20}
                  index={i}
                  colorFrom="#7dd3fc"
                  colorTo="#ff8a6b"
                  dataType={dataType}
                />
              )
            })}
          </g>

          {/* Phase containers - properly centered */}
          <g>
            {/* Input Phase Container - centered */}
            <rect x={320} y={100} width={640} height={120} rx={12} fill="rgba(255,255,255,0.02)" stroke="rgba(255,255,255,0.08)" strokeWidth="1" strokeDasharray="4,4" />
            
            {/* Processing Phase Container - centered */}
            <rect x={320} y={250} width={640} height={120} rx={12} fill="rgba(255,255,255,0.02)" stroke="rgba(255,255,255,0.08)" strokeWidth="1" strokeDasharray="4,4" />
            
            {/* Generation Phase Container - centered and expanded */}
            <rect x={320} y={400} width={640} height={200} rx={12} fill="rgba(255,255,255,0.02)" stroke="rgba(255,255,255,0.08)" strokeWidth="1" strokeDasharray="4,4" />
            
            {/* Output Phase Container - centered */}
            <rect x={160} y={700} width={960} height={120} rx={12} fill="rgba(255,255,255,0.02)" stroke="rgba(255,255,255,0.08)" strokeWidth="1" strokeDasharray="4,4" />
          </g>

          {/* nodes */}
          <g>
            {Object.keys(STAGES).map(k => {
              const node = STAGES[k as keyof typeof STAGES]
              const [x, y] = pos[k as keyof typeof pos]
              // overview pill is wider; pass labelBelow = k !== 'overview'
              return <NodeSVG key={k} node={node} x={x} y={y} labelBelow={k !== 'overview'} />
            })}
          </g>

          {/* subtle decorative wave lines (top area) */}
          <g opacity="0.06">
            <path d="M 40 28 C 200 10, 400 50, 640 30 C 880 10, 1100 60, 1240 40" stroke="#ffffff" strokeWidth="1.2" fill="none" strokeLinecap="round" />
          </g>
        </svg>

        {/* right side numbered cards — structured like the reference vertical list */}
        <div style={{ position: 'absolute', right: 28, top: 28 }}>
          {rightOrder.map((id, idx) => {
            const node = STAGES[id as keyof typeof STAGES]
            // vertical stacking with spacing
            const top = 20 + idx * 86
            return <RightCard key={id} node={node} number={idx + 1} top={top} />
          })}
        </div>

      </div>
    </div>
  )
}
