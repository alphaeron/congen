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
  outerRadius = 310,
  innerRadius = 255,
  centerX = 300,
  centerY = 300,
  width = 600,
  height = 600,
}) => {
  const segmentAngle = (2 * Math.PI) / nodes.length;
  
  // Dynamic sizing based on component dimensions and segment count
  const scaleFactor = Math.min(width, height) / 600; // Base scale on 600px
  const segmentCountFactor = Math.sqrt(6 / nodes.length); // Adjust for segment count
  
  // Dynamic arrowhead size based on segment count and scale
  const arrowheadSize = (0.3 * segmentCountFactor) * scaleFactor;
  
  // Dynamic gap size based on scale and segment count
  const gapSize = (0.033 * segmentCountFactor) * scaleFactor;
  
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
            const innerArrowheadBaseAngle = endAngle - angularGapInner - arrowheadSize / 2;
            const innerSlotBaseAngle = startAngle + angularGapInner - arrowheadSize / 2;
            
            // Arrowhead coordinates - tip at middle radius
            const arrowheadTipX = centerX + middleRadius * Math.cos(arrowheadTipAngle);
            const arrowheadTipY = centerY + middleRadius * Math.sin(arrowheadTipAngle);
            const arrowheadBaseX = centerX + outerRadius * Math.cos(arrowheadBaseAngle);
            const arrowheadBaseY = centerY + outerRadius * Math.sin(arrowheadBaseAngle);
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
                
                {/* Calculate text position with intelligent wrapping, consistent gaps, and collision detection */}
                {(() => {
                  // Calculate the angle for this segment's center
                  const segmentCenterAngle = startAngle + segmentAngle / 2;
                  
                  // Calculate available space considering adjacent segments
                  const currentIndex = nodes.findIndex(n => n.id === node.id);
                  const prevIndex = (currentIndex - 1 + nodes.length) % nodes.length;
                  const nextIndex = (currentIndex + 1) % nodes.length;
                  
                  // Calculate angles for adjacent segments
                  const prevSegmentAngle = (prevIndex * segmentAngle) + segmentAngle / 2;
                  const nextSegmentAngle = (nextIndex * segmentAngle) + segmentAngle / 2;
                  
                  // Calculate the actual available arc width considering adjacent text
                  const segmentStartAngle = startAngle;
                  const segmentEndAngle = startAngle + segmentAngle;
                  
                  // Find the closest point to adjacent segments to determine safe text width
                  const prevAngleDiff = Math.abs(segmentStartAngle - prevSegmentAngle);
                  const nextAngleDiff = Math.abs(nextSegmentAngle - segmentEndAngle);
                  const minAngleDiff = Math.min(prevAngleDiff, nextAngleDiff);
                  
                  // Dynamic text sizing based on scale and segment count
                  const fontSize = Math.max(10, 14 * scaleFactor * segmentCountFactor);
                  const charWidth = fontSize * 0.6; // Approximate character width based on font size
                  
                  // Use a conservative estimate of available space (50% of segment or distance to adjacent)
                  const availableAngle = Math.min(segmentAngle * 0.5, minAngleDiff * 0.8);
                  const maxArcWidth = availableAngle * innerRadius;
                  
                  const maxCharsPerLine = Math.floor(maxArcWidth / charWidth);
                  
                  // Split text into lines with more aggressive wrapping
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
                  const lineHeight = fontSize * 1.2; // Dynamic line height based on font size
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
                  
                  // Calculate intelligent gap distance based on segment angle and text box orientation
                  const baseGapDistance = 15 * scaleFactor; // Dynamic base gap based on scale
                  
                  // Use the already calculated segment center angle
                  
                  // Convert to degrees for easier quadrant calculation
                  const angleDegrees = (segmentCenterAngle * 180 / Math.PI + 360) % 360;
                  
                  // Calculate dynamic gap adjustment based on angle using a mathematical formula
                  // This formula works for any number of segments and adjusts positioning intelligently
                  
                  // Convert angle to radians for trigonometric calculations
                  const angleRadians = segmentCenterAngle;
                  
                  // Create a smooth adjustment curve based on the segment's position
                  // The formula uses sine and cosine to create natural positioning adjustments
                  // that work well for text boxes around a circle
                  
                  // Primary adjustment: based on vertical position (sine component)
                  // Segments at top (90°) and bottom (270°) get different treatments
                  const verticalAdjustment = Math.sin(angleRadians) * (8 * scaleFactor);
                  
                  // Secondary adjustment: based on horizontal position (cosine component)  
                  // Segments at left (-1) and right (1) get different treatments
                  const horizontalAdjustment = Math.cos(angleRadians) * (5 * scaleFactor);
                  
                  // Combine adjustments for natural positioning
                  const gapAdjustment = Math.round(verticalAdjustment + horizontalAdjustment);
                  
                  const gapDistance = baseGapDistance + gapAdjustment;
                  const desiredRadius = innerRadius - gapDistance - minDistance;
                  
                  // Calculate final text position
                  const textX = centerX + desiredRadius * Math.cos(segmentCenterAngle);
                  const textY = centerY + desiredRadius * Math.sin(segmentCenterAngle);
                  
                  return (
                    <React.Fragment key={`text-${node.id}`}>
                      {/* Title text positioned based on bounding box calculation */}
                      {lines.map((line, lineIndex) => (
                        <text
                          key={`line-${lineIndex}`}
                          x={textX}
                          y={textY + (lineIndex - (lines.length - 1) / 2) * lineHeight}
                          textAnchor="middle"
                          dominantBaseline="middle"
                          fill="white"
                          fontSize={fontSize}
                          fontWeight="600"
                          fontFamily="Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
                        >
                          {line}
                        </text>
                      ))}
                      
                      {/* Colored line under the title */}
                      <line
                        x1={textX - maxLineWidth / 2}
                        y1={textY + halfHeight + (5 * scaleFactor)}
                        x2={textX + maxLineWidth / 2}
                        y2={textY + halfHeight + (5 * scaleFactor)}
                        stroke={node.color}
                        strokeWidth={2 * scaleFactor}
                      />
                      
                      {/* Details text below the divider line */}
                      {node.details.map((detail, detailIndex) => (
                        <text
                          key={`detail-${detailIndex}`}
                          x={textX}
                          y={textY + halfHeight + (20 * scaleFactor) + (detailIndex * (12 * scaleFactor))}
                          textAnchor="middle"
                          dominantBaseline="middle"
                          fill="#cbd5e1"
                          fontSize={fontSize * 0.7}
                          fontWeight="400"
                          fontFamily="Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
                        >
                          {detail}
                        </text>
                      ))}
                    </React.Fragment>
                  );
                })()}
              </g>
            );
          })}
        </svg>
      </Box>
    </Box>
  );
};