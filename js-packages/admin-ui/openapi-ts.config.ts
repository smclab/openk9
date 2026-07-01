import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
  input: "../../core/app/datasource/target/generated/openapi.yaml",
  output: {
    path: "./src/openapi-generated",
    clean: true,
  },
  plugins: ["@hey-api/client-fetch", "@hey-api/typescript", "@hey-api/sdk"],
});
