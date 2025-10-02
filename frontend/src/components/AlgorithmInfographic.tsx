import React from 'react'
import { motion } from 'framer-motion'

// WorkoutAlgorithmInfographic.jsx
// Single-file React component designed to drop into a single-page React app.
// - Uses Tailwind utility classes for layout and spacing (no imports required here)
// - Uses inline SVG for crisp scalable output (easy to convert to PNG/WebP later)
// - Framer Motion is used for subtle entrance & hover animations (assumed available)
// - All algorithm stages are present exactly once (no duplicates)

// USAGE
// <WorkoutAlgorithmInfographic className="w-full h-[820px]" />

const WIDTH = 1200
const HEIGHT = 820

const nodes = [
  // Input Stage (Top Level)
  { id: 'equipment',       label: 'Equipment',       group: 'input',  x: 120,  y: 140, icon: 'dumbbell' },
  { id: 'one_rm',          label: 'One Rep Maxes',   group: 'input',  x: 540,  y: 120, icon: 'one' },
  { id: 'previous',        label: 'Previous Workouts',group: 'input', x: 960,  y: 140, icon: 'list' },

  // Exercise Pool Creation with Algorithm Details
  { id: 'equipment_filter', label: 'Equipment Filtering', group: 'prep', x: 180,  y: 320, icon: 'filter' },
  { id: 'user_prefs',       label: 'User Preferences', group: 'prep', x: 360,  y: 320, icon: 'prefs' },
  { id: 'sliding_window',   label: 'Sliding Window Logic', group: 'prep', x: 540,  y: 320, icon: 'window' },
  { id: 'exercise_pool',    label: 'Exercise Pool Creation', group: 'prep', x: 720,  y: 320, icon: 'pool' },

  // Set Scheme Generation with Details
  { id: 'prilepin',        label: 'Prilepin Guidelines', group: 'generation', x: 180, y: 480, icon: 'chart' },
  { id: 'weight_selection', label: 'Weight Selection', group: 'generation', x: 360, y: 480, icon: 'weight' },
  { id: 'movement_balance', label: 'Movement Balance', group: 'generation', x: 540, y: 480, icon: 'balance' },
  { id: 'exercise_rotation', label: 'Exercise Rotation', group: 'generation', x: 720, y: 480, icon: 'rotate' },

  // Workout Stage Generation
  { id: 'primary',         label: 'Primary Exercise Selection',   group: 'generation', x: 220, y: 520, icon: 'barbell' },
  { id: 'secondary',       label: 'Secondary Exercise Selection', group: 'generation', x: 540, y: 540, icon: 'support' },

  // Additional Algorithm Components
  { id: 'session_time',    label: 'Session Time Calculation',    group: 'additional', x: 860, y: 520, icon: 'stopwatch' },
  { id: 'matching',        label: 'Exercise Matching Service',    group: 'additional', x: 420, y: 660, icon: 'match' },
  { id: 'weak_point',      label: 'Weak Point Targeting',         group: 'additional', x: 740, y: 660, icon: 'target' },

  // Output Stages
  { id: 'warmup',          label: 'Warmup Stage',    group: 'output', x: 140, y: 760, icon: 'triangle' },
  { id: 'primary_out',     label: 'Primary Stage',   group: 'output', x: 380, y: 760, icon: 'triangle' },
  { id: 'secondary_out',   label: 'Secondary Stage', group: 'output', x: 620, y: 760, icon: 'triangle' },
  { id: 'accessory',       label: 'Accessory Stage', group: 'output', x: 860, y: 760, icon: 'triangle' },
  { id: 'conditioning',    label: 'Conditioning Stage', group: 'output', x: 1100, y: 760, icon: 'triangle' }
]

// Connections define the flow. They are intentionally single-instance and form continuous connected lines.
const links = [
  // Input to Exercise Pool Creation
  ['equipment','equipment_filter'],
  ['one_rm','user_prefs'],
  ['previous','sliding_window'],

  // Exercise Pool Creation flow
  ['equipment_filter','exercise_pool'],
  ['user_prefs','exercise_pool'],
  ['sliding_window','exercise_pool'],

  // Exercise Pool to Set Scheme Generation
  ['exercise_pool','prilepin'],
  ['exercise_pool','weight_selection'],
  ['exercise_pool','movement_balance'],
  ['exercise_pool','exercise_rotation'],

  // Set Scheme Generation to Workout Stages
  ['prilepin','primary'],
  ['weight_selection','primary'],
  ['movement_balance','primary'],
  ['exercise_rotation','primary'],

  ['primary','secondary'],
  ['secondary','session_time'],

  ['session_time','matching'],
  ['matching','weak_point'],
  ['weak_point','accessory'],

  ['primary','primary_out'],
  ['secondary','secondary_out'],
  ['accessory','accessory'],
  ['accessory','conditioning'],
  ['warmup','primary_out']
]

const GROUP_COLORS: Record<string, { from: string; to: string }> = {
  input:   { from: '#ff7a7a', to: '#ffb56b' },
  prep:    { from: '#7c6bff', to: '#60e0d8' },
  generation: { from: '#ff8ab6', to: '#7dd3fc' },
  additional: { from: '#ffd36b', to: '#a78bfa' },
  output:  { from: '#7ef27e', to: '#6be0ff' }
}

function gradientId(id: string): string { return `g-${id}` }

function Icon({type, size=36}: {type: string, size?: number}){
  // Minimal inline icons — shapes are simple so they scale cleanly.
  switch(type){
    case 'dumbbell': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="1" y="12" width="34" height="12" rx="2" opacity="0" />
        <path d="M6 18h4M30 18h4M14 14v8M22 14v8" />
      </g>
    )
    case 'one': return (
      <text x="0" y="24" fontSize="26" fontWeight="700" fill="white">1</text>
    )
    case 'list': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="1.8">
        <rect x="4" y="6" width="28" height="24" rx="3" />
        <path d="M10 14h14M10 20h10" />
      </g>
    )
    case 'db': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="1.8">
        <ellipse cx="18" cy="12" rx="12" ry="4" />
        <path d="M6 12v12c0 2 5 4 12 4s12-2 12-4V12" />
      </g>
    )
    case 'filter': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <path d="M6 8h24M10 18h14M14 28h6" strokeLinecap="round" />
      </g>
    )
    case 'prefs': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <circle cx="18" cy="18" r="8" />
        <path d="M18 10v-2M18 26v-2M10 18h-2M26 18h-2" />
      </g>
    )
    case 'window': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <rect x="6" y="8" width="24" height="20" rx="2" />
        <path d="M10 14h8M10 20h12" />
      </g>
    )
    case 'pool': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <circle cx="18" cy="18" r="10" />
        <path d="M8 18h20M18 8v20" />
      </g>
    )
    case 'chart': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <rect x="6" y="20" width="4" height="8" />
        <rect x="12" y="16" width="4" height="12" />
        <rect x="18" y="12" width="4" height="16" />
        <rect x="24" y="8" width="4" height="20" />
      </g>
    )
    case 'weight': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <path d="M4 18h28" />
        <rect x="2" y="14" width="6" height="8" rx="1" />
        <rect x="30" y="14" width="6" height="8" rx="1" />
        <circle cx="18" cy="18" r="3" />
      </g>
    )
    case 'balance': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <path d="M18 6v24M6 18h24" />
        <circle cx="18" cy="18" r="4" />
        <path d="M10 10l8 8M26 10l-8 8" />
      </g>
    )
    case 'rotate': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <path d="M18 6v6l4-4M18 30v-6l4 4" />
        <circle cx="18" cy="18" r="8" />
      </g>
    )
    case 'scheme': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <rect x="6" y="8" width="24" height="20" rx="3" />
        <path d="M12 14h8M12 20h12" />
      </g>
    )
    case 'barbell': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
        <path d="M4 18h28" />
        <rect x="2" y="14" width="6" height="8" rx="1" />
        <rect x="30" y="14" width="6" height="8" rx="1" />
      </g>
    )
    case 'support': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <circle cx="18" cy="14" r="6" />
        <path d="M18 20v6" />
      </g>
    )
    case 'stopwatch': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <circle cx="18" cy="20" r="8" />
        <path d="M18 12v-3" />
        <path d="M22 6l-2 2" />
      </g>
    )
    case 'match': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="2">
        <rect x="6" y="8" width="12" height="16" rx="2" />
        <circle cx="26" cy="18" r="6" />
      </g>
    )
    case 'target': return (
      <g transform={`scale(${size/36})`} fill="none" stroke="white" strokeWidth="1.8">
        <circle cx="18" cy="18" r="8" />
        <circle cx="18" cy="18" r="4" />
        <path d="M18 10v-4" />
      </g>
    )
    case 'triangle': return (
      <polygon points="18,6 30,30 6,30" fill="white" opacity="0.95" />
    )
    default: return null
  }
}

function wavyPath(a: {x: number, y: number}, b: {x: number, y: number}): string {
  // cubic bezier curve between points producing a smooth wave-like connector
  const dx = Math.abs(b.x - a.x)
  const mx = (a.x + b.x)/2
  const my = (a.y + b.y)/2
  const dy = Math.max(40, Math.min(150, (b.y - a.y)))
  const c1 = { x: a.x + dx*0.25, y: a.y }
  const c2 = { x: b.x - dx*0.25, y: b.y }
  return `M ${a.x} ${a.y} C ${c1.x} ${c1.y} ${c2.x} ${c2.y} ${b.x} ${b.y}`
}

export default function WorkoutAlgorithmInfographic({ className='' }: { className?: string }){
  // compute id->node map
  const map = Object.fromEntries(nodes.map(n=>[n.id,n]))

  return (
    <div className={`bg-gradient-to-b from-[#0f0b1a] to-[#120d22] rounded-2xl p-6 ${className}`}>
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" height="100%" preserveAspectRatio="xMidYMid meet" aria-hidden>
        <defs>
          {nodes.map(n=>{
            const col = GROUP_COLORS[n.group] || GROUP_COLORS.generation
            return (
              <linearGradient key={n.id} id={gradientId(n.id)} x1="0" x2="1" y1="0" y2="1">
                <stop offset="0%" stopColor={col.from} />
                <stop offset="100%" stopColor={col.to} />
              </linearGradient>
            )
          })}

          <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="8" result="coloredBlur" />
            <feMerge>
              <feMergeNode in="coloredBlur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        {/* Wavy connectors - draw first so nodes sit on top */}
        <g strokeWidth={6} strokeLinecap="round" strokeLinejoin="round" opacity={0.9} filter="url(#glow)">
          {links.map((pair, i)=>{
            const a = map[pair[0]]
            const b = map[pair[1]]
            if(!a || !b) return null
            // stroke color derived from source group
            const col = GROUP_COLORS[a.group] || GROUP_COLORS.generation
            return (
              <path key={i} d={wavyPath({x:a.x+60,y:a.y},{x:b.x-60,y:b.y})} stroke={`url(#${gradientId(a.id)})`} fill="none" />
            )
          })}
        </g>

        {/* Nodes */}
        {nodes.map(n=>{
          const g = GROUP_COLORS[n.group]
          const radius = n.group === 'output' ? 46 : 54
          return (
            <g key={n.id} transform={`translate(${n.x},${n.y})`}>
              {/* shadow ring */}
              <circle r={radius+8} fill="rgba(10,10,14,0.28)" />
              {/* gradient fill */}
              <motion.circle
                initial={{ r: 0, opacity: 0 }}
                animate={{ r: radius, opacity: 1 }}
                transition={{ duration: 0.6, delay: 0.02*nodes.indexOf(n) }}
                r={radius}
                fill={`url(#${gradientId(n.id)})`}
                stroke="rgba(255,255,255,0.06)"
                strokeWidth={1}
                style={{ mixBlendMode: 'screen' }}
              />

              {/* inner icon plate */}
              <motion.g whileHover={{ scale: 1.06 }} style={{ transformOrigin: 'center' }}>
                <g transform="translate(-28,-28)">
                  <rect x="0" y="0" width="56" height="56" rx="14" fill="rgba(0,0,0,0.12)" />
                  <g transform="translate(10,6)">{/* place icon */}
                    <Icon type={n.icon} size={36} />
                  </g>
                </g>
              </motion.g>

              {/* label */}
              <text x="0" y={radius+28} textAnchor="middle" style={{ fontFamily: 'Inter, system-ui, -apple-system, Segoe UI, Roboto, "Helvetica Neue", Arial', fontWeight:600 }} fill="#f7f3ea" fontSize={16}>{n.label}</text>
            </g>
          )
        })}

        {/* Bottom legend — tidy, single instance icons for outputs */}
      </svg>

      <div className="mt-3 text-sm text-[#d6cbdc]" style={{ maxWidth: 1100 }}>
        {/* Small descriptive line — keep it short so the canvas remains focused on the visual */}
        <p className="opacity-80">Conjugate powerlifting workout generator — single-instance stages shown, connected by continuous flow lines. Designed for responsive integration in a single-page React app.</p>
      </div>
    </div>
  )
}
