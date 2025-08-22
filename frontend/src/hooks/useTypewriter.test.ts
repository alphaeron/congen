import { renderHook, act } from '@testing-library/react';

import { useTypewriter } from './useTypewriter';

describe('useTypewriter', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    act(() => {
      jest.runOnlyPendingTimers();
    });
    jest.useRealTimers();
  });

  it('should return empty string initially', () => {
    const { result } = renderHook(() => useTypewriter('Hello World'));
    expect(result.current).toBe('');
  });

  it('should animate text character by character', () => {
    const { result } = renderHook(() => useTypewriter('Hello', 100));

    // Initially empty
    expect(result.current).toBe('');

    // After first tick
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('H');

    // After second tick
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('He');

    // After third tick
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('Hel');

    // After fourth tick
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('Hell');

    // After fifth tick
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('Hello');
  });

  it('should use default speed of 50ms when not specified', () => {
    const { result } = renderHook(() => useTypewriter('Hi'));

    act(() => {
      jest.advanceTimersByTime(50);
    });
    expect(result.current).toBe('H');

    act(() => {
      jest.advanceTimersByTime(50);
    });
    expect(result.current).toBe('Hi');
  });

  it('should handle empty string', () => {
    const { result } = renderHook(() => useTypewriter(''));

    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('');
  });

  it('should reset animation when text changes', () => {
    const { result, rerender } = renderHook(({ text }) => useTypewriter(text, 100), {
      initialProps: { text: 'Hello' },
    });

    // Animate first text
    act(() => {
      jest.advanceTimersByTime(200);
    });
    expect(result.current).toBe('He');

    // Change text
    act(() => {
      rerender({ text: 'World' });
    });

    // Should reset to empty
    expect(result.current).toBe('');

    // Animate new text
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('W');
  });

  it('should reset animation when speed changes', () => {
    const { result, rerender } = renderHook(({ speed }) => useTypewriter('Hello', speed), {
      initialProps: { speed: 100 },
    });

    // Animate with first speed
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('H');

    // Change speed
    act(() => {
      rerender({ speed: 200 });
    });

    // Should reset to empty
    expect(result.current).toBe('');

    // Animate with new speed
    act(() => {
      jest.advanceTimersByTime(200);
    });
    expect(result.current).toBe('H');
  });

  it('should complete animation and stop timer', () => {
    const { result } = renderHook(() => useTypewriter('Hi', 100));

    // Complete animation
    act(() => {
      jest.advanceTimersByTime(200);
    });
    expect(result.current).toBe('Hi');

    // Timer should be cleared, no more changes
    act(() => {
      jest.advanceTimersByTime(100);
    });
    expect(result.current).toBe('Hi');
  });

  it('should clean up timer on unmount', () => {
    const clearIntervalSpy = jest.spyOn(global, 'clearInterval');
    const { unmount } = renderHook(() => useTypewriter('Hello', 100));

    act(() => {
      unmount();
    });

    expect(clearIntervalSpy).toHaveBeenCalled();
    clearIntervalSpy.mockRestore();
  });
});
