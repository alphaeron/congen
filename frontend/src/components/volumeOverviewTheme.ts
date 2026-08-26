/** Plan-relative bullet band colors for volume overview cards. */
export const VOLUME_BAND_COLORS = {
  poor: '#5c2b2b',
  ok: '#5c4a1f',
  good: '#1f4d3a',
  overload: '#6b2a2a',
};

/** Marker colors for plan target and same-week average. */
export const VOLUME_MARKER_COLORS = {
  plan: 'var(--game-cyan)',
  weekAvg: '#f0c14a',
};

/** Series colors for volume trend sparkline and expanded dialog. */
export const VOLUME_SERIES_COLORS = {
  volume: 'var(--game-cyan)',
  acwr: '#f0c14a',
  intensity: 'var(--game-success)',
};

/** CongenBullet margin leaving room for longer axis ticks. */
export const VOLUME_BULLET_MARGIN = { top: 10, right: 8, bottom: 40, left: 8 };
