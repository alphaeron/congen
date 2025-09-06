#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * Performance Monitoring Script
 * Tracks bundle size metrics and performance over time
 */

const METRICS_FILE = path.join(__dirname, '../performance-metrics.json');
const DIST_DIR = path.join(__dirname, '../dist');
const JS_DIR = path.join(DIST_DIR, 'js');

function formatBytes(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function getCurrentMetrics() {
  if (!fs.existsSync(DIST_DIR)) {
    console.error('❌ Dist directory not found. Run "npm run build" first.');
    process.exit(1);
  }

  const jsFiles = fs.readdirSync(JS_DIR).filter(file => file.endsWith('.js'));
  const totalSize = jsFiles.reduce((total, file) => {
    const filePath = path.join(JS_DIR, file);
    const stats = fs.statSync(filePath);
    return total + stats.size;
  }, 0);

  const fileSizes = jsFiles
    .map(file => {
      const filePath = path.join(JS_DIR, file);
      const stats = fs.statSync(filePath);
      return {
        name: file,
        size: stats.size,
        percentage: ((stats.size / totalSize) * 100).toFixed(1),
      };
    })
    .sort((a, b) => b.size - a.size);

  return {
    timestamp: new Date().toISOString(),
    totalSize,
    fileCount: jsFiles.length,
    files: fileSizes,
    performance: {
      score: calculatePerformanceScore(totalSize, fileSizes),
      recommendations: getRecommendations(totalSize, fileSizes),
    },
  };
}

function calculatePerformanceScore(totalSize, fileSizes) {
  let score = 100;

  // Penalize large total size
  if (totalSize > 2000000)
    score -= 30; // 2MB+
  else if (totalSize > 1500000)
    score -= 20; // 1.5MB+
  else if (totalSize > 1000000) score -= 10; // 1MB+

  // Penalize large individual files
  const largeFiles = fileSizes.filter(file => file.size > 500000);
  largeFiles.forEach(() => (score -= 10));

  // Penalize too many files (fragmentation)
  if (fileSizes.length > 20) score -= 5;
  else if (fileSizes.length > 15) score -= 3;

  return Math.max(0, score);
}

function getRecommendations(totalSize, fileSizes) {
  const recommendations = [];

  if (totalSize > 1500000) {
    recommendations.push('Consider code splitting and lazy loading');
  }

  const largeFiles = fileSizes.filter(file => file.size > 500000);
  if (largeFiles.length > 0) {
    recommendations.push('Optimize large files: ' + largeFiles.map(f => f.name).join(', '));
  }

  if (fileSizes.length > 15) {
    recommendations.push('Consider consolidating small chunks');
  }

  const muiFiles = fileSizes.filter(file => file.name.includes('mui'));
  if (muiFiles.length > 5) {
    recommendations.push('Consider optimizing Material-UI imports');
  }

  if (recommendations.length === 0) {
    recommendations.push('Bundle is well optimized!');
  }

  return recommendations;
}

function loadHistoricalMetrics() {
  if (fs.existsSync(METRICS_FILE)) {
    try {
      return JSON.parse(fs.readFileSync(METRICS_FILE, 'utf8'));
    } catch (error) {
      console.warn('⚠️  Could not load historical metrics:', error.message);
      return [];
    }
  }
  return [];
}

function saveMetrics(metrics) {
  const historical = loadHistoricalMetrics();
  historical.push(metrics);

  // Keep only last 50 measurements
  if (historical.length > 50) {
    historical.splice(0, historical.length - 50);
  }

  fs.writeFileSync(METRICS_FILE, JSON.stringify(historical, null, 2));
}

function generateReport(currentMetrics, historical) {
  console.log('📊 Performance Monitoring Report');
  console.log('='.repeat(50));

  console.log(`📅 Date: ${new Date(currentMetrics.timestamp).toLocaleString()}`);
  console.log(`📦 Total Bundle Size: ${formatBytes(currentMetrics.totalSize)}`);
  console.log(`📁 File Count: ${currentMetrics.fileCount}`);
  console.log(`🏆 Performance Score: ${currentMetrics.performance.score}/100`);

  console.log('\n📈 Top 5 Largest Files:');
  currentMetrics.files.slice(0, 5).forEach((file, index) => {
    const status = file.size > 500000 ? '⚠️ ' : '✅ ';
    console.log(
      `${status}${index + 1}. ${file.name.padEnd(40)} ${formatBytes(file.size).padStart(10)} (${file.percentage}%)`
    );
  });

  console.log('\n💡 Recommendations:');
  currentMetrics.performance.recommendations.forEach(rec => {
    console.log(`   - ${rec}`);
  });

  if (historical.length > 1) {
    const previous = historical[historical.length - 2];
    const sizeDiff = currentMetrics.totalSize - previous.totalSize;
    const scoreDiff = currentMetrics.performance.score - previous.performance.score;

    console.log('\n📊 Changes from Previous Build:');
    console.log(`   Bundle Size: ${sizeDiff > 0 ? '+' : ''}${formatBytes(sizeDiff)}`);
    console.log(`   Performance Score: ${scoreDiff > 0 ? '+' : ''}${scoreDiff}`);

    if (sizeDiff > 100000) {
      console.log('   ⚠️  Bundle size increased significantly');
    } else if (sizeDiff < -100000) {
      console.log('   ✅ Bundle size decreased significantly');
    }
  }

  console.log('\n📈 Historical Trend:');
  if (historical.length >= 5) {
    const recent = historical.slice(-5);
    const avgSize = recent.reduce((sum, m) => sum + m.totalSize, 0) / recent.length;
    const avgScore = recent.reduce((sum, m) => sum + m.performance.score, 0) / recent.length;

    console.log(`   Average Size (last 5 builds): ${formatBytes(avgSize)}`);
    console.log(`   Average Score (last 5 builds): ${avgScore.toFixed(1)}/100`);
  }
}

function main() {
  console.log('🔍 Performance Monitoring...\n');

  const currentMetrics = getCurrentMetrics();
  const historical = loadHistoricalMetrics();

  saveMetrics(currentMetrics);
  generateReport(currentMetrics, historical);

  console.log('\n✅ Performance metrics saved to performance-metrics.json');
}

if (require.main === module) {
  main();
}

module.exports = {
  getCurrentMetrics,
  calculatePerformanceScore,
  getRecommendations,
};
