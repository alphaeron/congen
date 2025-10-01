import React from 'react';
import { Box, Typography } from '@mui/material';

export interface CycleNode {
  id: string;
  label: string;
  details: string[];
  color: string;
}

export interface CycleDiagramProps {
  nodes: CycleNode[];
  title?: string;
  outerRadius?: number;
  innerRadius?: number;
  centerX?: number;
  centerY?: number;
  nodeWidth?: number;
  nodeHeight?: number;
  width?: number;
  height?: number;
}

export const CycleDiagram: React.FC<CycleDiagramProps> = ({
  nodes,
  title = 'Smart Personalization Journey',
  outerRadius = 200,
  innerRadius = 120,
  centerX = 300,
  centerY = 300,
  width = 600,
  height = 600,
}) => {
  const segmentAngle = (2 * Math.PI) / nodes.length;
  const arrowheadSize = 0.3; // radians - make arrowheads and slots more visible

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 3,
        width: '100%',
        maxWidth: '100%',
        overflow: 'visible',
      }}
    >
      {/* Title */}
      <Typography
        variant="h4"
        sx={{
          color: '#3b82f6',
          fontWeight: 700,
          textAlign: 'center',
          fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
          borderBottom: '2px solid #3b82f6',
          paddingBottom: 1,
        }}
      >
        {title}
      </Typography>

      {/* SVG Diagram */}
      <Box
        sx={{
          width: width,
          height: height,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'visible',
        }}
      >
        <svg
          width={width}
          height={height}
          viewBox={`0 0 ${width} ${height}`}
          style={{ overflow: 'visible' }}
        >
          <defs>
            <filter id="segmentShadow">
              <feDropShadow dx="2" dy="2" stdDeviation="2" floodColor="rgba(0,0,0,0.3)"/>
            </filter>
          </defs>
          
          {/* Render each segment as a single clean path */}
          {nodes.map((node, index) => {
            const startAngle = (index * segmentAngle) - Math.PI / 2;
            const endAngle = ((index + 1) * segmentAngle) - Math.PI / 2;
            
            // Calculate the main arc angles (without arrowhead/slot)
            const arcStartAngle = startAngle + arrowheadSize;
            const arcEndAngle = endAngle - arrowheadSize;
            
            // Arrowhead points (at the end of the segment)
            // Tip extends beyond the segment end, base is at the arc end
            const arrowheadTipAngle = endAngle;
            const arrowheadBaseAngle = arcEndAngle;
            
            // Slot points (at the start of the segment)  
            // Tip is at the segment start, base extends inward
            const slotTipAngle = startAngle;
            const slotBaseAngle = startAngle - arrowheadSize;
            
            // Middle radius for tips
            const middleRadius = (outerRadius + innerRadius) / 2;
            
            // Arrowhead coordinates
            const arrowheadTipX = centerX + middleRadius * Math.cos(arrowheadTipAngle);
            const arrowheadTipY = centerY + middleRadius * Math.sin(arrowheadTipAngle);
            const arrowheadBaseX = centerX + outerRadius * Math.cos(arrowheadBaseAngle);
            const arrowheadBaseY = centerY + outerRadius * Math.sin(arrowheadBaseAngle);
            const arrowheadInnerTipX = centerX + middleRadius * Math.cos(arrowheadTipAngle);
            const arrowheadInnerTipY = centerY + middleRadius * Math.sin(arrowheadTipAngle);
            const arrowheadInnerBaseX = centerX + innerRadius * Math.cos(arrowheadBaseAngle);
            const arrowheadInnerBaseY = centerY + innerRadius * Math.sin(arrowheadBaseAngle);
            
            // Slot coordinates
            const slotTipX = centerX + middleRadius * Math.cos(slotTipAngle);
            const slotTipY = centerY + middleRadius * Math.sin(slotTipAngle);
            const slotBaseX = centerX + outerRadius * Math.cos(slotBaseAngle);
            const slotBaseY = centerY + outerRadius * Math.sin(slotBaseAngle);
            const slotInnerBaseX = centerX + innerRadius * Math.cos(slotBaseAngle);
            const slotInnerBaseY = centerY + innerRadius * Math.sin(slotBaseAngle);
            
            // Single clean path: slot -> outer arc -> arrowhead -> inner arc -> slot
            // Always use sweep flag 1 for clockwise direction
            const segmentPath = `
              M ${slotBaseX} ${slotBaseY}
              A ${outerRadius} ${outerRadius} 0 0 1 ${arrowheadBaseX} ${arrowheadBaseY}
              L ${arrowheadTipX} ${arrowheadTipY}
              L ${arrowheadInnerBaseX} ${arrowheadInnerBaseY}
              A ${innerRadius} ${innerRadius} 0 0 0 ${slotInnerBaseX} ${slotInnerBaseY}
              L ${slotTipX} ${slotTipY}
              L ${slotBaseX} ${slotBaseY}
              Z
            `;

            return (
              <g key={node.id}>
                {/* Single segment path */}
                <path
                  d={segmentPath}
                  fill={node.color}
                  stroke="none"
                  filter="url(#segmentShadow)"
                />
                
                {/* Title text inside the donut */}
                <text
                  x={centerX + (outerRadius + innerRadius) / 2 * Math.cos(startAngle + segmentAngle / 2)}
                  y={centerY + (outerRadius + innerRadius) / 2 * Math.sin(startAngle + segmentAngle / 2)}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fill="white"
                  fontSize="14"
                  fontWeight="600"
                  fontFamily="Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
                >
                  {node.label}
                </text>
              </g>
            );
          })}
        </svg>
      </Box>

      {/* Detailed descriptions below */}
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: 2,
          width: '100%',
          maxWidth: '800px',
        }}
      >
        {nodes.map((node, index) => (
          <Box
            key={node.id}
            sx={{
              textAlign: 'center',
              padding: 2,
              borderRadius: 2,
              backgroundColor: 'rgba(30, 41, 59, 0.5)',
              border: `2px solid ${node.color}`,
            }}
          >
            <Typography
              variant="body2"
              sx={{
                color: node.color,
                fontWeight: 600,
                marginBottom: 0.5,
                fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
              }}
            >
              {node.label}
            </Typography>
            <Typography
              variant="caption"
              sx={{
                color: '#cbd5e1',
                fontSize: '10px',
                lineHeight: 1.4,
                fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
              }}
            >
              {node.details.join(', ')}
            </Typography>
          </Box>
        ))}
      </Box>
    </Box>
  );
};