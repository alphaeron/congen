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
  minTextSize?: number; // Minimum text size before moving outside
  showText?: boolean; // Control text visibility for animation
}

export class CycleDiagram {
  private container: HTMLElement;
  private props: CycleDiagramProps;
  private resizeObserver: ResizeObserver | null = null;
  private dimensions: { width: number; height: number } = { width: 0, height: 0 };

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
      minTextSize: 10,
      showText: true,
      ...props
    };
    this.setupResizeObserver();
    
    // Trigger initial render after a short delay to ensure container is ready
    setTimeout(() => {
      if (this.dimensions.width === 0 || this.dimensions.height === 0) {
        this.render();
      }
    }, 50);
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
        '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272',
        '#fc8452', '#9a60b4', '#ea7ccc', '#5d7092', '#6e9ef1', '#a0a7e6'
      ],
      vibrant: [
        '#e74c3c', '#f39c12', '#f1c40f', '#2ecc71', '#1abc9c', '#3498db',
        '#9b59b6', '#e67e22', '#34495e', '#95a5a6', '#e91e63', '#ff5722'
      ],
      pastel: [
        '#f8b5c1', '#f7d794', '#f4e4bc', '#a8e6cf', '#b8e6b8', '#b3d9ff',
        '#d1a3ff', '#ffb3d1', '#c7ecee', '#dda0dd', '#f0e68c', '#ffb6c1'
      ],
      monochrome: [
        '#2c3e50', '#34495e', '#7f8c8d', '#95a5a6', '#bdc3c7', '#ecf0f1',
        '#d5dbdb', '#aeb6bf', '#85929e', '#5d6d7e', '#34495e', '#2c3e50'
      ],
      ocean: [
        '#2980b9', '#3498db', '#5dade2', '#85c1e9', '#aed6f1', '#d6eaf8',
        '#1abc9c', '#16a085', '#138d75', '#0e6655', '#0b5345', '#0a3d2e'
      ],
      sunset: [
        '#e67e22', '#d35400', '#c0392b', '#a93226', '#922b21', '#7b241c',
        '#f39c12', '#e67e22', '#d35400', '#ba4a00', '#a04000', '#8b4513'
      ],
      forest: [
        '#27ae60', '#229954', '#1e8449', '#196f3d', '#145a32', '#0f5132',
        '#52c41a', '#73d13d', '#95de64', '#b7eb8f', '#d9f7be', '#f6ffed'
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
      outerRadius: propOuterRadius, 
      innerRadius: propInnerRadius, 
      centerX: propCenterX, 
      centerY: propCenterY, 
      width: propWidth, 
      height: propHeight,
      theme = 'default',
      customColors,
      minTextSize = 10
    } = this.props;

    // Calculate responsive dimensions
    let width, height, outerRadius, innerRadius, centerX, centerY;
    
    // Use stored dimensions from ResizeObserver
    let containerSize = Math.min(this.dimensions.width, this.dimensions.height);
    
    // Fallback if dimensions are still 0 (initial render before ResizeObserver fires)
    if (containerSize === 0) {
      // Get the parent container dimensions using getBoundingClientRect
      const parentElement = this.container.parentElement;
      if (parentElement) {
        const parentRect = parentElement.getBoundingClientRect();
        containerSize = Math.min(parentRect.width, parentRect.height);
      } else {
        // Last resort: use the container itself
        containerSize = Math.min(this.container.offsetWidth, this.container.offsetHeight);
      }
    }
    
    width = height = containerSize;
    
    centerX = centerY = width / 2;
    
    // Calculate responsive radii (leave 20% margin for text)
    const maxRadius = (width * 0.4); // 40% of width for radius
    outerRadius = maxRadius;
    innerRadius = maxRadius * 0.82; // Inner radius is 82% of outer

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
                  
                  // Use a conservative estimate of available space (50% of segment or distance to adjacent)
                  const availableAngle = Math.min(segmentAngle * 0.5, minAngleDiff * 0.8);
                  const maxArcWidth = availableAngle * innerRadius;
                  
                  // Use Nivo's default text sizes (fixed, not dynamic)
                  const titleFontSize = 14; // Nivo default title size
                  const descriptionFontSize = 12; // Nivo default description size
                  const charWidth = titleFontSize * 0.6; // Approximate character width
                  
                  // Determine if text should be positioned outside the circle
                  // Use more aggressive thresholds to move text outside earlier
                  const textFitsInside = (maxArcWidth > 100) &&
                    (innerRadius > 100);
                  
                  const useOutsideText = !textFitsInside;
                  
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
                  const lineHeight = titleFontSize * 1.2; // Line height based on title font size
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
                  
                  let desiredRadius;
                  
                  if (useOutsideText) {
                    // Position text outside the circle
                    const outsideGap = 20 * scaleFactor; // Gap between circle and text
                    desiredRadius = outerRadius + outsideGap + minDistance;
                  } else {
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
                    desiredRadius = innerRadius - gapDistance - minDistance;
                  }
                  
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
        textElement.setAttribute('font-size', titleFontSize.toString());
        textElement.setAttribute('font-weight', '600');
        textElement.setAttribute('font-family', 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif');
        textElement.setAttribute('opacity', this.props.showText ? '1' : '0');
        textElement.setAttribute('class', 'cycle-diagram-text');
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
      lineElement.setAttribute('opacity', this.props.showText ? '1' : '0');
      lineElement.setAttribute('class', 'cycle-diagram-line');
      group.appendChild(lineElement);
      
      // Create details text elements
      node.details.forEach((detail, detailIndex) => {
        const detailElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        detailElement.setAttribute('x', textX.toString());
        detailElement.setAttribute('y', (textY + halfHeight + (20 * scaleFactor) + (detailIndex * (12 * scaleFactor))).toString());
        detailElement.setAttribute('text-anchor', 'middle');
        detailElement.setAttribute('dominant-baseline', 'middle');
        detailElement.setAttribute('fill', '#cbd5e1');
        detailElement.setAttribute('font-size', descriptionFontSize.toString());
        detailElement.setAttribute('font-weight', '400');
        detailElement.setAttribute('font-family', 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif');
        detailElement.setAttribute('opacity', this.props.showText ? '1' : '0');
        detailElement.setAttribute('class', 'cycle-diagram-detail');
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

  // Method to update text visibility for animation
  public updateTextVisibility(showText: boolean): void {
    this.props.showText = showText;
    
    // Animate all text-related elements: titles, lines, and details
    const textElements = this.container.querySelectorAll('.cycle-diagram-text, .cycle-diagram-line, .cycle-diagram-detail');
    textElements.forEach(element => {
      if (showText) {
        // Use Web Animations API for smooth opacity animation
        element.animate([
          { opacity: 0 },
          { opacity: 1 }
        ], {
          duration: 800,
          easing: 'ease-out',
          fill: 'forwards'
        });
      } else {
        element.setAttribute('opacity', '0');
      }
    });
  }

  // Method to destroy the diagram
  public destroy(): void {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
      this.resizeObserver = null;
    }
    this.container.innerHTML = '';
  }

  /**
   * Setup resize observer for responsive behavior
   */
  private setupResizeObserver(): void {
    if (typeof ResizeObserver === 'undefined') {
      return;
    }

    this.resizeObserver = new ResizeObserver((entries) => {
      for (let entry of entries) {
        // Get parent container dimensions using getBoundingClientRect
        const parentElement = this.container.parentElement;
        if (parentElement) {
          const parentRect = parentElement.getBoundingClientRect();
          this.dimensions = {
            width: parentRect.width,
            height: parentRect.height,
          };
        } else {
          // Fallback to container itself
          this.dimensions = {
            width: this.container.offsetWidth,
            height: this.container.offsetHeight,
          };
        }
        
        // Only render if we have valid dimensions
        if (this.dimensions.width > 0 && this.dimensions.height > 0) {
          // Debounce resize events
          if (this.resizeTimeout) {
            clearTimeout(this.resizeTimeout);
          }
          this.resizeTimeout = setTimeout(() => {
            this.render();
          }, 100);
        }
      }
    });

    // Observe the parent element, not the container itself
    const parentElement = this.container.parentElement;
    if (parentElement) {
      this.resizeObserver.observe(parentElement);
    } else {
      this.resizeObserver.observe(this.container);
    }
  }

  private resizeTimeout: NodeJS.Timeout | null = null;
}