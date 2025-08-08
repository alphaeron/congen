const path = require('path');
const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CompressionPlugin = require('compression-webpack-plugin');
const { GenerateSW } = require('workbox-webpack-plugin');

const defaultHtmlPluginConfig = {
  inject: true,
  template: path.resolve(__dirname, 'public/index.html'),
  manifest: path.resolve(__dirname, 'public/manifest.json'),
  filename: 'index.html',
};

const additionalProdHtmlPluginConfig = {
  minify: {
    minifyJS: true,
    minifyCSS: true,
    minifyURLs: true,
    removeComments: true,
    useShortDoctype: true,
    keepClosingSlash: true,
    collapseWhitespace: true,
    removeEmptyAttributes: true,
    removeRedundantAttributes: true,
    removeStyleLinkTypeAttributes: true,
    removeScriptTypeAttributes: true,
    removeOptionalTags: true,
  },
  // Security headers and CSP
  meta: {
    'Content-Security-Policy': {
      'http-equiv': 'Content-Security-Policy',
      content:
        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:; connect-src 'self' https:; frame-ancestors 'none';",
    },
    'X-Content-Type-Options': {
      'http-equiv': 'X-Content-Type-Options',
      content: 'nosniff',
    },
    'X-Frame-Options': {
      'http-equiv': 'X-Frame-Options',
      content: 'DENY',
    },
    'X-XSS-Protection': {
      'http-equiv': 'X-XSS-Protection',
      content: '1; mode=block',
    },
    'Referrer-Policy': {
      'http-equiv': 'Referrer-Policy',
      content: 'strict-origin-when-cross-origin',
    },
    'Permissions-Policy': {
      'http-equiv': 'Permissions-Policy',
      content: 'camera=(), microphone=(), geolocation=(), payment=()',
    },
  },
};

module.exports = merge(common, {
  mode: 'production',
  devtool: 'source-map',

  plugins: [
    new MiniCssExtractPlugin({
      filename: 'css/[name].[contenthash:8].css',
      chunkFilename: 'css/[name].[contenthash:8].chunk.css',
    }),

    new HtmlWebpackPlugin({
      ...defaultHtmlPluginConfig,
      ...additionalProdHtmlPluginConfig,
    }),

    // Gzip compression for all assets
    new CompressionPlugin({
      algorithm: 'gzip',
      test: /\.(js|css|html|svg)$/,
      threshold: 10240, // Only process files larger than 10kb
      minRatio: 0.8, // Only compress if compression ratio is better than 0.8
    }),

    // Brotli compression for better compression ratios
    new CompressionPlugin({
      filename: '[path][base].br',
      algorithm: 'brotliCompress',
      test: /\.(js|css|html|svg)$/,
      threshold: 10240,
      minRatio: 0.8,
      compressionOptions: {
        level: 11,
      },
    }),

    // Service Worker for PWA capabilities
    new GenerateSW({
      clientsClaim: true,
      skipWaiting: true,
      maximumFileSizeToCacheInBytes: 5 * 1024 * 1024, // 5MB
      runtimeCaching: [
        {
          urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
          handler: 'CacheFirst',
          options: {
            cacheName: 'google-fonts-cache',
            expiration: {
              maxEntries: 10,
              maxAgeSeconds: 60 * 60 * 24 * 365, // 1 year
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
        {
          urlPattern: /^https:\/\/fonts\.gstatic\.com\/.*/i,
          handler: 'CacheFirst',
          options: {
            cacheName: 'gstatic-fonts-cache',
            expiration: {
              maxEntries: 10,
              maxAgeSeconds: 60 * 60 * 24 * 365, // 1 year
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
        {
          urlPattern: /\.(?:png|jpg|jpeg|svg|gif)$/,
          handler: 'CacheFirst',
          options: {
            cacheName: 'images-cache',
            expiration: {
              maxEntries: 50,
              maxAgeSeconds: 60 * 60 * 24 * 30, // 30 days
            },
          },
        },
        {
          urlPattern: /^https:\/\/.*\.congen\.com\/api\/.*/i,
          handler: 'NetworkFirst',
          options: {
            cacheName: 'api-cache',
            expiration: {
              maxEntries: 100,
              maxAgeSeconds: 60 * 5, // 5 minutes
            },
            networkTimeoutSeconds: 3,
          },
        },
      ],
    }),
  ],

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            options: {
              importLoaders: 1,
              sourceMap: false,
            },
          },
        ],
      },
    ],
  },

  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        parallel: true,
        terserOptions: {
          parse: {
            ecma: 2020,
          },
          compress: {
            ecma: 5,
            warnings: false,
            comparisons: false,
            inline: 2,
            drop_console: true,
            drop_debugger: true,
            pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn'],
          },
          mangle: {
            safari10: true,
          },
          output: {
            ecma: 5,
            comments: false,
            ascii_only: true,
          },
        },
        extractComments: false,
      }),
      new CssMinimizerPlugin({
        minimizerOptions: {
          preset: [
            'default',
            {
              discardComments: { removeAll: true },
              normalizeWhitespace: true,
              minifyFontValues: true,
              minifySelectors: true,
            },
          ],
        },
      }),
    ],

    splitChunks: {
      chunks: 'all',
      minSize: 20000,
      minRemainingSize: 0,
      minChunks: 1,
      maxAsyncRequests: 30,
      maxInitialRequests: 30,
      enforceSizeThreshold: 50000,
      cacheGroups: {
        defaultVendors: {
          test: /[\\/]node_modules[\\/]/,
          priority: -10,
          reuseExistingChunk: true,
          name(module) {
            const match = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/);
            if (match) {
              const packageName = match[1];
              return `vendor.${packageName.replace('@', '')}`;
            }
            return 'vendor';
          },
        },
        default: {
          minChunks: 2,
          priority: -20,
          reuseExistingChunk: true,
        },
        // Separate React and ReactDOM
        react: {
          test: /[\\/]node_modules[\\/](react|react-dom)[\\/]/,
          name: 'react',
          chunks: 'all',
          priority: 40,
        },
        // Separate Material-UI
        mui: {
          test: /[\\/]node_modules[\\/]@mui[\\/]/,
          name: 'mui',
          chunks: 'all',
          priority: 30,
        },
        // Separate vendor libraries
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all',
          priority: 20,
        },
        // Separate CSS
        styles: {
          name: 'styles',
          type: 'css/mini-extract',
          chunks: 'all',
          enforce: true,
        },
      },
    },

    runtimeChunk: {
      name: 'runtime',
    },
  },

  performance: {
    hints: 'warning',
    maxEntrypointSize: 512000,
    maxAssetSize: 512000,
  },

  output: {
    filename: 'js/[name].[contenthash:8].js',
    chunkFilename: 'js/[name].[contenthash:8].chunk.js',
    assetModuleFilename: 'assets/[name].[contenthash:8][ext]',
    publicPath: '/',
    clean: true,
  },

  // Security: Prevent eval and other dangerous features
  experiments: {
    topLevelAwait: true,
  },

  // Additional security measures
  resolve: {
    ...common.resolve,
    fallback: {
      ...common.resolve.fallback,
      crypto: false,
      stream: false,
      util: false,
      buffer: false,
      process: false,
    },
  },
});
