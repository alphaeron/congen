import { Button, Typography } from '@mui/material';
import { motion } from 'framer-motion';
import React from 'react';

import { GameCard } from './GameTheme';

interface HeroCTAProps {
  onClick: () => void;
  disabled?: boolean;
  loading?: boolean;
  subtitle?: string;
  variant?: 'primary' | 'secondary';
}

/**
 * Hero Call-to-Action component for prominent action buttons with game-like styling.
 *
 * @param onClick Function to call when button is clicked
 * @param disabled Whether the button is disabled
 * @param loading Whether the button is in loading state
 * @param subtitle Optional subtitle text
 * @param variant Visual variant of the CTA (default: 'primary')
 * @return Hero CTA component
 */
export const HeroCTA: React.FC<HeroCTAProps> = ({
  onClick,
  disabled = false,
  loading = false,
  subtitle,
  variant = 'primary',
}) => {
  const isPrimary = variant === 'primary';

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <motion.div whileHover={{ y: -8 }} transition={{ duration: 0.3, ease: 'easeOut' }}>
        <GameCard
          className={`glassmorphism-card ${isPrimary ? 'game-card-interactive' : ''}`}
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            p: 4,
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          {/* Animated background gradient */}
          {isPrimary && (
            <motion.div
              animate={{
                opacity: [0.3, 0.6, 0.3],
              }}
              transition={{
                duration: 3,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background:
                  'linear-gradient(45deg, rgba(59, 130, 246, 0.1) 0%, rgba(16, 185, 129, 0.1) 50%, rgba(245, 158, 11, 0.1) 100%)',
              }}
            />
          )}

          {/* Content */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            style={{ position: 'relative', zIndex: 1, textAlign: 'center' }}
          >
            {subtitle && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.4 }}
              >
                <Typography
                  variant="body1"
                  sx={{
                    color: '#94a3b8',
                    mb: 3,
                    fontSize: { xs: '0.9rem', sm: '1rem' },
                    maxWidth: 400,
                  }}
                >
                  {subtitle}
                </Typography>
              </motion.div>
            )}

            <motion.div
              whileHover={{ y: -4 }}
              whileTap={{ scale: 0.98 }}
              transition={{ duration: 0.2 }}
            >
              <Button
                variant="contained"
                size="large"
                onClick={onClick}
                disabled={disabled || loading}
                sx={{
                  px: 4,
                  py: 1.5,
                  fontSize: '1.1rem',
                  fontWeight: 700,
                  borderRadius: 2,
                  background: isPrimary
                    ? 'linear-gradient(135deg, #3b82f6 0%, #10b981 100%)'
                    : 'linear-gradient(135deg, #64748b 0%, #475569 100%)',
                  border: 'none',
                  boxShadow: isPrimary
                    ? '0 8px 16px rgba(59, 130, 246, 0.4)'
                    : '0 4px 8px rgba(0, 0, 0, 0.2)',
                  '&:disabled': {
                    background: 'rgba(100, 116, 139, 0.3)',
                    color: 'rgba(255, 255, 255, 0.5)',
                  },
                }}
              >
                {loading ? 'Generating...' : 'Generate Next Week'}
              </Button>
            </motion.div>
          </motion.div>
        </GameCard>
      </motion.div>
    </motion.div>
  );
};
