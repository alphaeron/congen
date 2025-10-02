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
const gradId = (id) => `g-${id}`

// SVG shapes renderer
function Shape({ node, size = 86 }) {
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

// Single Node (SVG group representing a stage) — exact single instance each
function NodeSVG({ node, x, y, labelBelow = true }) {
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
        {/* center icon/text (simple first-letter or emoji fallback) */}
        <text x="0" y="6" textAnchor="middle" fontSize="18" fontWeight="700" fill="#fff">
          {node.title.split(' ').map(w => w[0]).slice(0,2).join('')}
        </text>
        {labelBelow && (
          <text x="0" y={60} textAnchor="middle" fontSize="12" fill={COLORS.text} fontWeight="600">
            {node.title}
          </text>
        )}
      </motion.g>
    </g>
  )
}

// curved connector path generator (smooth cubic)
function ConnectorPath({ x1, y1, x2, y2, index = 0, colorFrom = '#7dd3fc', colorTo = '#ff8a6b' }) {
  const dx = Math.abs(x2 - x1)
  const mx = (x1 + x2) / 2
  const my = (y1 + y2) / 2
  const c1x = x1 + (dx * 0.25)
  const c1y = y1 + (index % 2 === 0 ? -18 : 18)
  const c2x = x2 - (dx * 0.25)
  const c2y = y2 + (index % 2 === 0 ? -18 : 18)
  const d = `M ${x1} ${y1} C ${c1x} ${c1y} ${c2x} ${c2y} ${x2} ${y2}`

  return (
    <>
      <defs>
        <linearGradient id={`conn-${index}`} x1="0" x2="1">
          <stop offset="0%" stopColor={colorFrom} stopOpacity="0.95" />
          <stop offset="100%" stopColor={colorTo} stopOpacity="0.95" />
        </linearGradient>
      </defs>
      <motion.path
        d={d}
        stroke={`url(#conn-${index})`}
        strokeWidth={3}
        fill="none"
        strokeLinecap="round"
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={{ duration: 0.9, delay: 0.06 * index }}
      />
    </>
  )
}

// Right side numbered step card (process style)
function RightCard({ node, number, top }) {
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

  // map exact positions for each stage (no duplicates)
  const pos = {
    overview: [420, 36],

    equipment: [leftX, 120],
    one_rm: [midX, 120],
    previous: [rightX, 120],

    data_prep: [leftX + 60, 240],
    exercise_pool: [midX + 30, 260],
    set_scheme: [rightX - 20, 250],

    primary_select: [leftX + 20, 370],
    secondary_select: [midX + 20, 380],

    session_time: [rightX - 30, 360],
    matching: [midX + 120, 490],
    weak_point: [rightX + 60, 500],

    warmup: [140, 680],
    primary_out: [360, 680],
    secondary_out: [580, 680],
    accessory: [800, 680],
    conditioning: [1020, 680]
  }

  // connectors (ordered to form continuous flowing motion)
  const connectors = [
    // overview -> inputs
    ['overview', 'equipment'],
    ['overview', 'one_rm'],
    ['overview', 'previous'],

    // inputs -> data prep/exercise pool
    ['equipment', 'data_prep'],
    ['one_rm', 'exercise_pool'],
    ['previous', 'set_scheme'],

    // consolidation
    ['data_prep', 'exercise_pool'],
    ['exercise_pool', 'set_scheme'],

    // generation -> selection
    ['set_scheme', 'primary_select'],
    ['set_scheme', 'secondary_select'],

    // generation -> additional components
    ['primary_select', 'session_time'],
    ['secondary_select', 'matching'],
    ['session_time', 'weak_point'],
    ['matching', 'weak_point'],

    // final outputs (branch flows from selections & weak_point to outputs)
    ['primary_select', 'primary_out'],
    ['secondary_select', 'secondary_out'],
    ['weak_point', 'accessory'],
    ['session_time', 'warmup'],
    ['weak_point', 'conditioning']
  ]

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
        <svg viewBox="0 0 1280 820" width="100%" height="820" style={{ display: 'block' }}>
          <defs>
            {/* gradients for nodes and connectors */}
            {Object.values(STAGES).map(s => (
              <linearGradient id={gradId(s.id)} key={s.id} x1="0" x2="1">
                <stop offset="0%" stopColor={s.colorFrom} />
                <stop offset="100%" stopColor={s.colorTo} />
              </linearGradient>
            ))}
          </defs>

          {/* connectors (under nodes) */}
          <g>
            {connectors.map((c, i) => {
              const [a, b] = c
              const [x1, y1] = pos[a]
              const [x2, y2] = pos[b]
              // small offset so connectors attach outside shapes more cleanly
              const offA = (x2 > x1) ? 60 : -60
              const offB = (x2 > x1) ? -60 : 60
              return (
                <ConnectorPath
                  key={`${a}-${b}-${i}`}
                  x1={x1 + offA}
                  y1={y1 + 18}
                  x2={x2 + offB}
                  y2={y2 - 6}
                  index={i}
                  colorFrom={STAGES[a].colorFrom}
                  colorTo={STAGES[b].colorTo}
                />
              )
            })}
          </g>

          {/* nodes */}
          <g>
            {Object.keys(STAGES).map(k => {
              const node = STAGES[k]
              const [x, y] = pos[k]
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
            const node = STAGES[id]
            // vertical stacking with spacing
            const top = 20 + idx * 86
            return <RightCard key={id} node={node} number={idx + 1} top={top} />
          })}
        </div>

        {/* small footer legend */}
        <div style={{ position: 'absolute', left: 18, bottom: 14, color: '#cbd3ea', fontSize: 13 }}>
          <strong style={{ display: 'block', marginBottom: 6 }}>Flow legend</strong>
          <div style={{ display: 'flex', gap: 12 }}>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <div style={{ width: 12, height: 12, borderRadius: 3, background: '#ffffff11' }} /> Inputs
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <div style={{ width: 12, height: 12, borderRadius: 3, background: '#ffffff11' }} /> Processing
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <div style={{ width: 12, height: 12, borderRadius: 3, background: '#ffffff11' }} /> Output
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
