import dts from "rollup-plugin-dts";

export default [
  {
    input: "src/index.tsx",
    plugins: [dts()],
    output: {
      file: `dist/index.d.ts`,
      format: "es",
    },
  },
];
