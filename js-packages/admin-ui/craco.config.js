const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
const CompressionPlugin = require("compression-webpack-plugin");
const path = require("path");

module.exports = {
  webpack: {
    alias: {
      "@components": path.resolve(__dirname, "src/components"),
      "@pages": path.resolve(__dirname, "src/pages"),
    },
    plugins: {
      add: [
        [
          new MonacoWebpackPlugin({
            languages: [],
            features: [
              "bracketMatching",
              "clipboard",
              "contextmenu",
              "coreCommands",
              "cursorUndo",
              "find",
              "format",
              "hover",
              "inPlaceReplace",
              "iPadShowKeyboard",
              "links",
              "multicursor",
              "suggest",
            ],
          }),
          "append",
        ],
        [
          new CompressionPlugin({
            filename: "[path][base].gz",
            algorithm: "gzip",
            test: /\.(js|css|html|svg)$/,
            threshold: 10240,
            minRatio: 0.8,
          }),
          "append",
        ],
        [
          new CompressionPlugin({
            filename: "[path][base].br",
            algorithm: "brotliCompress",
            test: /\.(js|css|html|svg)$/,
            compressionOptions: {
              level: 11,
            },
            threshold: 10240,
            minRatio: 0.8,
          }),
          "append",
        ],
      ],
    },
    configure: (webpackConfig, { env }) => {
      // Applica splitChunks avanzato solo in production
      if (env === "production") {
        webpackConfig.optimization = {
          ...webpackConfig.optimization,
          splitChunks: {
            chunks: "all",
            cacheGroups: {
              monacoEditor: {
                test: /[\\/]node_modules[\\/]monaco-editor[\\/]/,
                name: "monaco-editor",
                priority: 40,
                reuseExistingChunk: true,
              },
              mui: {
                test: /[\\/]node_modules[\\/]@mui[\\/]/,
                name: "mui",
                priority: 30,
                reuseExistingChunk: true,
              },
              apollo: {
                test: /[\\/]node_modules[\\/]@apollo[\\/]/,
                name: "apollo-client",
                priority: 25,
                reuseExistingChunk: true,
              },
              react: {
                test: /[\\/]node_modules[\\/](react|react-dom|react-router|react-router-dom)[\\/]/,
                name: "react-vendors",
                priority: 20,
                reuseExistingChunk: true,
              },
              vendors: {
                test: /[\\/]node_modules[\\/]/,
                name: "vendors",
                priority: 10,
                reuseExistingChunk: true,
              },
              common: {
                minChunks: 2,
                priority: 5,
                reuseExistingChunk: true,
                enforce: true,
              },
            },
            maxInitialRequests: 25,
            maxAsyncRequests: 25,
            minSize: 20000,
          },
          runtimeChunk: {
            name: "runtime",
          },
        };
      }

      return webpackConfig;
    },
  },
};
