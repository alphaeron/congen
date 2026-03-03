import {
  capitalizeFirstLetter,
  capitalizeEachWord,
  categorizeExerciseVolume,
  formatWeightWithUnit,
  convertDisplayWeightToKg,
  KG_TO_LBS,
  formatBandWeightWithUnit,
} from './utils';

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

describe('formatWeightWithUnit', () => {
  it('returns "-" for null, undefined, or 0 weight', () => {
    expect(formatWeightWithUnit(null, 'LBS')).toBe('-');
    expect(formatWeightWithUnit(undefined, 'LBS')).toBe('-');
    expect(formatWeightWithUnit(0, 'LBS')).toBe('-');
    expect(formatWeightWithUnit(null, 'KG')).toBe('-');
  });

  it('rounds lbs to nearest 0.5 lbs to avoid conversion artifacts', () => {
    expect(formatWeightWithUnit(6.8, 'LBS')).toBe('15 lbs');
    expect(formatWeightWithUnit(18.14, 'LBS')).toBe('40 lbs');
    expect(formatWeightWithUnit(27.22, 'LBS')).toBe('60 lbs');
    expect(formatWeightWithUnit(61.23, 'LBS')).toBe('135 lbs');
    expect(formatWeightWithUnit(72.57, 'LBS')).toBe('160 lbs');
  });

  it('rounds lbs to nearest 0.5 lbs increment', () => {
    expect(formatWeightWithUnit(6.94, 'LBS')).toBe('15.5 lbs');
    expect(formatWeightWithUnit(11.34, 'LBS')).toBe('25 lbs');
    expect(formatWeightWithUnit(6.92, 'LBS')).toBe('15.5 lbs');
    expect(formatWeightWithUnit(6.81, 'LBS')).toBe('15 lbs');
  });

  it('formats kg with two decimal places', () => {
    expect(formatWeightWithUnit(100, 'KG')).toBe('100 kg');
    expect(formatWeightWithUnit(67.5, 'KG')).toBe('67.5 kg');
  });

  it('omits unit when includeUnit is false', () => {
    expect(formatWeightWithUnit(6.8, 'LBS', false)).toBe('15');
    expect(formatWeightWithUnit(100, 'KG', false)).toBe('100');
  });

  it('returns parseable number when includeUnit is false for form initial values', () => {
    expect(parseFloat(formatWeightWithUnit(72.57, 'LBS', false)) || 0).toBe(160);
    expect(parseFloat(formatWeightWithUnit(6.94, 'LBS', false)) || 0).toBe(15.5);
    expect(parseFloat(formatWeightWithUnit(0, 'LBS', false)) || 0).toBe(0);
  });
});

describe('convertDisplayWeightToKg', () => {
  it('returns weight unchanged when preferred unit is KG', () => {
    expect(convertDisplayWeightToKg(100, 'KG')).toBe(100);
  });

  it('converts lbs to kg when preferred unit is LBS', () => {
    expect(convertDisplayWeightToKg(160, 'LBS')).toBeCloseTo(72.574, 2);
  });
});

describe('KG_TO_LBS', () => {
  it('is the standard conversion factor', () => {
    expect(KG_TO_LBS).toBe(2.20462);
  });
});

describe('formatBandWeightWithUnit', () => {
  it('returns empty string for null, undefined, zero, or non-positive', () => {
    expect(formatBandWeightWithUnit(undefined)).toBe('');
    expect(formatBandWeightWithUnit(null)).toBe('');
    expect(formatBandWeightWithUnit(0)).toBe('');
    expect(formatBandWeightWithUnit(-1)).toBe('');
  });

  it('always formats band weight in lbs', () => {
    expect(formatBandWeightWithUnit(30)).toBe('30 lbs');
    expect(formatBandWeightWithUnit(15)).toBe('15 lbs');
  });

  it('omits unit when includeUnit is false', () => {
    expect(formatBandWeightWithUnit(30, false)).toBe('30');
  });
});
