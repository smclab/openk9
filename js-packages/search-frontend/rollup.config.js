import dts from "rollup-plugin-dts";

export default [
  {
    input: "src/embeddable/entry.tsx",
    plugins: [dts()],
    output: {
      file: `dist/embeddable.d.ts`,
      format: "es",
    },
  },
];
