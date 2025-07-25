import { useEffect, useState } from 'react';

/**
 * useTypewriter animates a string being typed out, one character at a time.
 * @param text The string to animate.
 * @param speed Typing speed in ms per character (default: 50ms).
 * @returns The currently visible portion of the string.
 */
export function useTypewriter(text: string, speed: number = 50): string {
  const [displayed, setDisplayed] = useState('');

  useEffect(() => {
    let current = 0;
    setDisplayed('');
    if (!text) return;
    const interval = setInterval(() => {
      current++;
      setDisplayed(text.slice(0, current));
      if (current >= text.length) {
        clearInterval(interval);
      }
    }, speed);
    return () => clearInterval(interval);
  }, [text, speed]);

  return displayed;
}
