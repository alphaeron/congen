import {
  VOLUME_BAND_COLORS,
  VOLUME_BULLET_MARGIN,
  VOLUME_MARKER_COLORS,
  VOLUME_SERIES_COLORS,
} from './volumeOverviewTheme';

describe('volumeOverviewTheme', () => {
  it('exports band, marker, series, and margin constants', () => {
    expect(VOLUME_BAND_COLORS.poor).toBeTruthy();
    expect(VOLUME_BAND_COLORS.overload).toBeTruthy();
    expect(VOLUME_MARKER_COLORS.plan).toBeTruthy();
    expect(VOLUME_SERIES_COLORS.volume).toBeTruthy();
    expect(VOLUME_BULLET_MARGIN.bottom).toBeGreaterThanOrEqual(40);
  });
});
