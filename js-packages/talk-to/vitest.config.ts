import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
	plugins: [react()],
	test: {
		environment: "jsdom",
		setupFiles: ["./src/setupTests.ts"],
		// ../shared is compiled by this app, so its tests run here
		include: ["src/**/*.{test,spec}.{ts,tsx}", "../shared/**/*.{test,spec}.{ts,tsx}"],
		// like resetMocks under CRA
		mockReset: true,
	},
});
