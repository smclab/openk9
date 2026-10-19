import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
  input: "../../core/app/tenant-manager/target/generated/openapi.yaml",
  output: {
    path: "./src/openapi-generated",
    clean: true,
  },
  plugins: ["@hey-api/client-fetch", { name: "@hey-api/typescript", enums: "javascript" }, "@hey-api/sdk"],
});
