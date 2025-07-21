const path = require("path");
const { merge } = require("webpack-merge");
const common = require("./webpack.common.js");

const HtmlWebpackPlugin = require("html-webpack-plugin");

const defaultHtmlPluginConfig = {
  inject: true,
  template: path.resolve(__dirname, "public/index.html"),
  manifest: path.resolve(__dirname, "public/manifest.json"),
  filename: "index.html",
};

module.exports = merge(common, {
  mode: "development",
  devtool: "eval-source-map",
  devServer: {
    static: path.resolve(__dirname, "dist"),
    port: 3000,
  },
  plugins: [
    new HtmlWebpackPlugin({
      ...defaultHtmlPluginConfig,
    }),
  ],
  module: {
    rules: [
      {
        test: /.css$/,
        use: ["style-loader", "css-loader"],
      },
    ],
  },
});
