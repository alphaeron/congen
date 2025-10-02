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
  outerRadius = 280,
  innerRadius = 230, // (280 + 180) / 2 = 230
  centerX = 300,
  centerY = 300,
  width = 600,
  height = 600,
}) => {
  const segmentAngle = (2 * Math.PI) / nodes.length;
  const arrowheadSize = 0.3; // radians - make arrowheads and slots more visible
  const gapSize = 0.033; // radians - small gap between arrowheads and slots (2/3 of 0.05)
  
  // Calculate middle radius first
  const middleRadius = (outerRadius + innerRadius) / 2;
  
  // Calculate the actual linear gap distance to maintain consistent spacing
  const linearGapSize = gapSize * outerRadius; // Convert angular gap to linear distance
  const angularGapOuter = linearGapSize / outerRadius; // Angular gap for outer edge
  const angularGapInner = linearGapSize / innerRadius; // Angular gap for inner edge
  const angularGapTip = linearGapSize / middleRadius; // Angular gap for tip (middle radius)

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
      <Box sx={{ textAlign: 'center' }}>
        <Typography
          variant="h4"
          sx={{
            color: '#3b82f6',
            fontWeight: 700,
            fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
            display: 'inline-block',
            position: 'relative',
          }}
        >
          {title}
        </Typography>
        {/* Properly centered underline */}
        <Box
          sx={{
            width: '100%',
            height: '2px',
            backgroundColor: '#3b82f6',
            marginTop: 1,
          }}
        />
      </Box>

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
          
          {/* Render each segment as a single clean path - in correct order */}
          {nodes.map((node, index) => {
            // Each segment gets equal angle space, accounting for arrowhead/slot space
            const segmentAngleWithSpace = (2 * Math.PI) / nodes.length;
            const startAngle = (index * segmentAngleWithSpace) - Math.PI / 2;
            const endAngle = ((index + 1) * segmentAngleWithSpace) - Math.PI / 2;
            
            // Calculate the main arc angles (without arrowhead/slot/gap)
            // Each segment uses half the arrowhead size for slot and half for arrowhead, plus gap
            const arcStartAngle = startAngle + arrowheadSize / 2 + angularGapOuter;
            const arcEndAngle = endAngle - arrowheadSize / 2 - angularGapOuter;
            
            // Arrowhead points (at the end of the segment)
            // Tip stays within the outer radius, base is at the arc end (convex, pointing outward)
            const arrowheadTipAngle = endAngle - angularGapTip;
            const arrowheadBaseAngle = endAngle - angularGapOuter - arrowheadSize / 2;
            
            // Slot points (at the start of the segment)  
            // Tip is at the segment start, base extends inward (concave)
            const slotTipAngle = startAngle + angularGapTip;
            const slotBaseAngle = startAngle + angularGapOuter - arrowheadSize / 2;
            
            // Middle radius is already calculated above
            
            // Calculate inner edge angles with proper gap sizing
            const innerArcStartAngle = startAngle + arrowheadSize / 2 + angularGapInner;
            const innerArcEndAngle = endAngle - arrowheadSize / 2 - angularGapInner;
            const innerArrowheadTipAngle = endAngle - angularGapTip;
            const innerArrowheadBaseAngle = endAngle - angularGapInner - arrowheadSize / 2;
            const innerSlotTipAngle = startAngle + angularGapTip;
            const innerSlotBaseAngle = startAngle + angularGapInner - arrowheadSize / 2;
            
            // Arrowhead coordinates - tip at middle radius
            const arrowheadTipX = centerX + middleRadius * Math.cos(arrowheadTipAngle);
            const arrowheadTipY = centerY + middleRadius * Math.sin(arrowheadTipAngle);
            const arrowheadBaseX = centerX + outerRadius * Math.cos(arrowheadBaseAngle);
            const arrowheadBaseY = centerY + outerRadius * Math.sin(arrowheadBaseAngle);
            const arrowheadInnerTipX = centerX + middleRadius * Math.cos(innerArrowheadTipAngle);
            const arrowheadInnerTipY = centerY + middleRadius * Math.sin(innerArrowheadTipAngle);
            const arrowheadInnerBaseX = centerX + innerRadius * Math.cos(innerArrowheadBaseAngle);
            const arrowheadInnerBaseY = centerY + innerRadius * Math.sin(innerArrowheadBaseAngle);
            
            // Slot coordinates
            const slotTipX = centerX + middleRadius * Math.cos(slotTipAngle);
            const slotTipY = centerY + middleRadius * Math.sin(slotTipAngle);
            const slotBaseX = centerX + outerRadius * Math.cos(slotBaseAngle);
            const slotBaseY = centerY + outerRadius * Math.sin(slotBaseAngle);
            const slotInnerBaseX = centerX + innerRadius * Math.cos(innerSlotBaseAngle);
            const slotInnerBaseY = centerY + innerRadius * Math.sin(innerSlotBaseAngle);
            
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
                
                {/* Calculate text position with intelligent wrapping and consistent gaps */}
                {(() => {
                  // Calculate the angle for this segment's center
                  const segmentCenterAngle = startAngle + segmentAngle / 2;
                  
                  // Intelligent text wrapping based on available arc space
                  const maxArcWidth = segmentAngle * innerRadius * 0.8; // 80% of arc width
                  const charWidth = 8; // Approximate character width
                  const maxCharsPerLine = Math.floor(maxArcWidth / charWidth);
                  
                  // Split text into lines
                  const words = node.label.split(' ');
                  const lines = [];
                  let currentLine = '';
                  
                  for (const word of words) {
                    if ((currentLine + ' ' + word).length <= maxCharsPerLine) {
                      currentLine = currentLine ? currentLine + ' ' + word : word;
                    } else {
                      if (currentLine) lines.push(currentLine);
                      currentLine = word;
                    }
                  }
                  if (currentLine) lines.push(currentLine);
                  
                  // Calculate text dimensions for wrapped text
                  const lineHeight = 16;
                  const textHeight = lines.length * lineHeight;
                  const maxLineWidth = Math.max(...lines.map(line => line.length * charWidth));
                  
                  // Calculate text bounding box corners relative to center
                  const halfWidth = maxLineWidth / 2;
                  const halfHeight = textHeight / 2;
                  
                  // Calculate the four corners of the text bounding box
                  const corners = [
                    { x: -halfWidth, y: -halfHeight }, // Top-left
                    { x: halfWidth, y: -halfHeight },  // Top-right
                    { x: halfWidth, y: halfHeight },   // Bottom-right
                    { x: -halfWidth, y: halfHeight }   // Bottom-left
                  ];
                  
                  // Find the corner closest to the center (origin)
                  let closestCorner = corners[0];
                  let minDistance = Math.sqrt(corners[0].x ** 2 + corners[0].y ** 2);
                  
                  for (const corner of corners) {
                    const distance = Math.sqrt(corner.x ** 2 + corner.y ** 2);
                    if (distance < minDistance) {
                      minDistance = distance;
                      closestCorner = corner;
                    }
                  }
                  
                  // Calculate the desired radius so the closest corner is consistent gap distance from inner radius
                  const consistentGapDistance = 15; // pixels - consistent for all segments
                  const desiredRadius = innerRadius - consistentGapDistance - minDistance;
                  
                  // Calculate final text position
                  const textX = centerX + desiredRadius * Math.cos(segmentCenterAngle);
                  const textY = centerY + desiredRadius * Math.sin(segmentCenterAngle);
                  
                  return (
                    <React.Fragment key={`text-${node.id}`}>
                      {/* Wrapped text positioned based on bounding box calculation */}
                      {lines.map((line, lineIndex) => (
                        <text
                          key={`line-${lineIndex}`}
                          x={textX}
                          y={textY + (lineIndex - (lines.length - 1) / 2) * lineHeight}
                          textAnchor="middle"
                          dominantBaseline="middle"
                          fill="white"
                          fontSize="14"
                          fontWeight="600"
                          fontFamily="Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
                        >
                          {line}
                        </text>
                      ))}
                      
                      {/* Colored line under the text - positioned relative to text */}
                      <line
                        x1={textX - maxLineWidth / 2}
                        y1={textY + halfHeight + 5}
                        x2={textX + maxLineWidth / 2}
                        y2={textY + halfHeight + 5}
                        stroke={node.color}
                        strokeWidth="2"
                      />
                    </React.Fragment>
                  );
                })()}
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