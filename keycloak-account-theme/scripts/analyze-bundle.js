#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * Bundle Analysis Script
 * Analyzes webpack bundle output and provides performance insights
 */

const DIST_DIR = path.join(__dirname, '../dist');
const JS_DIR = path.join(DIST_DIR, 'js');

function formatBytes(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function analyzeBundle() {
  console.log('🔍 Analyzing Bundle Performance...\n');

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

  console.log('📊 Bundle Analysis Results:');
  console.log('='.repeat(50));

  // Sort files by size (largest first)
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

  fileSizes.forEach(file => {
    const status = file.size > 500000 ? '⚠️ ' : '✅ ';
    console.log(
      `${status}${file.name.padEnd(40)} ${formatBytes(file.size).padStart(10)} (${file.percentage}%)`
    );
  });

  console.log('='.repeat(50));
  console.log(`📦 Total Bundle Size: ${formatBytes(totalSize)}`);

  // Performance recommendations
  console.log('\n🎯 Performance Recommendations:');

  const largeFiles = fileSizes.filter(file => file.size > 500000);
  if (largeFiles.length > 0) {
    console.log('⚠️  Large files detected:');
    largeFiles.forEach(file => {
      console.log(`   - ${file.name}: ${formatBytes(file.size)}`);
    });
    console.log('   Consider code splitting or lazy loading for these files.');
  } else {
    console.log('✅ All files are under 500KB - good performance!');
  }

  // Bundle composition analysis
  const muiFile = fileSizes.find(file => file.name.includes('mui'));
  const reactFile = fileSizes.find(file => file.name.includes('react'));
  const vendorFile = fileSizes.find(file => file.name.includes('vendor'));

  console.log('\n📈 Bundle Composition:');
  if (muiFile) {
    console.log(`   Material-UI: ${formatBytes(muiFile.size)} (${muiFile.percentage}%)`);
  }
  if (reactFile) {
    console.log(`   React: ${formatBytes(reactFile.size)} (${reactFile.percentage}%)`);
  }
  if (vendorFile) {
    console.log(`   Vendors: ${formatBytes(vendorFile.size)} (${vendorFile.percentage}%)`);
  }

  // Performance score
  let score = 100;
  if (totalSize > 2000000)
    score -= 30; // 2MB+
  else if (totalSize > 1500000)
    score -= 20; // 1.5MB+
  else if (totalSize > 1000000) score -= 10; // 1MB+

  largeFiles.forEach(() => (score -= 10));

  console.log(`\n🏆 Performance Score: ${score}/100`);

  if (score >= 90) {
    console.log('🌟 Excellent! Your bundle is well optimized.');
  } else if (score >= 70) {
    console.log('👍 Good! Consider some optimizations for better performance.');
  } else {
    console.log('⚠️  Needs improvement. Consider aggressive optimization.');
  }

  console.log('\n💡 Optimization Tips:');
  console.log('   - Use dynamic imports for code splitting');
  console.log('   - Implement lazy loading for routes');
  console.log('   - Consider tree shaking unused code');
  console.log('   - Use webpack-bundle-analyzer for detailed analysis');
  console.log('   - Enable gzip compression on your server');
}

if (require.main === module) {
  analyzeBundle();
}

module.exports = { analyzeBundle, formatBytes };
