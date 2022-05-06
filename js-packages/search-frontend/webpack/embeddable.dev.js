// builds a script with react inside for use with <script src=""/>
// with sourcemaps and without compression for easier debugging

const path = require("path");

module.exports = {
  mode: "development",
  entry: {
    embeddable: "./src/embeddable/entry.tsx",
  },
  devtool: "source-map",
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            plugins: [
              "macros",
              [
                "@babel/plugin-transform-runtime",
                {
                  regenerator: true,
                },
              ],
            ],
            presets: ["@babel/preset-env", "@babel/react", "@babel/typescript"],
          },
        },
      },
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader"],
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: "asset/inline",
      },
    ],
  },
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
  output: {
    path: path.resolve(__dirname, "../dist"),
    filename: "[name].js",
    libraryTarget: "umd",
  },
};
