export interface CycleNode {
  id: string;
  label: string;
  details: string[];
  color?: string; // Made optional - will be auto-generated if not provided
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
  theme?: 'default' | 'vibrant' | 'pastel' | 'monochrome' | 'ocean' | 'sunset' | 'forest' | 'custom';
  customColors?: string[]; // For custom theme
}

export class CycleDiagram {
  private container: HTMLElement;
  private props: CycleDiagramProps;

  constructor(container: HTMLElement, props: CycleDiagramProps) {
    this.container = container;
    this.props = {
      outerRadius: 310,
      innerRadius: 255,
      centerX: 300,
      centerY: 300,
      width: 600,
      height: 600,
      theme: 'default',
      ...props
    };
    this.render();
  }

  /**
   * Generate intelligent color palette based on segment count and theme
   */
  private generateColorPalette(segmentCount: number, theme: string, customColors?: string[]): string[] {
    if (theme === 'custom' && customColors && customColors.length >= segmentCount) {
      return customColors.slice(0, segmentCount);
    }

    const colorSchemes = {
      default: [
        '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4',
        '#84cc16', '#f97316', '#ec4899', '#6366f1', '#14b8a6', '#eab308'
      ],
      vibrant: [
        '#ff0080', '#00ff80', '#8000ff', '#ff8000', '#0080ff', '#80ff00',
        '#ff0040', '#40ff00', '#4000ff', '#ff4000', '#0040ff', '#40ff80'
      ],
      pastel: [
        '#ffb3ba', '#ffdfba', '#ffffba', '#baffc9', '#bae1ff', '#e6b3ff',
        '#ffb3d9', '#ffd9b3', '#d9ffb3', '#b3ffd9', '#b3d9ff', '#d9b3ff'
      ],
      monochrome: [
        '#1f2937', '#374151', '#4b5563', '#6b7280', '#9ca3af', '#d1d5db',
        '#e5e7eb', '#f3f4f6', '#f9fafb', '#ffffff', '#000000', '#111827'
      ],
      ocean: [
        '#0ea5e9', '#0284c7', '#0369a1', '#075985', '#0c4a6e', '#164e63',
        '#155e75', '#0f766e', '#0d9488', '#14b8a6', '#2dd4bf', '#5eead4'
      ],
      sunset: [
        '#f97316', '#ea580c', '#dc2626', '#b91c1c', '#991b1b', '#7f1d1d',
        '#fbbf24', '#f59e0b', '#d97706', '#b45309', '#92400e', '#78350f'
      ],
      forest: [
        '#16a34a', '#15803d', '#166534', '#14532d', '#052e16', '#365314',
        '#4ade80', '#22c55e', '#16a34a', '#15803d', '#166534', '#14532d'
      ]
    };

    const baseColors = colorSchemes[theme as keyof typeof colorSchemes] || colorSchemes.default;
    
    // If we need more colors than available, generate additional colors
    if (segmentCount > baseColors.length) {
      const additionalColors = this.generateAdditionalColors(segmentCount - baseColors.length, baseColors);
      return [...baseColors, ...additionalColors];
    }
    
    // If we need fewer colors, intelligently select the most harmonious ones
    if (segmentCount < baseColors.length) {
      return this.selectHarmoniousColors(baseColors, segmentCount);
    }
    
    return baseColors;
  }

  /**
   * Generate additional colors that harmonize with the base palette
   */
  private generateAdditionalColors(count: number, baseColors: string[]): string[] {
    const additionalColors: string[] = [];
    
    for (let i = 0; i < count; i++) {
      // Create variations by adjusting hue, saturation, and lightness
      const baseColor = baseColors[i % baseColors.length];
      const hsl = this.hexToHsl(baseColor);
      
      // Rotate hue and adjust saturation/lightness for variety
      const newHsl = {
        h: (hsl.h + (i * 30)) % 360,
        s: Math.max(20, Math.min(80, hsl.s + (i % 2 === 0 ? 10 : -10))),
        l: Math.max(30, Math.min(70, hsl.l + (i % 3 === 0 ? 5 : -5)))
      };
      
      additionalColors.push(this.hslToHex(newHsl.h, newHsl.s, newHsl.l));
    }
    
    return additionalColors;
  }

  /**
   * Select the most harmonious colors from a larger palette
   */
  private selectHarmoniousColors(colors: string[], count: number): string[] {
    if (count >= colors.length) return colors;
    
    // For small counts, use evenly spaced colors
    if (count <= 3) {
      const step = Math.floor(colors.length / count);
      return colors.filter((_, index) => index % step === 0).slice(0, count);
    }
    
    // For larger counts, use a more sophisticated selection
    const selected: string[] = [];
    const step = colors.length / count;
    
    for (let i = 0; i < count; i++) {
      const index = Math.floor(i * step);
      selected.push(colors[index]);
    }
    
    return selected;
  }

  /**
   * Convert hex color to HSL
   */
  private hexToHsl(hex: string): { h: number; s: number; l: number } {
    const r = parseInt(hex.slice(1, 3), 16) / 255;
    const g = parseInt(hex.slice(3, 5), 16) / 255;
    const b = parseInt(hex.slice(5, 7), 16) / 255;

    const max = Math.max(r, g, b);
    const min = Math.min(r, g, b);
    let h = 0, s = 0, l = (max + min) / 2;

    if (max !== min) {
      const d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      
      switch (max) {
        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
        case g: h = (b - r) / d + 2; break;
        case b: h = (r - g) / d + 4; break;
      }
      h /= 6;
    }

    return { h: h * 360, s: s * 100, l: l * 100 };
  }

  /**
   * Convert HSL to hex color
   */
  private hslToHex(h: number, s: number, l: number): string {
    h /= 360;
    s /= 100;
    l /= 100;

    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1;
      if (t > 1) t -= 1;
      if (t < 1/6) return p + (q - p) * 6 * t;
      if (t < 1/2) return q;
      if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
      return p;
    };

    let r, g, b;

    if (s === 0) {
      r = g = b = l; // achromatic
    } else {
      const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      const p = 2 * l - q;
      r = hue2rgb(p, q, h + 1/3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1/3);
    }

    const toHex = (c: number) => {
      const hex = Math.round(c * 255).toString(16);
      return hex.length === 1 ? '0' + hex : hex;
    };

    return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
  }

  private render(): void {
    const { 
      nodes, 
      outerRadius = 310, 
      innerRadius = 255, 
      centerX = 300, 
      centerY = 300, 
      width = 600, 
      height = 600,
      theme = 'default',
      customColors
    } = this.props;

    // Generate intelligent color palette
    const colorPalette = this.generateColorPalette(nodes.length, theme, customColors);
    
    // Assign colors to nodes (use provided colors or generated ones)
    const nodesWithColors = nodes.map((node, index) => ({
      ...node,
      color: node.color || colorPalette[index]
    }));
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

    // Clear container and create HTML structure
    this.container.innerHTML = '';
    
    // Create main container
    const mainContainer = document.createElement('div');
    mainContainer.className = 'cycle-diagram-container';
    mainContainer.style.cssText = `
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 24px;
      width: 100%;
      max-width: 100%;
      overflow: visible;
    `;

    // Create SVG container
    const svgContainer = document.createElement('div');
    svgContainer.className = 'cycle-diagram-svg-container';
    svgContainer.style.cssText = `
      width: ${width}px;
      height: ${height}px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: visible;
    `;

    // Create SVG element
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('width', width.toString());
    svg.setAttribute('height', height.toString());
    svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
    svg.style.overflow = 'visible';

    // Add title if provided
    if (this.props.title) {
      const titleElement = document.createElement('h2');
      titleElement.className = 'cycle-diagram-title';
      titleElement.textContent = this.props.title;
      titleElement.style.cssText = `
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #1f2937;
        text-align: center;
      `;
      mainContainer.appendChild(titleElement);
    }

    // Add SVG to container
    svgContainer.appendChild(svg);
    mainContainer.appendChild(svgContainer);
    this.container.appendChild(mainContainer);
    // Add SVG definitions
    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
    const filter = document.createElementNS('http://www.w3.org/2000/svg', 'filter');
    filter.setAttribute('id', 'segmentShadow');
    const feDropShadow = document.createElementNS('http://www.w3.org/2000/svg', 'feDropShadow');
    feDropShadow.setAttribute('dx', '2');
    feDropShadow.setAttribute('dy', '2');
    feDropShadow.setAttribute('stdDeviation', '2');
    feDropShadow.setAttribute('floodColor', 'rgba(0,0,0,0.3)');
    filter.appendChild(feDropShadow);
    defs.appendChild(filter);
    svg.appendChild(defs);
    
    // Render each segment as a single clean path - in correct order
    nodesWithColors.forEach((node, index) => {
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

      // Create group element for this segment
      const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      
      // Create single segment path
      const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      path.setAttribute('d', segmentPath);
      path.setAttribute('fill', node.color);
      path.setAttribute('stroke', 'none');
      path.setAttribute('filter', 'url(#segmentShadow)');
      group.appendChild(path);
                
      // Calculate text position with intelligent wrapping, consistent gaps, and collision detection
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
                  
      // Create title text elements
      lines.forEach((line, lineIndex) => {
        const textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        textElement.setAttribute('x', textX.toString());
        textElement.setAttribute('y', (textY + (lineIndex - (lines.length - 1) / 2) * lineHeight).toString());
        textElement.setAttribute('text-anchor', 'middle');
        textElement.setAttribute('dominant-baseline', 'middle');
        textElement.setAttribute('fill', 'white');
        textElement.setAttribute('font-size', fontSize.toString());
        textElement.setAttribute('font-weight', '600');
        textElement.setAttribute('font-family', 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif');
        textElement.textContent = line;
        group.appendChild(textElement);
      });
      
      // Create colored line under the title
      const lineElement = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      lineElement.setAttribute('x1', (textX - maxLineWidth / 2).toString());
      lineElement.setAttribute('y1', (textY + halfHeight + (5 * scaleFactor)).toString());
      lineElement.setAttribute('x2', (textX + maxLineWidth / 2).toString());
      lineElement.setAttribute('y2', (textY + halfHeight + (5 * scaleFactor)).toString());
      lineElement.setAttribute('stroke', node.color);
      lineElement.setAttribute('stroke-width', (2 * scaleFactor).toString());
      group.appendChild(lineElement);
      
      // Create details text elements
      node.details.forEach((detail, detailIndex) => {
        const detailElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        detailElement.setAttribute('x', textX.toString());
        detailElement.setAttribute('y', (textY + halfHeight + (20 * scaleFactor) + (detailIndex * (12 * scaleFactor))).toString());
        detailElement.setAttribute('text-anchor', 'middle');
        detailElement.setAttribute('dominant-baseline', 'middle');
        detailElement.setAttribute('fill', '#cbd5e1');
        detailElement.setAttribute('font-size', (fontSize * 0.7).toString());
        detailElement.setAttribute('font-weight', '400');
        detailElement.setAttribute('font-family', 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif');
        detailElement.textContent = detail;
        group.appendChild(detailElement);
      });
      
      // Add group to SVG
      svg.appendChild(group);
    });
  }

  // Method to update the diagram with new props
  public update(newProps: Partial<CycleDiagramProps>): void {
    this.props = { ...this.props, ...newProps };
    this.render();
  }

  // Method to destroy the diagram
  public destroy(): void {
    this.container.innerHTML = '';
  }
}