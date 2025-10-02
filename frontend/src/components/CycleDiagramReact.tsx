import React, { useEffect, useRef } from 'react';
import { CycleDiagram, CycleDiagramProps } from './CycleDiagram';

export const CycleDiagramReact: React.FC<CycleDiagramProps> = (props) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const diagramRef = useRef<CycleDiagram | null>(null);

  useEffect(() => {
    if (containerRef.current) {
      // Create new instance of the class-based CycleDiagram
      diagramRef.current = new CycleDiagram(containerRef.current, props);
    }

    return () => {
      // Clean up the diagram when component unmounts
      if (diagramRef.current) {
        diagramRef.current.destroy();
        diagramRef.current = null;
      }
    };
  }, []);

  // Update diagram when props change
  useEffect(() => {
    if (diagramRef.current) {
      diagramRef.current.update(props);
    }
  }, [props]);

  return <div ref={containerRef} />;
};
