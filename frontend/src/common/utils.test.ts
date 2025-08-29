import { capitalizeFirstLetter, capitalizeEachWord, categorizeExerciseVolume } from './utils';

describe('capitalizeFirstLetter', () => {
  it('Only capitalizes the first word', () => {
    expect(capitalizeFirstLetter('one two')).toBe('One two');
  });
});

describe('capitalizeEachWord', () => {
  it('Capitalizes all words', () => {
    expect(capitalizeEachWord('one two')).toBe('One Two');
  });
});

describe('categorizeExerciseVolume', () => {
  const mockExerciseInfo = {
    is_accessory: false,
    is_upper: true,
  };

  const mockAccessoryExerciseInfo = {
    is_accessory: true,
    is_upper: false,
  };

  it('should categorize accessory exercises as accessory volume', () => {
    const result = categorizeExerciseVolume(mockAccessoryExerciseInfo, 'ME Upper Day', 100);
    expect(result.accessoryVolume).toBe(100);
    expect(result.maxEffortVolume).toBe(0);
    expect(result.dynamicEffortVolume).toBe(0);
  });

  it('should categorize primary exercises in pure ME workouts as max effort', () => {
    const result = categorizeExerciseVolume(mockExerciseInfo, 'ME Upper Day', 100);
    expect(result.maxEffortVolume).toBe(100);
    expect(result.dynamicEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });

  it('should categorize primary exercises in pure DE workouts as dynamic effort', () => {
    const result = categorizeExerciseVolume(mockExerciseInfo, 'DE Lower Day', 100);
    expect(result.dynamicEffortVolume).toBe(100);
    expect(result.maxEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });

  it('should categorize upper body exercises in ME Upper + DE Lower as max effort', () => {
    const result = categorizeExerciseVolume(mockExerciseInfo, 'ME Upper DE Lower Day', 100);
    expect(result.maxEffortVolume).toBe(100);
    expect(result.dynamicEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });

  it('should categorize lower body exercises in ME Upper + DE Lower as dynamic effort', () => {
    const lowerBodyExercise = { is_accessory: false, is_upper: false };
    const result = categorizeExerciseVolume(lowerBodyExercise, 'ME Upper DE Lower Day', 100);
    expect(result.dynamicEffortVolume).toBe(100);
    expect(result.maxEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });

  it('should handle undefined exercise info gracefully', () => {
    const result = categorizeExerciseVolume(undefined, 'ME Upper Day', 100);
    expect(result.maxEffortVolume).toBe(100);
    expect(result.dynamicEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });

  it('should handle undefined exercise info in combined workouts', () => {
    const result = categorizeExerciseVolume(undefined, 'ME Upper DE Lower Day', 100);
    expect(result.dynamicEffortVolume).toBe(100);
    expect(result.maxEffortVolume).toBe(0);
    expect(result.accessoryVolume).toBe(0);
  });
});
